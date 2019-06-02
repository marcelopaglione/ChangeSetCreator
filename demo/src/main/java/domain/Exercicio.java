package domain;

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;

public class Exercicio {
    @Id
    @NotNull
    private Long id;

    private String descricao;
}
