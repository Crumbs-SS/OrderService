package com.crumbs.orderservice.repository;

import com.crumbs.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findOrderByFulfilledAndCustomer(Boolean fulfilled, Customer customer);
}
