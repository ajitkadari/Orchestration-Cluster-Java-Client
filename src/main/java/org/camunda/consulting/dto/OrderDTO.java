package org.camunda.consulting.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.camunda.consulting.enumeration.CustomerType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("customerType")
    private CustomerType customerType;

    @JsonProperty("total")
    private Double total;

    @JsonProperty("items")
    private List<ItemDTO> items;

    @JsonProperty("discount")
    private Double discount;

    @JsonProperty("couponCode")
    private String couponCode;

    public OrderDTO() {
    }

    public OrderDTO(CustomerType customerType, Double total, List<ItemDTO> items, Double discount, String couponCode) {
        this.customerType = customerType;
        this.total = total;
        this.items = items;
        this.discount = discount;
        this.couponCode = couponCode;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
