<c:set var="pageTitle" value="Checkout"/>
<c:set var="activeNav" value="cart"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container mt-4">
    <div class="page-header">
        <h1 class="section-title h3 mb-1">Book your appointment</h1>
        <p class="text-muted mb-0">Choose a date, time slot, visit type and how you would like to pay.</p>
    </div>

    <form action="${ctx}/patient/checkout" method="post" id="checkoutForm" novalidate>
        ${csrfInput}
        <div class="row g-4">
            <div class="col-lg-8">
                <%-- 1. Date & slot --%>
                <div class="rr-card p-4 mb-4">
                    <h5 class="fw-bold mb-3"><span class="badge bg-teal me-2">1</span>Appointment date &amp; time slot</h5>
                    <div class="row g-3 align-items-end mb-3">
                        <div class="col-md-5">
                            <label for="appointmentDate" class="form-label">Appointment date</label>
                            <input type="date" id="appointmentDate" name="appointmentDate" class="form-control"
                                   min="${minDate}" max="${maxDate}" value="${fn:escapeXml(form.appointmentDate)}" required>
                        </div>
                        <div class="col-md-7 small text-muted">
                            <i class="bi bi-info-circle"></i> Bookings open from tomorrow up to 30 days ahead.
                            Slots show remaining seats for <strong id="slotDateText"><c:out value="${slotDate}"/></strong>.
                        </div>
                    </div>
                    <div class="row g-2" id="slotContainer">
                        <c:forEach var="s" items="${slots}" varStatus="st">
                            <div class="col-6 col-md-4 slot-option">
                                <input type="radio" name="timeSlot" id="slot${st.index}" value="${fn:escapeXml(s.label)}"
                                       ${s.full ? 'disabled' : ''} ${form.timeSlot == s.label and not s.full ? 'checked' : ''}>
                                <label for="slot${st.index}">${fn:escapeXml(s.label)}
                                    <small><c:choose><c:when test="${s.full}">Fully booked</c:when><c:otherwise>${s.available} of ${s.capacity} left</c:otherwise></c:choose></small>
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                    <div id="slotMessage" class="text-danger small mt-2"></div>
                </div>

                <%-- 2. Visit mode --%>
                <div class="rr-card p-4 mb-4">
                    <h5 class="fw-bold mb-3"><span class="badge bg-teal me-2">2</span>Visit type</h5>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="form-check border rounded-3 p-3 ps-5 h-100">
                                <input class="form-check-input" type="radio" name="visitMode" id="vmWalkIn" value="WALK_IN"
                                       ${form.visitMode != 'HOME_COLLECTION' ? 'checked' : ''}>
                                <label class="form-check-label" for="vmWalkIn">
                                    <strong><i class="bi bi-hospital text-teal"></i> Walk-in at RR Diagno Care</strong><br>
                                    <small class="text-muted">Visit our centre at Labbipet, Vijayawada</small>
                                </label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-check border rounded-3 p-3 ps-5 h-100">
                                <input class="form-check-input" type="radio" name="visitMode" id="vmHome" value="HOME_COLLECTION"
                                       ${form.visitMode == 'HOME_COLLECTION' ? 'checked' : ''}>
                                <label class="form-check-label" for="vmHome">
                                    <strong><i class="bi bi-house-heart text-teal"></i> Home Sample Collection</strong><br>
                                    <small class="text-muted">Our phlebotomist visits you in the chosen slot</small>
                                </label>
                            </div>
                        </div>
                    </div>
                    <div class="mt-3" id="addressBlock">
                        <label for="collectionAddress" class="form-label">Collection address</label>
                        <textarea id="collectionAddress" name="collectionAddress" rows="2" class="form-control"
                                  placeholder="House no, street, area, landmark, city">${fn:escapeXml(form.collectionAddress)}</textarea>
                    </div>
                    <div class="mt-3">
                        <label for="contactPhone" class="form-label">Contact mobile number</label>
                        <div class="input-group">
                            <span class="input-group-text">+91</span>
                            <input type="tel" id="contactPhone" name="contactPhone" class="form-control" maxlength="14"
                                   pattern="[6-9][0-9]{9}" value="${fn:escapeXml(form.contactPhone)}" required>
                        </div>
                        <div class="form-text">10-digit Indian mobile number starting with 6, 7, 8 or 9.</div>
                    </div>
                </div>

                <%-- 3. Payment --%>
                <div class="rr-card p-4">
                    <h5 class="fw-bold mb-3"><span class="badge bg-teal me-2">3</span>Payment preference (offline)</h5>
                    <div class="form-check mb-2">
                        <input class="form-check-input" type="radio" name="paymentMode" id="payCenter" value="PAY_AT_CENTER"
                               ${form.paymentMode != 'PAY_ON_COLLECTION' ? 'checked' : ''}>
                        <label class="form-check-label" for="payCenter"><strong>Pay at Center</strong> &mdash; cash / UPI / card at the billing counter</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="paymentMode" id="payCollection" value="PAY_ON_COLLECTION"
                               ${form.paymentMode == 'PAY_ON_COLLECTION' ? 'checked' : ''}>
                        <label class="form-check-label" for="payCollection"><strong>Pay on Home Collection</strong> &mdash; pay our phlebotomist at your doorstep</label>
                    </div>
                    <p class="small text-muted mt-2 mb-0"><i class="bi bi-shield-check"></i> No card details are collected online. Payment status stays PENDING until paid.</p>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="rr-card p-4 position-sticky" style="top: 90px;">
                    <h5 class="fw-bold mb-3">Booking summary</h5>
                    <ul class="list-unstyled small">
                        <c:forEach var="item" items="${cartItems}">
                            <li class="d-flex justify-content-between border-bottom py-2">
                                <span><c:out value="${item.name}"/><c:if test="${item['package']}"> <span class="text-muted">(${item.testCount} tests)</span></c:if></span>
                                <span class="fw-semibold">&#8377;<fmt:formatNumber value="${item.price}" pattern="#,##0.00"/></span>
                            </li>
                        </c:forEach>
                    </ul>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <span class="fw-bold">Total</span>
                        <span class="price">&#8377;<fmt:formatNumber value="${cartTotal}" pattern="#,##0.00"/></span>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 btn-lg"><i class="bi bi-calendar-check"></i> Confirm booking</button>
                    <a href="${ctx}/cart" class="btn btn-link w-100 mt-1">Back to cart</a>
                </div>
            </div>
        </div>
    </form>
