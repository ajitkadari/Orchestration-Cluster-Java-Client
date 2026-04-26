package org.camunda.consulting.dto;

import java.io.Serial;
import java.io.Serializable;

import org.camunda.consulting.enumeration.ItemCategory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("category")
    private ItemCategory category;
    
    @JsonProperty("quantity")
    private int quantity;

    public ItemDTO() {
    }

    public ItemDTO(ItemCategory category, int quantity) {
        this.category = category;
        this.quantity = quantity;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
