package domain;

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;

public class Responsavel {
    @Id
    @NotNull
    @JoinColumn(name = "id")
    private Long id;

    private String descricao;
}
