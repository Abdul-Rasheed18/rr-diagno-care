package com.rrdiagnocare.model;

import java.io.Serializable;

/** Form backing object for the checkout page. */
public class CheckoutForm implements Serializable {
    private static final long serialVersionUID = 1L;

    private String appointmentDate;   // yyyy-MM-dd from <input type="date">
    private String timeSlot;
    private String visitMode = "WALK_IN";
    private String collectionAddress;
    private String contactPhone;
    private String paymentMode = "PAY_AT_CENTER";

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getVisitMode() { return visitMode; }
    public void setVisitMode(String visitMode) { this.visitMode = visitMode; }
    public String getCollectionAddress() { return collectionAddress; }
    public void setCollectionAddress(String collectionAddress) { this.collectionAddress = collectionAddress; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
}
