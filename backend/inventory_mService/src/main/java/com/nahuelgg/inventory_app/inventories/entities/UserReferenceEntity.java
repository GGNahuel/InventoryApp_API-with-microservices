package com.nahuelgg.inventory_app.inventories.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "user_reference")
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserReferenceEntity {
  @Id @GeneratedValue
  private UUID id;
  @Column(nullable = false)
  private UUID referenceId;
}
