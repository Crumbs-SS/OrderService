package com.crumbs.orderservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable {
    @Id
    @Column(name = "user_details_id", unique = true, nullable = false)
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_details_id")
    @JsonIgnoreProperties("customer")
    @ToString.Exclude
    private UserDetails userDetails;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orders;
}

