package com.rrdiagnocare.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** A test or package held in the patient's HttpSession cart before checkout. */
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String itemType;   // TEST | PACKAGE
    private Long itemId;
    private String name;
    private String category;
    private BigDecimal price;
    private String preparationNotes;
    private List<Long> testIds = new ArrayList<>();

    public CartItem() { }

    public CartItem(String itemType, Long itemId, String name, String category, BigDecimal price,
                    String preparationNotes, List<Long> testIds) {
        this.itemType = itemType;
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.preparationNotes = preparationNotes;
        this.testIds = testIds == null ? new ArrayList<>() : new ArrayList<>(testIds);
    }

    public String getKey() { return itemType + "-" + itemId; }
    public boolean isPackage() { return "PACKAGE".equals(itemType); }
    public int getTestCount() { return testIds.size(); }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getPreparationNotes() { return preparationNotes; }
    public void setPreparationNotes(String preparationNotes) { this.preparationNotes = preparationNotes; }
    public List<Long> getTestIds() { return testIds; }
    public void setTestIds(List<Long> testIds) { this.testIds = testIds; }
}
