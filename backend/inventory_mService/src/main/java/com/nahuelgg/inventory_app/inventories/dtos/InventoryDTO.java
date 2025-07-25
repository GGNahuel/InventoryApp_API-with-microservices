package com.nahuelgg.inventory_app.inventories.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class InventoryDTO {
  private String id;
  private String name;
  private String accountId;
  private List<String> usersIds;
  private List<ProductInInvDTO> products;
}
