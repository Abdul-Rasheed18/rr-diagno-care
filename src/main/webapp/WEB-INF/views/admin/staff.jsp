<c:set var="pageTitle" value="Lab Staff"/>
<c:set var="activeNav" value="staff"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container-xl mt-4">
    <div class="page-header">
        <h1 class="section-title h3 mb-1">Lab Technicians</h1>
        <p class="text-muted mb-0">Create staff accounts and enable/disable access.</p>
    </div>

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="rr-card table-responsive">
                <table class="table table-rr mb-0">
                    <thead><tr><th>Name</th><th>Username</th><th>Contact</th><th>Created</th><th>Status</th><th class="text-end">Action</th></tr></thead>
                    <tbody>
                    <c:forEach var="s" items="${staffList}">
                        <tr class="${s.enabled ? '' : 'table-secondary'}">
                            <td class="fw-semibold"><c:out value="${s.fullName}"/></td>
                            <td><code><c:out value="${s.username}"/></code></td>
                            <td class="small"><c:out value="${s.email}"/><br><c:out value="${s.phone}"/></td>
                            <td class="small"><c:out value="${s.createdAtText}"/></td>
                            <td><span class="badge ${s.enabled ? 'bg-success' : 'bg-secondary'}">${s.enabled ? 'Active' : 'Disabled'}</span></td>
                            <td class="text-end">
                                <form action="${ctx}/admin/staff/${s.id}/toggle" method="post">
                                    ${csrfInput}
                                    <button class="btn btn-sm ${s.enabled ? 'btn-outline-danger' : 'btn-outline-success'}" type="submit">
                                        ${s.enabled ? 'Disable' : 'Enable'}</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty staffList}"><tr><td colspan="6" class="text-muted text-center py-4">No lab technicians yet.</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="col-lg-4">
            <form action="${ctx}/admin/staff" method="post" class="rr-card p-4">
                ${csrfInput}
                <h5 class="fw-bold mb-3"><i class="bi bi-person-plus text-teal"></i> New Lab Technician</h5>
                <div class="mb-2">
                    <label class="form-label small" for="sName">Full name *</label>
                    <input id="sName" name="fullName" class="form-control form-control-sm" maxlength="120" value="${fn:escapeXml(staffForm.fullName)}" required>
                </div>
                <div class="mb-2">
                    <label class="form-label small" for="sUser">Username *</label>
                    <input id="sUser" name="username" class="form-control form-control-sm" maxlength="30" value="${fn:escapeXml(staffForm.username)}" required>
                </div>
                <div class="mb-2">
                    <label class="form-label small" for="sEmail">Email *</label>
                    <input id="sEmail" name="email" type="email" class="form-control form-control-sm" maxlength="120" value="${fn:escapeXml(staffForm.email)}" required>
                </div>
                <div class="mb-2">
                    <label class="form-label small" for="sPhone">Mobile *</label>
                    <input id="sPhone" name="phone" type="tel" class="form-control form-control-sm" maxlength="14" value="${fn:escapeXml(staffForm.phone)}" required>
                </div>
                <div class="mb-3">
                    <label class="form-label small" for="sPass">Initial password *</label>
                    <input id="sPass" name="password" type="password" class="form-control form-control-sm" autocomplete="new-password" required>
                    <div class="form-text">Min 8 characters with letters and digits. Share it securely with the technician.</div>
                </div>
                <button type="submit" class="btn btn-primary btn-sm w-100"><i class="bi bi-person-check"></i> Create account</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
