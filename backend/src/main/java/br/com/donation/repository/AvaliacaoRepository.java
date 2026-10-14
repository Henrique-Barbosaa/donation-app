package br.com.donation.repository;

import br.com.donation.model.Avaliacao;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de Avaliacao.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface AvaliacaoRepository {

    Avaliacao save(Avaliacao avaliacao);

    Optional<Avaliacao> findById(Integer id);

    List<Avaliacao> findAll();

    void deleteById(Integer id);

    List<Avaliacao> findByDoacaoId(Integer doacaoId);

}
