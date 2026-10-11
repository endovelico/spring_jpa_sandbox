package com.sandbox.jpa.entity_graph.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedEntityGraph(
        name = "Customer.orders.items.product.category",
        attributeNodes = @NamedAttributeNode(
                value = "orders",
                subgraph = "orders"
        ),
        subgraphs = {
                @NamedSubgraph(
                        name = "orders",
                        attributeNodes = @NamedAttributeNode(
                                value = "items",
                                subgraph = "items"
                        )
                ),
                @NamedSubgraph(
                        name = "items",
                        attributeNodes = @NamedAttributeNode(
                                value = "product",
                                subgraph = "product"
                        )
                ),
                @NamedSubgraph(
                        name = "product",
                        attributeNodes = @NamedAttributeNode("category")
                )
        }
)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseOrder> orders = new ArrayList<>();

    protected Customer() {}

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<PurchaseOrder> getOrders() {
        return orders;
    }

    public void addOrder(PurchaseOrder order) {
        orders.add(order);
        order.setCustomer(this);
    }
}
