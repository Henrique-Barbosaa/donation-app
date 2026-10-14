package br.com.donation.repository;

import br.com.donation.model.Doacao;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de Doacao.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface DoacaoRepository {

    Doacao save(Doacao doacao);

    Optional<Doacao> findById(Integer id);

    List<Doacao> findAll();

    void deleteById(Integer id);

    List<Doacao> findByDoadorId(Integer doadorId);

    List<Doacao> findByDonatarioId(Integer donatarioId);

    List<Doacao> findDisponiveis(int page, int size);

    List<Doacao> findDisponiveis(int page, int size, String categoria, String busca);

    int countDisponiveis();

    int countDisponiveis(String categoria, String busca);

}
