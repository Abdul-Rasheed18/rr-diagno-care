<c:set var="pageTitle" value="Manage Tests & Packages"/>
<c:set var="activeNav" value="manage-tests"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container-xl mt-4">
    <div class="page-header">
        <h1 class="section-title h3 mb-1">Manage Tests &amp; Packages</h1>
        <p class="text-muted mb-0">${fn:length(tests)} tests &middot; ${fn:length(packages)} packages. Inactive items are hidden from patients.</p>
    </div>

    <%-- ======================= Individual tests ======================= --%>
    <div class="row g-4 mb-5">
        <div class="col-xl-8">
            <div class="rr-card table-responsive">
                <table class="table table-rr table-hover mb-0">
                    <thead><tr><th>Code</th><th>Test</th><th>Category</th><th class="text-end">Price</th><th>Reference range</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
                    <tbody>
                    <c:forEach var="t" items="${tests}">
                        <tr class="${t.active ? '' : 'table-secondary'}">
                            <td class="small"><c:out value="${t.code}"/></td>
                            <td><div class="fw-semibold"><c:out value="${t.name}"/></div><small class="text-muted"><c:out value="${t.preparationNotes}"/></small></td>
                            <td class="small"><c:out value="${t.category}"/></td>
                            <td class="text-end">&#8377;<fmt:formatNumber value="${t.price}" pattern="#,##0.00"/></td>
                            <td class="small"><c:out value="${t.referenceRange}"/></td>
                            <td><span class="badge ${t.active ? 'bg-success' : 'bg-secondary'}">${t.active ? 'Active' : 'Inactive'}</span></td>
                            <td class="text-end text-nowrap">
                                <a href="${ctx}/admin/tests?editTest=${t.id}#test-form" class="btn btn-sm btn-outline-primary" title="Edit"><i class="bi bi-pencil"></i></a>
                                <form action="${ctx}/admin/tests/${t.id}/toggle" method="post" class="d-inline">
                                    ${csrfInput}
                                    <button class="btn btn-sm btn-outline-secondary" type="submit" title="${t.active ? 'Deactivate' : 'Activate'}">
                                        <i class="bi ${t.active ? 'bi-toggle-on' : 'bi-toggle-off'}"></i></button>
                                </form>
                                <form action="${ctx}/admin/tests/${t.id}/delete" method="post" class="d-inline"
                                      onsubmit="return confirm('Delete this test permanently? Tests used in packages or bookings cannot be deleted.');">
                                    ${csrfInput}
                                    <button class="btn btn-sm btn-outline-danger" type="submit" title="Delete"><i class="bi bi-trash"></i></button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="col-xl-4" id="test-form">
            <form action="${ctx}/admin/tests/save" method="post" class="rr-card p-4">
                ${csrfInput}
                <h5 class="fw-bold mb-3">${empty testForm.id ? 'Add new test' : 'Edit test'}</h5>
                <c:if test="${not empty testForm.id}"><input type="hidden" name="id" value="${testForm.id}"></c:if>
                <div class="row g-2">
                    <div class="col-5">
                        <label class="form-label small" for="tCode">Code *</label>
                        <input id="tCode" name="code" class="form-control form-control-sm" maxlength="20" value="${fn:escapeXml(testForm.code)}" required>
                    </div>
                    <div class="col-7">
                        <label class="form-label small" for="tCategory">Category *</label>
                        <select id="tCategory" name="category" class="form-select form-select-sm">
                            <c:forEach var="cat" items="${categories}">
                                <option value="${fn:escapeXml(cat)}" ${testForm.category == cat ? 'selected' : ''}><c:out value="${cat}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12">
                        <label class="form-label small" for="tName">Test name *</label>
                        <input id="tName" name="name" class="form-control form-control-sm" maxlength="150" value="${fn:escapeXml(testForm.name)}" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small" for="tPrice">Price (&#8377;) *</label>
                        <input id="tPrice" name="price" type="number" step="0.01" min="0" class="form-control form-control-sm" value="${testForm.price}" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small" for="tTat">Report in (hrs)</label>
                        <input id="tTat" name="turnaroundHours" type="number" min="1" class="form-control form-control-sm" value="${empty testForm.id ? 24 : testForm.turnaroundHours}">
                    </div>
                    <div class="col-4">
                        <label class="form-label small" for="tMin">Normal min</label>
                        <input id="tMin" name="normalMin" type="number" step="0.01" class="form-control form-control-sm" value="${testForm.normalMin}">
                    </div>
                    <div class="col-4">
                        <label class="form-label small" for="tMax">Normal max</label>
                        <input id="tMax" name="normalMax" type="number" step="0.01" class="form-control form-control-sm" value="${testForm.normalMax}">
                    </div>
                    <div class="col-4">
                        <label class="form-label small" for="tUnit">Unit</label>
                        <input id="tUnit" name="unit" class="form-control form-control-sm" maxlength="30" value="${fn:escapeXml(testForm.unit)}">
                    </div>
                    <div class="col-12">
                        <label class="form-label small" for="tSample">Sample type</label>
                        <input id="tSample" name="sampleType" class="form-control form-control-sm" maxlength="50" value="${fn:escapeXml(empty testForm.sampleType ? 'Blood' : testForm.sampleType)}">
                    </div>
                    <div class="col-12">
                        <label class="form-label small" for="tPrep">Preparation notes</label>
                        <input id="tPrep" name="preparationNotes" class="form-control form-control-sm" maxlength="255"
                               placeholder="e.g. 10-12 hrs fasting required" value="${fn:escapeXml(testForm.preparationNotes)}">
                    </div>
                    <div class="col-12">
                        <label class="form-label small" for="tDesc">Description</label>
                        <textarea id="tDesc" name="description" rows="2" class="form-control form-control-sm" maxlength="500">${fn:escapeXml(testForm.description)}</textarea>
                    </div>
                    <div class="col-12 form-check ms-1">
                        <input type="hidden" name="_active" value="on">
                        <input class="form-check-input" type="checkbox" id="tActive" name="active" ${testForm.active ? 'checked' : ''}>
                        <label class="form-check-label small" for="tActive">Active (visible to patients)</label>
                    </div>
                </div>
                <div class="d-flex gap-2 mt-3">
                    <button type="submit" class="btn btn-primary btn-sm flex-grow-1"><i class="bi bi-save"></i> ${empty testForm.id ? 'Add test' : 'Save changes'}</button>
                    <c:if test="${not empty testForm.id}"><a href="${ctx}/admin/tests" class="btn btn-outline-secondary btn-sm">Cancel</a></c:if>
                </div>
            </form>
        </div>
    </div>

    <%-- ======================= Health packages ======================= --%>
    <h2 class="h4 fw-bold mb-3" id="packages"><i class="bi bi-box2-heart text-teal"></i> Health Packages</h2>
    <div class="row g-4">
        <div class="col-xl-8">
            <div class="rr-card table-responsive">
                <table class="table table-rr table-hover mb-0">
                    <thead><tr><th>Package</th><th>Tests</th><th class="text-end">MRP</th><th class="text-end">Price</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
                    <tbody>
                    <c:forEach var="p" items="${packages}">
                        <tr class="${p.active ? '' : 'table-secondary'}">
                            <td>
                                <div class="fw-semibold"><c:out value="${p.name}"/> <c:if test="${p.featured}"><span class="badge bg-warning text-dark">Featured</span></c:if></div>
                                <small class="text-muted"><c:forEach var="t" items="${p.tests}" varStatus="s"><c:out value="${t.code}"/>${s.last ? '' : ', '}</c:forEach></small>
                            </td>
                            <td>${p.testCount}</td>
                            <td class="text-end mrp">&#8377;<fmt:formatNumber value="${p.mrp}" pattern="#,##0"/></td>
                            <td class="text-end fw-semibold">&#8377;<fmt:formatNumber value="${p.price}" pattern="#,##0"/>
                                <c:if test="${p.discountPercent > 0}"><br><small class="text-success">${p.discountPercent}% off</small></c:if></td>
                            <td><span class="badge ${p.active ? 'bg-success' : 'bg-secondary'}">${p.active ? 'Active' : 'Inactive'}</span></td>
                            <td class="text-end text-nowrap">
                                <a href="${ctx}/admin/tests?editPackage=${p.id}#package-form" class="btn btn-sm btn-outline-primary" title="Edit"><i class="bi bi-pencil"></i></a>
                                <form action="${ctx}/admin/packages/${p.id}/toggle" method="post" class="d-inline">
                                    ${csrfInput}
                                    <button class="btn btn-sm btn-outline-secondary" type="submit" title="Toggle"><i class="bi ${p.active ? 'bi-toggle-on' : 'bi-toggle-off'}"></i></button>
                                </form>
                                <form action="${ctx}/admin/packages/${p.id}/delete" method="post" class="d-inline"
                                      onsubmit="return confirm('Delete this package? Existing bookings keep their receipt lines.');">
                                    ${csrfInput}
                                    <button class="btn btn-sm btn-outline-danger" type="submit" title="Delete"><i class="bi bi-trash"></i></button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="col-xl-4" id="package-form">
            <form action="${ctx}/admin/packages/save" method="post" class="rr-card p-4">
                ${csrfInput}
                <h5 class="fw-bold mb-3">${empty packageForm.id ? 'Create package' : 'Edit package'}</h5>
                <c:if test="${not empty packageForm.id}"><input type="hidden" name="id" value="${packageForm.id}"></c:if>
                <div class="mb-2">
                    <label class="form-label small" for="pName">Package name *</label>
                    <input id="pName" name="name" class="form-control form-control-sm" maxlength="150" value="${fn:escapeXml(packageForm.name)}" required>
                </div>
                <div class="mb-2">
                    <label class="form-label small" for="pDesc">Description</label>
                    <textarea id="pDesc" name="description" rows="2" class="form-control form-control-sm" maxlength="500">${fn:escapeXml(packageForm.description)}</textarea>
                </div>
                <div class="mb-2">
                    <label class="form-label small" for="pPrep">Preparation notes</label>
                    <input id="pPrep" name="preparationNotes" class="form-control form-control-sm" maxlength="255" value="${fn:escapeXml(packageForm.preparationNotes)}">
                </div>
                <label class="form-label small mb-1">Bundle tests * <span class="text-muted">(MRP: &#8377;<span id="mrpTotal">0</span>)</span></label>
                <div class="border rounded-3 p-2 mb-2" style="max-height: 220px; overflow-y: auto;">
                    <c:set var="selectedIds" value="${packageForm.testIds}"/>
                    <c:forEach var="t" items="${activeTests}">
                        <div class="form-check small">
                            <input class="form-check-input pkg-test" type="checkbox" name="testIds" value="${t.id}" id="pt${t.id}"
                                   data-price="${t.price}" ${selectedIds.contains(t.id) ? 'checked' : ''}>
                            <label class="form-check-label d-flex justify-content-between" for="pt${t.id}">
                                <span><c:out value="${t.name}"/></span><span class="text-muted">&#8377;<fmt:formatNumber value="${t.price}" pattern="#,##0"/></span>
                            </label>
                        </div>
                    </c:forEach>
                </div>
                <div class="row g-2 align-items-end">
                    <div class="col-6">
                        <label class="form-label small" for="pPrice">Package price (&#8377;) *</label>
                        <input id="pPrice" name="price" type="number" step="0.01" min="1" class="form-control form-control-sm" value="${packageForm.price}" required>
                    </div>
                    <div class="col-6 small" id="discountHint"></div>
                </div>
                <div class="form-check mt-2">
                    <input type="hidden" name="_featured" value="on">
                    <input class="form-check-input" type="checkbox" id="pFeatured" name="featured" ${packageForm.featured ? 'checked' : ''}>
                    <label class="form-check-label small" for="pFeatured">Feature on home page</label>
                </div>
                <div class="form-check">
                    <input type="hidden" name="_active" value="on">
                    <input class="form-check-input" type="checkbox" id="pActive" name="active" ${packageForm.active ? 'checked' : ''}>
                    <label class="form-check-label small" for="pActive">Active</label>
                </div>
                <div class="d-flex gap-2 mt-3">
                    <button type="submit" class="btn btn-primary btn-sm flex-grow-1"><i class="bi bi-save"></i> ${empty packageForm.id ? 'Create package' : 'Save changes'}</button>
                    <c:if test="${not empty packageForm.id}"><a href="${ctx}/admin/tests#packages" class="btn btn-outline-secondary btn-sm">Cancel</a></c:if>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
(function () {
    var boxes = document.querySelectorAll('.pkg-test');
    var price = document.getElementById('pPrice');
    var mrpEl = document.getElementById('mrpTotal');
    var hint = document.getElementById('discountHint');
    function update() {
        var mrp = 0;
        boxes.forEach(function (b) { if (b.checked) { mrp += parseFloat(b.dataset.price); } });
        mrpEl.textContent = mrp.toFixed(0);
        var p = parseFloat(price.value);
        if (mrp > 0 && !isNaN(p)) {
            var pct = Math.round((mrp - p) * 100 / mrp);
            hint.innerHTML = p > mrp ? '<span class="text-danger">Price exceeds MRP</span>' : '<span class="text-success">' + pct + '% discount</span>';
        } else {
            hint.textContent = '';
        }
    }
    boxes.forEach(function (b) { b.addEventListener('change', update); });
    price.addEventListener('input', update);
    update();
})();
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
