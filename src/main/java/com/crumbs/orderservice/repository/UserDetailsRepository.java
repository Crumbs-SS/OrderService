package com.crumbs.orderservice.repository;

import com.crumbs.orderservice.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer> {
}
