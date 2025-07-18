package com.nahuelgg.inventory_app.users.services.implementations;

import static com.nahuelgg.inventory_app.users.utilities.Validations.checkFieldsHasContent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nahuelgg.inventory_app.users.dtos.AccountDTO;
import com.nahuelgg.inventory_app.users.dtos.PermissionsForInventoryDTO;
import com.nahuelgg.inventory_app.users.dtos.UserDTO;
import com.nahuelgg.inventory_app.users.entities.AccountEntity;
import com.nahuelgg.inventory_app.users.entities.InventoryRefEntity;
import com.nahuelgg.inventory_app.users.entities.PermissionsForInventoryEntity;
import com.nahuelgg.inventory_app.users.entities.UserEntity;
import com.nahuelgg.inventory_app.users.exceptions.InvalidValueException;
import com.nahuelgg.inventory_app.users.exceptions.ResourceNotFoundException;
import com.nahuelgg.inventory_app.users.repositories.AccountRepository;
import com.nahuelgg.inventory_app.users.repositories.InventoryRefRepository;
import com.nahuelgg.inventory_app.users.repositories.PermissionsForInventoryRepository;
import com.nahuelgg.inventory_app.users.repositories.UserRepository;
import com.nahuelgg.inventory_app.users.services.AccountService;
import com.nahuelgg.inventory_app.users.utilities.DTOMappers;
import com.nahuelgg.inventory_app.users.utilities.EntityMappers;
import com.nahuelgg.inventory_app.users.utilities.Validations.Field;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService_Impl implements AccountService {
  private final AccountRepository repository;
  private final UserRepository userRepository;
  private final InventoryRefRepository inventoryRefRepository;
  private final PermissionsForInventoryRepository permsRepository;
  private final DTOMappers dtoMappers;
  private final EntityMappers entityMappers = new EntityMappers();
  private final RestTemplate restTemplate;
  private final BCryptPasswordEncoder encoder;
  private final HttpGraphQlClient client;

  @Override @Transactional(readOnly = true)
  public List<AccountDTO> getAll() {
    return repository.findAll().stream().map(
      acc -> entityMappers.mapAccount(acc)
    ).toList();
  }

  @Override @Transactional(readOnly = true)
  public AccountDTO getById(UUID id) {
    checkFieldsHasContent(new Field("id de cuenta", id));

    return entityMappers.mapAccount(repository.findById(id).orElseThrow(
      () -> new ResourceNotFoundException("cuenta", "id", id.toString())
    ));
  }

  @Override @Transactional
  public AccountDTO create(String username, String password, String passwordRepeated, String adminPassword, String adminPasswordRepeated) {
    checkFieldsHasContent(
      new Field("nombre de usuario", username), new Field("contraseña", password), new Field("contraseña de admin", adminPassword),
      new Field("repetición de contraseña", passwordRepeated), new Field("repetición de contraseña de admin", adminPasswordRepeated)
    );
    if (!password.equals(passwordRepeated) || !adminPassword.equals(adminPasswordRepeated))
      throw new InvalidValueException("Las contraseñas no coinciden con sus respectivas repeticiones");

    UserEntity adminUser = userRepository.save(UserEntity.builder()
      .name("admin")
      .password(encoder.encode(adminPassword))
      .role("admin")
      .isAdmin(true)
    .build());

    AccountEntity accountToCreate = AccountEntity.builder()
      .username(username)
      .password(encoder.encode(password))
      .users(List.of(adminUser))
    .build();

    AccountEntity accountSaved = repository.save(accountToCreate);

    adminUser.setAssociatedAccount(accountSaved);
    userRepository.save(adminUser);

    return entityMappers.mapAccount(accountSaved);
  }

  @Override @Transactional
  public UserDTO addUser(UserDTO user, UUID accountId, String passwordForNewUser, String passwordRepeated) {
    checkFieldsHasContent(new Field("usuario a agregar", user));
    checkFieldsHasContent(
      new Field("nombre del usuario", user.getName()), new Field("rol/puesto del usuario", user.getRole()),
      new Field("id de cuenta a asociar", accountId),
      new Field("contraseña para el usuario", passwordForNewUser), new Field("repetición de contraseña", passwordRepeated)
    );
    if (user.getInventoryPerms() != null && !user.getInventoryPerms().isEmpty()) {
      for (PermissionsForInventoryDTO perm : user.getInventoryPerms()) {
        checkFieldsHasContent(
          new Field("inventario de referemcia", perm.getIdOfInventoryReferenced()),
          new Field("permisos", perm.getPermissions())
        );
      }
    }

    if (!passwordForNewUser.equals(passwordRepeated))
      throw new InvalidValueException("Las contraseñas para el nuevo usuario no coinciden");

    if (userRepository.findByNameAndAssociatedAccountId(user.getName(), accountId).isPresent())
      throw new InvalidValueException("Ya existe un usuario con ese nombre asociado a esta cuenta");

    AccountEntity parentAccount = repository.findById(accountId).orElseThrow(
      () -> new ResourceNotFoundException("cuenta", "id", accountId.toString())
    );

    UserEntity newUser = dtoMappers.mapUser(user, parentAccount);

    List<PermissionsForInventoryEntity> permsEntities = new ArrayList<>();
    for (int i = 0; i < user.getInventoryPerms().size(); i++) {
      PermissionsForInventoryDTO permsDto = user.getInventoryPerms().get(i);

      permsEntities.add(permsRepository.save(dtoMappers.mapPerms(permsDto)));
    }

    newUser.setInventoryPerms(permsEntities);
    newUser.setIsAdmin(false);
    newUser.setPassword(encoder.encode(passwordForNewUser));
    UserEntity savedUser = userRepository.save(newUser);

    parentAccount.getUsers().add(savedUser);
    repository.save(parentAccount);
    return entityMappers.mapUser(savedUser);
  }

  @Override @Transactional
  public AccountDTO assignInventory(UUID accountId, UUID inventoryId) {
    checkFieldsHasContent(new Field("id de cuenta", accountId), new Field("id referenciada de inventario", inventoryId));

    AccountEntity account = repository.findById(accountId).orElseThrow(
      () -> new ResourceNotFoundException("cuenta", "id", accountId.toString())
    );

    List<InventoryRefEntity> inventoriesReferences = account.getInventoriesReferences() == null ? new ArrayList<>() : account.getInventoriesReferences();
    inventoriesReferences.add(
      inventoryRefRepository.save(InventoryRefEntity.builder().inventoryIdReference(inventoryId).build())
    );

    account.setInventoriesReferences(inventoriesReferences);
    return entityMappers.mapAccount(repository.save(account));
  }

  @Override @Transactional
  public void removeInventoryAssigned(UUID accountId, UUID inventoryId) {
    checkFieldsHasContent(new Field("id de cuenta", accountId), new Field("id referenciada de inventario", inventoryId));

    AccountEntity account = repository.findById(accountId).orElseThrow(
      () -> new ResourceNotFoundException("cuenta", "id", accountId.toString())
    );
    inventoryRefRepository.findByInventoryIdReference(inventoryId).orElseThrow(
      () -> new ResourceNotFoundException("entidad de referencia a inventario", "id de referencia", inventoryId.toString())
    );

    List<InventoryRefEntity> inventoriesReferences = account.getInventoriesReferences().stream().filter(
      invRefEntity -> invRefEntity.getInventoryIdReference() != inventoryId
    ).toList();
    List<PermissionsForInventoryEntity> permsAssociatedWithInventoryRef = permsRepository.findByReferencedInventoryId(inventoryId);
    for (int i = 0; i < permsAssociatedWithInventoryRef.size(); i++) {
      permsRepository.deleteById(permsAssociatedWithInventoryRef.get(i).getId());
    }

    account.setInventoriesReferences(inventoriesReferences);
    repository.save(account);
  }

  @Override @Transactional
  public void delete(UUID accountId) {
    checkFieldsHasContent(new Field("id de cuenta", accountId));

    boolean accountWithIdExists = repository.findById(accountId).isPresent();
    if (accountWithIdExists) {
      boolean inventoryRequestWasSuccess = client.document("""
        mutation {
          deleteByAccountId(
            id: "%s"
          )
        }
      """.formatted(accountId.toString())).retrieve("deleteByAccountId").toEntity(Boolean.class).block();

      if (!inventoryRequestWasSuccess) 
        throw new RuntimeException("El borrado de inventarios asociados no se ha podido realizar, operación cancelada");

      restTemplate.delete("http://api-products:8081/product/delete-by-account?id=" + accountId.toString());

      repository.deleteById(accountId);
    }
  }
}
