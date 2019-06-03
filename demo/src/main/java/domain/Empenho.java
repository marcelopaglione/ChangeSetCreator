package domain;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;

public class Empenho {
    @Id
    @NotNull
    @JoinColumn(name = "id")
    private Long idEmp;

    @Column(name = "descricao", length = 250)
    private String descricao;
}
