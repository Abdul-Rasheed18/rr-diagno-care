<c:set var="pageTitle" value="Register"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="rr-card p-4 p-md-5">
                <h1 class="h4 fw-bold mb-1">Create your patient account</h1>
                <p class="text-muted small mb-4">Register once to book tests, track samples and download reports.</p>

                <form action="${ctx}/register" method="post" novalidate>
                    ${csrfInput}
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label for="fullName" class="form-label">Full name *</label>
                            <input type="text" class="form-control" id="fullName" name="fullName" maxlength="120"
                                   value="${fn:escapeXml(form.fullName)}" required>
                        </div>
                        <div class="col-md-6">
                            <label for="username" class="form-label">Username *</label>
                            <input type="text" class="form-control" id="username" name="username" maxlength="30"
                                   value="${fn:escapeXml(form.username)}" autocomplete="username" required>
                            <div class="form-text">3&ndash;30 characters, starts with a letter.</div>
                        </div>
                        <div class="col-md-6">
                            <label for="email" class="form-label">Email *</label>
                            <input type="email" class="form-control" id="email" name="email" maxlength="120"
                                   value="${fn:escapeXml(form.email)}" required>
                        </div>
                        <div class="col-md-6">
                            <label for="phone" class="form-label">Mobile number *</label>
                            <div class="input-group">
                                <span class="input-group-text">+91</span>
                                <input type="tel" class="form-control" id="phone" name="phone" maxlength="14" pattern="[6-9][0-9]{9}"
                                       value="${fn:escapeXml(form.phone)}" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <label for="gender" class="form-label">Gender</label>
                            <select class="form-select" id="gender" name="gender">
                                <option value="">Prefer not to say</option>
                                <option value="MALE" ${form.gender == 'MALE' ? 'selected' : ''}>Male</option>
                                <option value="FEMALE" ${form.gender == 'FEMALE' ? 'selected' : ''}>Female</option>
                                <option value="OTHER" ${form.gender == 'OTHER' ? 'selected' : ''}>Other</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label for="dateOfBirth" class="form-label">Date of birth</label>
                            <input type="date" class="form-control" id="dateOfBirth" name="dateOfBirth" value="${form.dateOfBirth}">
                        </div>
                        <div class="col-12">
                            <label for="address" class="form-label">Address <small class="text-muted">(used for home collection)</small></label>
                            <textarea class="form-control" id="address" name="address" rows="2" maxlength="255">${fn:escapeXml(form.address)}</textarea>
                        </div>
                        <div class="col-md-6">
                            <label for="password" class="form-label">Password *</label>
                            <input type="password" class="form-control" id="password" name="password" autocomplete="new-password" required>
                            <div class="form-text">Min 8 characters with letters and digits.</div>
                        </div>
                        <div class="col-md-6">
                            <label for="confirmPassword" class="form-label">Confirm password *</label>
                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" autocomplete="new-password" required>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary btn-lg w-100 mt-4">Create account</button>
                </form>
                <p class="text-center small mt-4 mb-0">Already registered? <a href="${ctx}/login" class="fw-semibold">Login</a></p>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
