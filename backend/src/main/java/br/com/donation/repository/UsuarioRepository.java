package br.com.donation.repository;

import br.com.donation.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de Usuario.
 * <p>
 * Independe de tecnologia/banco de dados: a implementação concreta (hoje JDBC) fica
 * em {@code br.com.donation.repository.jdbc} e pode ser trocada sem afetar os services.
 */
public interface UsuarioRepository {

    Usuario save(Usuario usuario);

    void updatePerfil(Integer id, String nome, String logradouro, String bairro, String numero, String cep);

    void updateSenha(Integer id, String novaSenha);

    Optional<Usuario> findById(Integer id);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findAll();

    void deleteById(Integer id);

}
