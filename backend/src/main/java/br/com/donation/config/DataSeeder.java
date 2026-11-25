package br.com.donation.config;

import br.com.donation.model.CampanhaDoacao;
import br.com.donation.model.Instituicao;
import br.com.donation.model.Usuario;
import br.com.donation.repository.CampanhaDoacaoRepository;
import br.com.donation.repository.InstituicaoRepository;
import br.com.donation.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Popula campanhas iniciais automaticamente quando o banco recém-criado ainda está vazio,
 * garantindo que a tela inicial do app já exiba dados reais vindos da API.
 */
@Component
@ConditionalOnProperty(name = "spring.sql.init.mode", havingValue = "always")
public class DataSeeder implements ApplicationRunner {

    private static final String EMAIL_INSTITUICAO_DEMO = "contato@maossolidarias.org.br";

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final CampanhaDoacaoRepository campanhaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            InstituicaoRepository instituicaoRepository,
            CampanhaDoacaoRepository campanhaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.campanhaRepository = campanhaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (campanhaRepository.countAll() > 0) {
            return;
        }

        Usuario instituicaoUsuario = usuarioRepository.findByEmail(EMAIL_INSTITUICAO_DEMO)
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setNome("Instituto Mãos Solidárias");
                    u.setEmail(EMAIL_INSTITUICAO_DEMO);
                    u.setSenha(passwordEncoder.encode("123456"));
                    u.setLogradouro("Rua das Flores");
                    u.setBairro("Rocas");
                    u.setNumero("120");
                    u.setCep("59010100");
                    return usuarioRepository.save(u);
                });

        if (instituicaoRepository.findById(instituicaoUsuario.getId()).isEmpty()) {
            Instituicao inst = new Instituicao();
            inst.setUsuarioId(instituicaoUsuario.getId());
            inst.setCnpj("12345678000199");
            inst.setSite("https://maossolidarias.org.br");
            instituicaoRepository.save(inst);
        }

        criarCampanha(
                instituicaoUsuario.getId(),
                "Cestas básicas para o bairro das Rocas",
                "Apoio alimentar mensal para 50 famílias em vulnerabilidade no bairro das Rocas. Sua doação garante dignidade e alimentação para crianças e idosos.",
                "Alimentos não perecíveis (50 un.), Materiais de higiene (30 un.), Produtos de limpeza (20 un.)",
                100
        );

        criarCampanha(
                instituicaoUsuario.getId(),
                "Agasalhos e cobertores para o inverno",
                "Arrecadação de casacos, roupas de frio e cobertores em bom estado para distribuição em abrigos comunitários de Natal.",
                "Casacos (20 un.), Cobertores (15 un.), Roupas infantis (25 un.)",
                60
        );

        criarCampanha(
                instituicaoUsuario.getId(),
                "Material escolar para 120 crianças",
                "Kits escolares completos com cadernos, mochilas e estojos para estudantes da rede pública no início do ano letivo.",
                "Cadernos (40 un.), Mochilas (20 un.), Estojos completos (30 un.), Livros (30 un.)",
                120
        );
    }

    private void criarCampanha(
            Integer instituicaoId,
            String titulo,
            String descricao,
            String itensFoco,
            int metaVoluntarios
    ) {
        CampanhaDoacao c = new CampanhaDoacao();
        c.setInstituicaoUsuarioId(instituicaoId);
        c.setTitulo(titulo);
        c.setDescricao(descricao);
        c.setDataInicio(LocalDate.now());
        c.setDataFim(LocalDate.now().plusMonths(2));
        c.setStatus("ATIVA");
        c.setItensFoco(itensFoco);
        c.setMetaVoluntarios(metaVoluntarios);
        campanhaRepository.save(c);
    }
}
