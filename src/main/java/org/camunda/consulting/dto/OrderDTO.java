package org.camunda.consulting.dto;

import org.camunda.consulting.enumeration.CustomerType;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class OrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private CustomerType customerType;
    private Double total;
    private List<ItemDTO> items;

    public OrderDTO(CustomerType customerType, Double total, List<ItemDTO> items) {
        this.customerType = customerType;
        this.total = total;
        this.items = items;
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
}
