package br.com.donation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampanhaDoacao {
    private Integer id;
    private Integer instituicaoUsuarioId;
    
    private String titulo;
    private String descricao;
    private String urlImagemCapa;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;
    private String itensFoco;
    private Integer metaVoluntarios;
    
    private Instituicao instituicao;
}
