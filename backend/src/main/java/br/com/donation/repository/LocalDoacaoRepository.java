package br.com.donation.repository;

import br.com.donation.model.LocalDoacao;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de LocalDoacao.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface LocalDoacaoRepository {

    LocalDoacao save(LocalDoacao localDoacao);

    Optional<LocalDoacao> findById(Integer id);

    List<LocalDoacao> findAll();

    void deleteById(Integer id);

}
