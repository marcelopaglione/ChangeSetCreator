package clazzTest;

import javax.persistence.Column;
import javax.persistence.Id;

public class CreateNewChangeSet {

    @Id
    Long id;

    @Column(name = "full_name")
    String fullName;

}
