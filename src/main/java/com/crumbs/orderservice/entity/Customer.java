package com.crumbs.orderservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonBackReference
    @ToString.Exclude
    private UserDetails userDetails;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CartItem> cartItems;
}

