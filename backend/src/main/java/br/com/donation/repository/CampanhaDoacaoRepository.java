package br.com.donation.repository;

import br.com.donation.model.CampanhaDoacao;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de CampanhaDoacao.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface CampanhaDoacaoRepository {

    CampanhaDoacao save(CampanhaDoacao campanha);

    Optional<CampanhaDoacao> findById(Integer id);

    List<CampanhaDoacao> findAll(int page, int size);

    int countAll();

    void deleteById(Integer id);

    List<CampanhaDoacao> findByInstituicaoId(Integer instituicaoId);

}
