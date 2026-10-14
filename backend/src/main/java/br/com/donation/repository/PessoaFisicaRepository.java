package br.com.donation.repository;

import br.com.donation.model.PessoaFisica;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de PessoaFisica.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface PessoaFisicaRepository {

    PessoaFisica save(PessoaFisica pessoaFisica);

    Optional<PessoaFisica> findById(Integer usuarioId);

    List<PessoaFisica> findAll();

    void deleteById(Integer usuarioId);

}
