package br.com.donation.repository;

import br.com.donation.model.Inscricao;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de Inscricao.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface InscricaoRepository {

    Inscricao save(Inscricao inscricao);

    Optional<Inscricao> findById(Integer pessoaFisicaUsuarioId, Integer campanhaId, Integer vagaVoluntarioCodigoVaga);

    List<Inscricao> findAll();

    void deleteById(Integer pessoaFisicaUsuarioId, Integer campanhaId, Integer vagaVoluntarioCodigoVaga);

    List<Inscricao> findByPessoaFisicaId(Integer pessoaFisicaId);

    List<Inscricao> findByCampanhaAndVaga(Integer campanhaId, Integer codigoVaga);

}
