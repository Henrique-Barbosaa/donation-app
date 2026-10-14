package br.com.donation.repository;

import br.com.donation.model.CampanhaLocalDoacao;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de CampanhaLocalDoacao.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface CampanhaLocalDoacaoRepository {

    CampanhaLocalDoacao save(CampanhaLocalDoacao assoc);

    Optional<CampanhaLocalDoacao> findById(Integer campanhaDoacaoId, Integer localDoacaoId);

    List<CampanhaLocalDoacao> findAll();

    void deleteById(Integer campanhaDoacaoId, Integer localDoacaoId);

    List<CampanhaLocalDoacao> findByCampanhaId(Integer campanhaId);

}
