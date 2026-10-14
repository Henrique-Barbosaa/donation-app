package br.com.donation.repository;

import br.com.donation.model.Item;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de Item.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface ItemRepository {

    Item save(Item item);

    Optional<Item> findById(String nome, Integer doacaoId);

    List<Item> findAll();

    List<Item> findByDoacaoId(Integer doacaoId);

    void deleteById(String nome, Integer doacaoId);

}
