package com.crumbs.orderservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    @NotNull
    @NotEmpty
    private String phone;

    @NotNull
    @NotEmpty
    private String address;

    private String preferences;
    private List<CartItemDTO> cartItems;
}
