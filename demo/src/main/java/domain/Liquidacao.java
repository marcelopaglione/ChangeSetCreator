package domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "liquidacao")
@EqualsAndHashCode(of = "id", callSuper = false)
public class Liquidacao {


    @Id
    @NotNull
    private Long id;


    @Column(name = "numero_liquidacao")
    private Long numeroLiquidacao;


    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_exercicio")
    private Exercicio exercicio;


    @NotNull
    @ManyToOne
    @JoinColumn(name = "Id_Empenho")
    private Empenho empenho;


    @Column(name = "data_liquidacao")
    private LocalDate dataLiquidacao;


    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;


    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_responsavel")
    private Responsavel responsavel;


    @NotNull
    @JoinColumn(name = "valor")
    private BigDecimal valor;


    @ManyToOne
    @JoinColumn(name = "id_historico_padrao")
    private HistoricoPadrao historicoPadrao;


    @NotNull
    @Convert(converter = BooleanToStringConverter.class)
    @JoinColumn(name = "contabilizado")
    private Boolean contabilizado = Boolean.FALSE;


    @Column(name = "complemento_historico")
    private String complementoHistorico;


    @Column(name = "complemento_historico2", length = 25)
    private String complementoHistorico2;

}
