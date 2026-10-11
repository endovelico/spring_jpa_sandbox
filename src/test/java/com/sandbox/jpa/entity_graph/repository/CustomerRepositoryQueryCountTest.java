package com.sandbox.jpa.entity_graph.repository;

import com.sandbox.jpa.entity_graph.domain.*;
import jakarta.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CustomerRepositoryQueryCountTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    private Statistics statistics;

    private String email = "john@example.com";

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = entityManager
                .getEntityManagerFactory()
                .unwrap(SessionFactory.class);

        statistics = sessionFactory.getStatistics();

        createTestData();

        // Exclude fixture setup SQL from our measurements.
        entityManager.flush();
        entityManager.clear();
        statistics.clear();
    }

    private void createTestData() {
        Customer customer = new Customer(
                "John Doe",
                email
        );

        Category category = new Category("Electronics");

        Product laptop = new Product(
                "Laptop",
                new BigDecimal("1200.00"),
                category
        );

        Product mouse = new Product(
                "Mouse",
                new BigDecimal("25.00"),
                category
        );

        PurchaseOrder firstOrder =
                new PurchaseOrder("COMPLETED");

        firstOrder.addItem(new OrderItem(
                laptop, 1, new BigDecimal("1200.00")
        ));

        firstOrder.addItem(new OrderItem(
                mouse, 2, new BigDecimal("25.00")
        ));

        PurchaseOrder secondOrder =
                new PurchaseOrder("PROCESSING");

        secondOrder.addItem(new OrderItem(
                mouse, 1, new BigDecimal("25.00")
        ));

        customer.addOrder(firstOrder);
        customer.addOrder(secondOrder);

        entityManager.persist(category);
        entityManager.persist(laptop);
        entityManager.persist(mouse);
        entityManager.persist(customer);
    }

    private long queryCount() {
        return statistics.getPrepareStatementCount();
    }

    // Tests go here...
    @Test
    void defaultFetchShouldExecuteAdditionalQueriesWhenTraversingGraph() {
        Customer customer = customerRepository
                .findByEmail(email)
                .orElseThrow();

        // Count statements executed by the repository lookup.
        long queriesAfterLoad = queryCount();

        // Traverse lazy relationships.
        customer.getOrders().forEach(order ->
                order.getItems().forEach(item -> {
                    item.getProduct().getName();
                    item.getProduct().getCategory().getName();
                })
        );

        long queriesAfterTraversal = queryCount();

        assertThat(queriesAfterTraversal)
                .as("Traversing lazy relationships should execute additional SQL")
                .isGreaterThan(queriesAfterLoad);
    }

    @Test
    void ordersEntityGraphShouldAvoidAdditionalQueriesForOrders() {
        Customer customer = customerRepository
                .findWithOrdersByEmail(email)
                .orElseThrow();

        long queriesAfterLoad = queryCount();

        // The orders association should already be fetched.
        int orderCount = customer.getOrders().size();

        long queriesAfterTraversal = queryCount();

        assertThat(orderCount).isEqualTo(2);

        assertThat(queriesAfterTraversal)
                .as("Accessing fetched orders should not execute additional SQL")
                .isEqualTo(queriesAfterLoad);
    }

    @Test
    void fullEntityGraphShouldAvoidAdditionalQueriesWhenTraversingGraph() {
        Customer customer = customerRepository
                .findWithFullGraphByEmail(email)
                .orElseThrow();

        long queriesAfterLoad = queryCount();

        customer.getOrders().forEach(order ->
                order.getItems().forEach(item -> {
                    item.getProduct().getName();
                    item.getProduct().getCategory().getName();
                })
        );

        long queriesAfterTraversal = queryCount();

        assertThat(customer.getOrders()).hasSize(2);

        assertThat(queriesAfterTraversal)
                .as("Traversing the complete entity graph should not execute additional SQL")
                .isEqualTo(queriesAfterLoad);
    }
}
