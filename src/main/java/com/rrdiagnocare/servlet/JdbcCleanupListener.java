package com.rrdiagnocare.servlet;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * On undeploy/redeploy: deregisters the MySQL JDBC driver loaded by this web app and stops
 * the connector's cleanup thread, preventing Tomcat's "memory leak" warnings.
 * Declared first in web.xml so its contextDestroyed runs last (after Spring closes the pool).
 */
public class JdbcCleanupListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(JdbcCleanupListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing to do on startup
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ClassLoader webappLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == webappLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                    log.info("Deregistered JDBC driver {}", driver.getClass().getName());
                } catch (SQLException e) {
                    log.warn("Could not deregister JDBC driver {}: {}", driver, e.getMessage());
                }
            }
        }
        AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
