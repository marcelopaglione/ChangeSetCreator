package domain;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

public class Exercicio {
    @Id
    @NotNull
    private Long id;

    @Column(length = 25)
    private String descricao;
}
