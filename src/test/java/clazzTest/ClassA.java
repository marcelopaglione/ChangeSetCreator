package clazzTest;

import javax.persistence.Id;

public class ClassA {

    @Id
    private Long id;

    private ClassB classB;
}
