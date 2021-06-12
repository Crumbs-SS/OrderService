package com.crumbs.orderservice.DTO;

import com.crumbs.orderservice.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {

    @NotNull
    private Long id;

    private Restaurant restaurant;

    @NotNull
    private String name;

    @NotNull
    private Float price;

    @NotNull
    private String description;

    @NotNull
    private String preferences;
}
