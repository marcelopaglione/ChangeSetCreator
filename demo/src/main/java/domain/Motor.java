package domain;


import com.sun.istack.internal.NotNull;
import org.hibernate.annotations.Entity;
import org.hibernate.annotations.Table;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import java.util.List;

@Entity
public class Motor {

    @Id
    private Long id;

    @Column(name="ADDRESS_ID")
    private String descricao;

    @NotNull
    private String codigo;

    private List<String> pecas;

    private boolean funcionando;

    private int peso;

}