</div>

<script>
(function () {
    var ctx = '${ctx}';
    var dateInput = document.getElementById('appointmentDate');
    var container = document.getElementById('slotContainer');
    var message = document.getElementById('slotMessage');
    var dateText = document.getElementById('slotDateText');
    var walkIn = document.getElementById('vmWalkIn');
    var home = document.getElementById('vmHome');
    var addressBlock = document.getElementById('addressBlock');
    var payCenter = document.getElementById('payCenter');
    var payCollection = document.getElementById('payCollection');

    function syncVisitMode() {
        var isHome = home.checked;
        addressBlock.style.display = isHome ? '' : 'none';
        payCollection.disabled = !isHome;
        if (!isHome && payCollection.checked) { payCenter.checked = true; }
    }
    walkIn.addEventListener('change', syncVisitMode);
    home.addEventListener('change', syncVisitMode);
    syncVisitMode();

    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, function (c) {
            return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c];
        });
    }

    dateInput.addEventListener('change', function () {
        var selected = (container.querySelector('input[name=timeSlot]:checked') || {}).value;
        fetch(ctx + '/patient/slots?date=' + encodeURIComponent(dateInput.value), {credentials: 'same-origin'})
            .then(function (r) { return r.json(); })
            .then(function (data) {
                message.textContent = data.valid ? '' : data.message;
                if (data.dateText) { dateText.textContent = data.dateText; }
                container.innerHTML = data.slots.map(function (s, i) {
                    var full = s.available <= 0;
                    var checked = !full && s.label === selected ? ' checked' : '';
                    return '<div class="col-6 col-md-4 slot-option">'
                        + '<input type="radio" name="timeSlot" id="slot' + i + '" value="' + escapeHtml(s.label) + '"' + (full ? ' disabled' : '') + checked + '>'
                        + '<label for="slot' + i + '">' + escapeHtml(s.label)
                        + '<small>' + (full ? 'Fully booked' : s.available + ' of ' + s.capacity + ' left') + '</small></label></div>';
                }).join('');
            })
            .catch(function () { message.textContent = 'Could not load slot availability. Please try again.'; });
    });
})();
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
