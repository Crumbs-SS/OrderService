package com.crumbs.orderservice.DTO;

import com.crumbs.orderservice.entity.MenuItem;
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

    private MenuItem menuItem;

    @NotNull
    private String preferences;
}
