package com.rrdiagnocare.model;

import java.io.Serializable;

/** Appointment time slot with capacity and (for a given date) the current booked count. */
public class TimeSlot implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String label;
    private int sortOrder;
    private int capacity;
    private boolean active = true;
    private int booked;

    public int getAvailable() { return Math.max(0, capacity - booked); }
    public boolean isFull() { return getAvailable() == 0; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getBooked() { return booked; }
    public void setBooked(int booked) { this.booked = booked; }
}
