package com.sandbox.jpa.entity_graph.repository;

import com.sandbox.jpa.entity_graph.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Default fetch behavior
    Optional<Customer> findByEmail(String email);

    // Load the complete nested graph
    @EntityGraph(
            value = "Customer.orders.items.product.category",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Optional<Customer> findWithFullGraphByEmail(String email);

    // A smaller graph, defined dynamically
    @EntityGraph(attributePaths = {"orders"})
    Optional<Customer> findWithOrdersByEmail(String email);
}
