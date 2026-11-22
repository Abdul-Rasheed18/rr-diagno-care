<%-- Shared page header: <head>, top bar, navbar and flash messages. Set pageTitle / activeNav before including. --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${empty pageTitle ? 'Online Test Booking' : pageTitle}"/> | RR Diagno Care</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body>
<div class="rr-topbar py-1 no-print">
    <div class="container d-flex flex-wrap justify-content-between gap-2">
        <span><i class="bi bi-clock"></i> Mon&ndash;Sat 7:00 AM &ndash; 8:00 PM &nbsp;|&nbsp; Sun 7:00 AM &ndash; 1:00 PM</span>
        <span><i class="bi bi-telephone"></i> +91 98480 00001 &nbsp;|&nbsp; <i class="bi bi-house-heart"></i> Free home collection above &#8377;999</span>
    </div>
</div>

<nav class="navbar navbar-expand-lg rr-navbar sticky-top shadow-sm no-print">
    <div class="container">
        <a class="navbar-brand rr-brand d-flex align-items-center" href="${ctx}/">
            <span class="rr-logo"><i class="bi bi-heart-pulse-fill"></i></span>
            <span>RR Diagno Care<small>DIAGNOSTICS &amp; PATHOLOGY LAB</small></span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"
                aria-controls="mainNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav me-auto ms-lg-4">
                <li class="nav-item"><a class="nav-link ${activeNav == 'home' ? 'active' : ''}" href="${ctx}/">Home</a></li>
                <li class="nav-item"><a class="nav-link ${activeNav == 'tests' ? 'active' : ''}" href="${ctx}/tests">Tests &amp; Packages</a></li>
                <c:if test="${isPatient}">
                    <li class="nav-item"><a class="nav-link ${activeNav == 'bookings' ? 'active' : ''}" href="${ctx}/patient/bookings">My Bookings</a></li>
                </c:if>
                <c:if test="${isStaff}">
                    <li class="nav-item"><a class="nav-link ${activeNav == 'worklist' ? 'active' : ''}" href="${ctx}/staff/worklist"><i class="bi bi-clipboard2-pulse"></i> Lab Worklist</a></li>
                </c:if>
                <c:if test="${isAdmin}">
                    <li class="nav-item"><a class="nav-link ${activeNav == 'dashboard' ? 'active' : ''}" href="${ctx}/admin/dashboard">Dashboard</a></li>
                    <li class="nav-item"><a class="nav-link ${activeNav == 'manage-tests' ? 'active' : ''}" href="${ctx}/admin/tests">Manage Tests</a></li>
                    <li class="nav-item"><a class="nav-link ${activeNav == 'admin-bookings' ? 'active' : ''}" href="${ctx}/admin/bookings">Bookings</a></li>
                    <li class="nav-item"><a class="nav-link ${activeNav == 'staff' ? 'active' : ''}" href="${ctx}/admin/staff">Staff</a></li>
                </c:if>
            </ul>
            <ul class="navbar-nav align-items-lg-center gap-lg-2">
                <c:if test="${not isAdmin and not isStaff}">
                    <li class="nav-item">
                        <a class="nav-link position-relative ${activeNav == 'cart' ? 'active' : ''}" href="${ctx}/cart">
                            <i class="bi bi-cart3 fs-5"></i> Cart
                            <c:set var="cartCount" value="${fn:length(sessionScope.cart)}"/>
                            <span class="badge rounded-pill bg-danger cart-badge ${cartCount == 0 ? 'd-none' : ''}">${cartCount}</span>
                        </a>
                    </li>
                </c:if>
                <c:choose>
                    <c:when test="${loggedIn}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person-circle"></i>
                                <c:out value="${pageContext.request.userPrincipal.principal.fullName}"/>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><span class="dropdown-item-text small text-muted">
                                    Signed in as <strong><c:out value="${pageContext.request.userPrincipal.name}"/></strong><br>
                                    <c:choose>
                                        <c:when test="${isAdmin}">Administrator</c:when>
                                        <c:when test="${isStaff}">Lab Technician</c:when>
                                        <c:otherwise>Patient</c:otherwise>
                                    </c:choose>
                                </span></li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <form action="${ctx}/logout" method="post" class="px-3">
                                        ${csrfInput}
                                        <button type="submit" class="btn btn-sm btn-outline-danger w-100"><i class="bi bi-box-arrow-right"></i> Logout</button>
                                    </form>
                                </li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item"><a class="nav-link" href="${ctx}/login"><i class="bi bi-box-arrow-in-right"></i> Login</a></li>
                        <li class="nav-item"><a class="btn btn-primary btn-sm px-3" href="${ctx}/register">Register</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>

<main class="pb-5">
    <c:if test="${not empty success or not empty error}">
        <div class="container mt-3">
            <c:if test="${not empty success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="bi bi-check-circle-fill me-1"></i> <c:out value="${success}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i> <c:out value="${error}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>
        </div>
    </c:if>
