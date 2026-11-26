package br.com.donation.dto;

import br.com.donation.dto.campanha.ItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemDtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializaEDesserializaCampoENovoComCamelCaseExato() throws Exception {
        ItemDTO original = ItemDTO.builder()
                .nome("Casaco de frio")
                .quantidade(2)
                .categoria("ROUPAS")
                .eNovo("S")
                .build();

        String json = objectMapper.writeValueAsString(original);
        assertTrue(json.contains("\"eNovo\":\"S\""), "JSON deveria conter campo \"eNovo\", mas retornou: " + json);

        ItemDTO lido = objectMapper.readValue(
                "{\"nome\":\"Casaco de frio\",\"quantidade\":2,\"categoria\":\"ROUPAS\",\"eNovo\":\"S\"}",
                ItemDTO.class
        );
        assertEquals("S", lido.getENovo());
    }
}
