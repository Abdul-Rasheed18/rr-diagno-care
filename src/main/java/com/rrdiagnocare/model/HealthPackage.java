package com.rrdiagnocare.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** A discounted combo of several diagnostic tests. */
public class HealthPackage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String preparationNotes;
    private boolean featured;
    private boolean active = true;
    private List<DiagnosticTest> tests = new ArrayList<>();

    /** Sum of the individual test prices (the "MRP" before the package discount). */
    public BigDecimal getMrp() {
        return tests.stream().map(DiagnosticTest::getPrice).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSavings() {
        BigDecimal s = getMrp().subtract(price == null ? BigDecimal.ZERO : price);
        return s.signum() > 0 ? s : BigDecimal.ZERO;
    }

    public int getDiscountPercent() {
        BigDecimal mrp = getMrp();
        if (mrp.signum() == 0) return 0;
        return getSavings().multiply(BigDecimal.valueOf(100)).divide(mrp, 0, RoundingMode.HALF_UP).intValue();
    }

    public int getTestCount() { return tests.size(); }

    public Set<Long> getTestIds() {
        Set<Long> ids = new HashSet<>();
        for (DiagnosticTest t : tests) ids.add(t.getId());
        return ids;
    }

    public String getCategory() { return "Full Body Packages"; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getPreparationNotes() { return preparationNotes; }
    public void setPreparationNotes(String preparationNotes) { this.preparationNotes = preparationNotes; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<DiagnosticTest> getTests() { return tests; }
    public void setTests(List<DiagnosticTest> tests) { this.tests = tests; }
}
