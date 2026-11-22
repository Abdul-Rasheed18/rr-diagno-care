package com.rrdiagnocare.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** An individual lab test with its price, preparation notes and normal reference range. */
public class DiagnosticTest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;
    private String category;
    private BigDecimal price;
    private String sampleType;
    private String preparationNotes;
    private String unit;
    private BigDecimal normalMin;
    private BigDecimal normalMax;
    private int turnaroundHours = 24;
    private String description;
    private boolean active = true;

    /** e.g. "13 - 17 g/dL". */
    public String getReferenceRange() {
        if (normalMin == null && normalMax == null) {
            return "-";
        }
        String u = unit == null ? "" : " " + unit;
        if (normalMin == null) return "< " + plain(normalMax) + u;
        if (normalMax == null) return "> " + plain(normalMin) + u;
        return plain(normalMin) + " - " + plain(normalMax) + u;
    }

    public boolean isFastingRequired() {
        if (preparationNotes == null) return false;
        String p = preparationNotes.toLowerCase();
        return p.contains("fasting") && !p.startsWith("no fasting");
    }

    static String plain(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getSampleType() { return sampleType; }
    public void setSampleType(String sampleType) { this.sampleType = sampleType; }
    public String getPreparationNotes() { return preparationNotes; }
    public void setPreparationNotes(String preparationNotes) { this.preparationNotes = preparationNotes; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getNormalMin() { return normalMin; }
    public void setNormalMin(BigDecimal normalMin) { this.normalMin = normalMin; }
    public BigDecimal getNormalMax() { return normalMax; }
    public void setNormalMax(BigDecimal normalMax) { this.normalMax = normalMax; }
    public int getTurnaroundHours() { return turnaroundHours; }
    public void setTurnaroundHours(int turnaroundHours) { this.turnaroundHours = turnaroundHours; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
