package domain;

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;

public class Empenho {
    @Id
    @NotNull
    @JoinColumn(name = "id")
    private Long idEmp;

    private String descricao;
}
