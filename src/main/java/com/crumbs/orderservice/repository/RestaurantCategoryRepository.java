package com.crumbs.orderservice.repository;

import com.crumbs.orderservice.entity.RestaurantCategory;
import com.crumbs.orderservice.entity.RestaurantCategoryID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, RestaurantCategoryID> {

}
