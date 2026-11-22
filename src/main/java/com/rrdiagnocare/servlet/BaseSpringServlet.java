package com.rrdiagnocare.servlet;

import com.rrdiagnocare.exception.ResourceNotFoundException;
import com.rrdiagnocare.security.AppUserPrincipal;
import com.rrdiagnocare.security.SecurityUtil;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Common plumbing for the plain HttpServlets: access to Spring beans and friendly error handling. */
public abstract class BaseSpringServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected <T> T bean(Class<T> type) {
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        return ctx.getBean(type);
    }

    protected AppUserPrincipal currentUser() {
        return SecurityUtil.currentUser();
    }

    protected Long parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void forward(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + jsp).forward(req, resp);
    }

    protected void notFound(HttpServletRequest req, HttpServletResponse resp, String message) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        req.setAttribute("errorTitle", "Not found");
        req.setAttribute("errorMessage", message);
        forward(req, resp, "error.jsp");
    }

    protected boolean isNotFound(Exception e) {
        return e instanceof ResourceNotFoundException;
    }
}
