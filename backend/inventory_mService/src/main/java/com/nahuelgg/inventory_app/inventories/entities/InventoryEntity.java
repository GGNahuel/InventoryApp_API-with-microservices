package com.nahuelgg.inventory_app.inventories.entities;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class InventoryEntity {
  @Id @GeneratedValue
  private UUID id;
  @Column(nullable = false)
  private String name;
  @Column(nullable = false)
  private UUID accountId;

  @ManyToMany @JoinTable(
    name = "product_categories", 
    joinColumns = @JoinColumn(referencedColumnName = "id"), // id del inventario
    inverseJoinColumns = @JoinColumn(referencedColumnName = "id") // id de la ref al usuario
  )
  private List<UserReferenceEntity> users;
  @OneToMany(mappedBy = "inventory", cascade = CascadeType.REMOVE)
  private List<ProductInInvEntity> products;
}
