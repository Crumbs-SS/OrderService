package com.crumbs.orderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistanceMatrixDTO {
    private String origin;
    private String destination;
}
