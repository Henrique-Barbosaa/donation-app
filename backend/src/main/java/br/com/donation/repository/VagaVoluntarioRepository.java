package br.com.donation.repository;

import br.com.donation.model.VagaVoluntario;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de VagaVoluntario.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface VagaVoluntarioRepository {

    VagaVoluntario save(VagaVoluntario vaga);

    Optional<VagaVoluntario> findById(Integer campanhaDoacaoId, Integer codigoVaga);

    List<VagaVoluntario> findAll();

    void deleteById(Integer campanhaDoacaoId, Integer codigoVaga);

    List<VagaVoluntario> findByCampanhaId(Integer campanhaId);

    Integer findMaxCodigoVagaByCampanha(Integer campanhaId);

}
