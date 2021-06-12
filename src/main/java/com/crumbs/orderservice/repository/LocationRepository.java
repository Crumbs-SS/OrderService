package com.crumbs.orderservice.repository;

import com.crumbs.orderservice.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("select street from location where street = ?1")
    String findLocationByStreet(String street);
}
