package br.com.donation.dto.doacao;
import br.com.donation.dto.campanha.ItemDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriarDoacaoDTO {

    @NotEmpty(message = "A doação deve ter pelo menos um item")
    @Valid
    private List<ItemDTO> itens;

    /**
     * Opcional: quando a doação é destinada diretamente a uma campanha, identifica
     * o usuário/instituição criador da campanha para constar nas doações recebidas.
     */
    private Integer donatarioId;

    /** Opcional: ID da campanha para atualizar o valor arrecadado em doações financeiras. */
    private Integer campanhaId;

    /** Opcional: valor em centavos de uma contribuição financeira (PIX) para a campanha. */
    private Long valorEmCentavos;
}


