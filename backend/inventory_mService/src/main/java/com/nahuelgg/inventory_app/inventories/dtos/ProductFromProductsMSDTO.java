package com.nahuelgg.inventory_app.inventories.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductFromProductsMSDTO {
  private String id;
  private String name;
  private String brand;
  private String model;
  private String description;
  private Integer unitPrice;
  private List<String> categories;
  private String accountId;
}
