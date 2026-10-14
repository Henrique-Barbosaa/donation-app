package br.com.donation.repository;

import br.com.donation.model.Interesse;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de Interesse.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface InteresseRepository {

    Interesse save(Interesse interesse);

    Optional<Interesse> findById(Integer usuarioId, Integer doacaoId);

    List<Interesse> findAll();

    void deleteById(Integer usuarioId, Integer doacaoId);

    List<Interesse> findByDoacaoId(Integer doacaoId);

}
