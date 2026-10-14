package br.com.donation.repository;

import br.com.donation.model.Instituicao;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de Instituicao.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface InstituicaoRepository {

    Instituicao save(Instituicao instituicao);

    Optional<Instituicao> findById(Integer usuarioId);

    List<Instituicao> findAll();

    void deleteById(Integer usuarioId);

}
