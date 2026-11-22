package com.rrdiagnocare.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** A purchased line (single test or a package) of a booking - used on receipts. */
public class BookingItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bookingId;
    private String itemType;
    private Long itemId;
    private String itemName;
    private BigDecimal price;

    public BookingItem() { }

    public BookingItem(String itemType, Long itemId, String itemName, BigDecimal price) {
        this.itemType = itemType;
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
