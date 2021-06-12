package com.crumbs.orderservice.repository;

import com.crumbs.orderservice.entity.RestaurantCategory;
import com.crumbs.orderservice.entity.RestaurantCategoryID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface
RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, RestaurantCategoryID> {

    @Transactional
    @Modifying
    @Query("DELETE FROM restaurant_category WHERE restaurant_id= ?1")
    void deleteByRestaurantID(Long id);
}
