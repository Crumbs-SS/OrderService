package com.crumbs.orderservice.repository;

import com.crumbs.orderservice.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findAllByRestaurantId(Long restaurantId, Pageable pageable);
}
