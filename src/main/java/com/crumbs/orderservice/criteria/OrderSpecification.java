package com.crumbs.orderservice.criteria;

import com.crumbs.lib.entity.Order;
import org.springframework.data.jpa.domain.Specification;


public class OrderSpecification  {
    private static final String userDetails = "userDetails";

    private OrderSpecification(){
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Order> getOrdersByCustomerFirstName(String name){
        return (root, query, builder) -> builder.like(root.join("customer")
                .join(userDetails).get("firstName"), "%" + name + "%");
    }

    public static Specification<Order> getOrdersByCustomerLastName(String name){
        return (root, query, builder) -> builder.like(root.join("customer")
                .join(userDetails).get("lastName"), "%"+name+"%");
    }

    public static Specification<Order> getOrdersByAddress(String street){
        return (root, query, builder) -> builder.like(root.join("deliveryLocation")
                .get("street"), "%"+street+"%");
    }

    public static Specification<Order> getOrdersByRestaurant(String name){
        return (root, query, builder) -> builder.like(root.join("restaurant")
                .get("name"), "%"+name+"%");
    }

    public static Specification<Order> getOrdersByDriverFirstName(String name){
        return (root, query, builder) -> builder.like(root.join("driver")
                .join(userDetails).get("firstName"), "%"+name+"%");
    }

    public static Specification<Order> getOrdersByDriverLastName(String name){
        return (root, query, builder) -> builder.like(root.join("driver")
                .join(userDetails).get("lastName"), "%"+name+"%");
    }

    public static Specification<Order> getOrdersByStatus(String status){
        return (root, query, builder) -> builder.like(root.join("orderStatus")
                .get("status"), "%"+status+"%");
    }

    public static Specification<Order> getOrdersBySearch(String query, String filterBy){
        return (getOrdersByCustomerFirstName(query)
                .or(getOrdersByCustomerLastName(query))
                .or(getOrdersByAddress(query))
                .or(getOrdersByRestaurant(query))
                .and(getOrdersByStatus(filterBy)));
    }


}
