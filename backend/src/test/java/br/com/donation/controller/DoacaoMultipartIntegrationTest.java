package br.com.donation.controller;

import br.com.donation.model.Instituicao;
import br.com.donation.model.Usuario;
import br.com.donation.repository.InstituicaoRepository;
import br.com.donation.repository.UsuarioRepository;
import br.com.donation.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:donation_multipart_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.sql.init.mode=always",
        "spring.sql.init.platform=postgresql"
})
class DoacaoMultipartIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void criarDoacaoViaMultipartRetorna201ComItemENovo() throws Exception {
        Usuario u = new Usuario();
        u.setNome("Doador Teste");
        u.setEmail("doador.multipart@solidar.io");
        u.setSenha("hash");
        u = usuarioRepository.save(u);

        String token = jwtUtil.gerarToken(u.getId(), u.getEmail(), "PESSOA_FISICA");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        String json = """
                {
                  "itens": [
                    {
                      "nome": "casaco",
                      "quantidade": 5,
                      "eNovo": "N",
                      "categoria": "ROUPAS"
                    }
                  ]
                }
                """;
        MockMultipartFile part = new MockMultipartFile(
                "doacao",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/doacoes")
                        .file(part)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itens[0].nome").value("casaco"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(5))
                .andExpect(jsonPath("$.itens[0].eNovo").value("N"));
    }

    @Test
    void campanhaComMetaFinanceiraIniciaEmZeroEAtualizaComContribuicaoPix() throws Exception {
        Usuario ongUser = new Usuario();
        ongUser.setNome("ONG Meta Real");
        ongUser.setEmail("ong.meta@solidar.io");
        ongUser.setSenha("hash");
        ongUser = usuarioRepository.save(ongUser);

        Instituicao inst = new Instituicao();
        inst.setUsuarioId(ongUser.getId());
        inst.setCnpj("99887766000155");
        instituicaoRepository.save(inst);

        Usuario doador = new Usuario();
        doador.setNome("Doador PIX");
        doador.setEmail("doador.pix@solidar.io");
        doador.setSenha("hash");
        doador = usuarioRepository.save(doador);

        String tokenOng = jwtUtil.gerarToken(ongUser.getId(), ongUser.getEmail(), "INSTITUICAO");
        String tokenDoador = jwtUtil.gerarToken(doador.getId(), doador.getEmail(), "PESSOA_FISICA");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        String campanhaJson = """
                {
                  "titulo": "Reforma da biblioteca",
                  "descricao": "Meta exclusivamente financeira",
                  "metaEmCentavos": 50000,
                  "locais": [
                    {
                      "nome": "Sede da ONG",
                      "logradouro": "Av. Salgado Filho",
                      "bairro": "Lagoa Nova",
                      "numero": "1200",
                      "cep": "59075000"
                    }
                  ]
                }
                """;
        MockMultipartFile campanhaPart = new MockMultipartFile(
                "campanha",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                campanhaJson.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult created = mockMvc.perform(multipart("/api/campanhas")
                        .file(campanhaPart)
                        .header("Authorization", "Bearer " + tokenOng))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metaEmCentavos").value(50000))
                .andExpect(jsonPath("$.arrecadadoEmCentavos").value(0))
                .andExpect(jsonPath("$.locais[0].nome").value("Sede da ONG"))
                .andExpect(jsonPath("$.locais[0].bairro").value("Lagoa Nova"))
                .andReturn();

        JsonNode body = new ObjectMapper().readTree(created.getResponse().getContentAsString());
        int campanhaId = body.get("id").asInt();

        String pixJson = String.format("""
                {
                  "donatarioId": %d,
                  "campanhaId": %d,
                  "valorEmCentavos": 10000,
                  "itens": [
                    {
                      "nome": "Contribuição PIX R$ 100,00",
                      "quantidade": 1,
                      "categoria": "OUTROS",
                      "eNovo": "S"
                    }
                  ]
                }
                """, ongUser.getId(), campanhaId);

        MockMultipartFile doacaoPart = new MockMultipartFile(
                "doacao",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                pixJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/doacoes")
                        .file(doacaoPart)
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/campanhas/" + campanhaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaEmCentavos").value(50000))
                .andExpect(jsonPath("$.arrecadadoEmCentavos").value(10000));
    }
}
