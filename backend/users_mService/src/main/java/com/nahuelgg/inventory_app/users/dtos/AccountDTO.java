package com.nahuelgg.inventory_app.users.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class AccountDTO {
  private String id;
  private String username;
  private List<String> idsOfInventoryReferred;
  private List<UserDTO> users;
}
