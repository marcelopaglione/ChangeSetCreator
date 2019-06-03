package clazzTest;

import javax.persistence.Column;
import javax.persistence.Id;

public class MaxLength {

    @Id
    private Long id;

    private String maxLengthNotSet;

    @Column(name = "max_length_default")
    private String maxLengthDefault;

    @Column(name = "user_defined_max_length", length = 50)
    private String userDefinedMaxLength;
}
