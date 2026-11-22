# RR Diagno Care — Online Test Booking & Lab Management Portal

Classic Maven **WAR** web application: Spring MVC 5.3.39 + Spring Security 5.8.16 + Spring JDBC/AOP/TX,
JSP/JSTL views with Bootstrap 5, MySQL 8, deployed on **Apache Tomcat 9.0.x** (`javax.servlet`).
No Spring Boot, no React, and no online payment gateway. Payment is chosen offline (Pay at Center / Pay on Home Collection).

---

## 1. Project tree

```
rr-diagno-care/
├── pom.xml                         WAR packaging, Spring 5.3.39, Security 5.8.16, release 21
├── .gitignore
├── database/
│   ├── schema.sql                  DB + tables + stored procedure sp_get_daily_revenue
│   └── data.sql                    12 tests, 3 packages, slots, 3 users, demo bookings
└── src/main/
    ├── java/com/rrdiagnocare/
    │   ├── model/        User, DiagnosticTest, HealthPackage, Booking, BookingItem, CartItem,
    │   │                 DailyRevenueReport, AuditLog, TestResult, TimeSlot, CheckoutForm
    │   ├── dao/          UserDao, DiagnosticTestDao, HealthPackageDao, BookingDao (batchUpdate),
    │   │                 TimeSlotDao (SELECT … FOR UPDATE), AuditLogDao,
    │   │                 RevenueReportDao (SimpleJdbcCall + CallableStatement → sp_get_daily_revenue)
    │   ├── servlet/      BookingReceiptServlet, ReportPrintServlet, ExportBookingsServlet,
    │   │                 RequestAuditFilter, ActiveSessionListener, JdbcCleanupListener, BaseSpringServlet
    │   ├── service/      UserService, CatalogService, CartService, BookingService (@Transactional),
    │   │                 LabService (@LabAudited), DashboardService
    │   ├── aspect/       LabAuditAspect (@Aspect/@Around), LabAudited (annotation)
    │   ├── security/     CustomUserDetailsService, CustomAuthSuccessHandler, AppUserPrincipal, SecurityUtil
    │   ├── controller/   PublicController, CartBookingController, StaffController, AdminController,
    │   │                 GlobalExceptionHandler (@ControllerAdvice)
    │   ├── exception/    BusinessException, ResourceNotFoundException
    │   └── util/         AppConstants, DateUtil, ValidationUtil
    ├── resources/
    │   ├── db.properties           JDBC URL / user / password  <-- edit this
    │   └── simplelogger.properties
    └── webapp/
        ├── assets/css/app.css      white & teal medical theme
        └── WEB-INF/
            ├── web.xml             listeners, filters, DispatcherServlet, 3 plain servlets, JSP config
            ├── applicationContext.xml   DataSource (HikariCP), JdbcTemplate, TxManager, AOP, Spring Security
            ├── dispatcher-servlet.xml   MVC, static resources, view resolver
            └── views/
                ├── common/   taglibs.jspf (prelude), header.jsp, footer.jsp, result-table.jsp
                ├── index.jsp, tests.jsp, cart.jsp, checkout.jsp, my-bookings.jsp, view-report.jsp,
                ├── login.jsp, register.jsp, error.jsp, receipt.jsp, report-print.jsp
                ├── staff/    worklist.jsp, enter-result.jsp
                └── admin/    dashboard.jsp, manage-tests.jsp, bookings.jsp, staff.jsp
```

## 2. Default logins (seeded by `data.sql`)

| Role | Username | Password | Lands on |
|---|---|---|---|
| ROLE_ADMIN | `admin` | `Admin@123` | `/admin/dashboard` |
| ROLE_STAFF (Lab Technician) | `staff` | `Staff@123` | `/staff/worklist` |
| ROLE_PATIENT | `patient` | `Patient@123` | `/patient/bookings` (or checkout if the cart has items) |

Passwords are stored as BCrypt hashes (strength 10). Change them before any real use.

## 3. Prerequisites

