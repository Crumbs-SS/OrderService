package com.crumbs.orderservice.dto;

import com.crumbs.lib.entity.MenuItem;
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
