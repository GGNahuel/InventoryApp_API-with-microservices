package com.nahuelgg.inventory_app.products.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class ResponseDTO<T> {
  private Integer status;
  private ErrorDTO error;
  private T data;
}