* JDK 21 or newer (developed for JDK 24; `pom.xml` compiles with `release 21`)
* Apache Maven 3.9+ (IntelliJ's bundled Maven is fine)
* MySQL Server 8.x
* **Apache Tomcat 9.0.x** (download the zip from https://tomcat.apache.org/download-90.cgi).
  Tomcat 10/11 use `jakarta.servlet` and **will not run** this WAR.

## 4. Create the database

From a terminal in the project folder (adjust the MySQL path / user):

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "source database/schema.sql"
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p rr_diagno_care -e "source database/data.sql"
```

Or in **MySQL Workbench**: *File → Open SQL Script → `schema.sql` → Execute (⚡)*, then the same for `data.sql`.
`schema.sql` drops and recreates the `rr_diagno_care` database, so re-running both scripts resets everything.

Check the stored procedure:

```sql
CALL rr_diagno_care.sp_get_daily_revenue(CURDATE(), @b, @c, @rev, @paid, @pending);
SELECT @b AS bookings, @c AS cancelled, @rev AS revenue, @paid AS collected, @pending AS pending;
```

Then set your MySQL credentials in `src/main/resources/db.properties`:

```properties
jdbc.username=root
jdbc.password=root
```

Any key can instead be overridden with a JVM option, e.g. `-Djdbc.password=secret` (Tomcat VM options).

## 5. Run in IntelliJ IDEA

### 5a. Import the project (both editions)
1. *File → Open…* → select `rr-diagno-care/pom.xml` → **Open as Project**.
2. Wait for Maven to download dependencies (Maven tool window → ⟳ *Reload All Maven Projects* if needed).
3. *File → Project Structure → Project*: SDK = your JDK 24 (or 21+). Language level is taken from `pom.xml`.

### 5b. IntelliJ IDEA **Ultimate** — integrated Tomcat 9
1. *Run → Edit Configurations… → + → Tomcat Server → Local*.
2. **Application server** → *Configure…* → point *Tomcat Home* to your unzipped `apache-tomcat-9.0.x` folder.
3. **Deployment** tab → *+ → Artifact…* → choose **`rr-diagno-care:war exploded`**.
   Set *Application context* to **`/rr-diagno-care`**.
4. **Server** tab: URL `http://localhost:8080/rr-diagno-care/`, HTTP port `8080`,
   *On 'Update' action* = **Update classes and resources** (hot-redeploy JSPs/classes).
   Optional VM options: `-Djdbc.password=yourPassword`.
5. Click **Run ▶**. The browser opens the home page.

### 5c. IntelliJ IDEA **Community** — build the WAR and deploy into Tomcat
Community Edition has no Tomcat integration, so deploy the WAR manually:
1. Maven tool window → *rr-diagno-care → Lifecycle → **package*** (or run `mvn clean package`).
   This produces `target/rr-diagno-care.war`.
2. Copy the WAR into Tomcat's `webapps` folder:
   ```powershell
   Copy-Item target\rr-diagno-care.war C:\apache-tomcat-9.0.98\webapps\
   ```
3. Start Tomcat (set `JAVA_HOME` to your JDK first if it is not set system-wide):
   ```powershell
   C:\apache-tomcat-9.0.98\bin\startup.bat
   ```
   Tomcat unpacks the WAR automatically. To redeploy after code changes, run `package` again and copy
   the new WAR over the old one; Tomcat hot-redeploys it within a few seconds.
4. Stop with `C:\apache-tomcat-9.0.98\bin\shutdown.bat`.

*Tip:* To pass the DB password without editing `db.properties`, create `bin\setenv.bat` in Tomcat with
`set "CATALINA_OPTS=-Djdbc.password=yourPassword"`.

### 5d. Open the application
* Home: http://localhost:8080/rr-diagno-care/
* Login: http://localhost:8080/rr-diagno-care/login

## 6. Feature tour

**Patient**: search/filter the catalog (Blood, Thyroid, Diabetes, Liver & Kidney, Full Body Packages), add tests or
packages to the session cart (with a warning if a test is already inside a cart package), checkout with a future
date (tomorrow up to 30 days ahead), slot (live remaining capacity, locked with `SELECT … FOR UPDATE`), walk-in or
home collection, and offline payment choice. Then print the receipt (`/print/receipt`, a plain `HttpServlet`), track status
`BOOKED → SAMPLE_COLLECTED → PROCESSING → COMPLETED`, cancel while `BOOKED`, and view/print reports with
abnormal values in **bold red** (JSTL `<c:choose>`).

**Lab Technician**: daily worklist with date/status filter, *Sample collected* → *Start processing*, result entry
(batch update) with live out-of-range hints, *Save draft* or *Save & mark COMPLETED*. Every action is timed and
written to `audit_logs` by `LabAuditAspect`, failures included.

**Admin**: dashboard with revenue from `sp_get_daily_revenue` (selected day via `SimpleJdbcCall`, 7-day trend via an
explicit `CallableStatement`), total bookings, pending reports, active sessions (`ActiveSessionListener`), recent
audit trail; test CRUD (delete blocked when a test is in use, so deactivate it instead), package bundling with MRP and discount
preview; booking filters, toggle `PAID`/`PENDING`, cancel, CSV export (`ExportBookingsServlet`, Excel-ready UTF-8 BOM);
create and disable lab technician accounts.

## 7. Implementation notes

* **Version lock:** Spring 5.3.39 / Security 5.8.16 are the last lines on `javax.servlet`; do not upgrade to 6.x.
* **Transactions:** `BookingService.createBooking` inserts `bookings`, `booking_items` and `test_results`
  in one `@Transactional` method, so any failure rolls back all three.
* **AOP order:** `LabAuditAspect` is `@Order(0)`, outside the transaction advice, so audit rows survive rollbacks.
  It must not be `HIGHEST_PRECEDENCE`, because Spring's `ExposeInvocationInterceptor` has to run first for `@annotation(...)` binding.
* **Security:** CSRF is on (every POST form includes the token), role-based URL rules live in `applicationContext.xml`,
  and patients can only open their own bookings/reports.
* **JSP:** `scripting-invalid=true` in `web.xml`, so no scriptlets are possible; common taglibs come from `taglibs.jspf`.
* **Logs:** `RequestAuditFilter` logs every request with its status and time (`[REQ] GET /tests -> 200 user=- 12ms`).
