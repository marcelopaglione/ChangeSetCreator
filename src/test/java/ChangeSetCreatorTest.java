import changeset.ChangeSetCreator;
import clazzTest.*;
import org.jdom2.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ChangeSetCreatorTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void createNewChangeSet() {
        ChangeSetCreator changeSetCreator = new ChangeSetCreator();
        changeSetCreator.create(CreateNewChangeSet.class, "abc");
        Element root = changeSetCreator.getChangeSet().getDatabaseChangeLog();

        assertThat(root, notNullValue());
        assertThat(root.getContent(), hasSize(2));
        assertThat(root.getName(), equalTo("databaseChangeLog"));

        Element databaseChangeLog = (Element) root.getContent().get(0);
        assertThat(databaseChangeLog.getContent(), hasSize(2));
        Element preConditions = (Element) databaseChangeLog.getContent().get(0);
        assertThat(preConditions.getAttributes(), hasSize(2));

        assertThat(preConditions.getAttributes().get(0).getName(), equalTo("onFail"));
        assertThat(preConditions.getAttributes().get(0).getValue(), equalTo("MARK_RAN"));
        assertThat(preConditions.getAttributes().get(1).getName(), equalTo("onFailMessage"));
        assertThat(preConditions.getAttributes().get(1).getValue(), equalTo("A tabela CREATE_NEW_CHANGE_SET já existe"));

        assertThat(preConditions.getContent(), hasSize(1));
        Element not = (Element) preConditions.getContent().get(0);
        assertThat(not.getName(), equalTo("not"));

        assertThat(not.getContent(), hasSize(1));
        Element tableExists = (Element) not.getContent().get(0);
        assertThat(tableExists.getAttributes(), hasSize(1));
        assertThat(tableExists.getAttributes().get(0).getName(), equalTo("tableName"));
        assertThat(tableExists.getAttributes().get(0).getValue(), equalTo("CREATE_NEW_CHANGE_SET"));


        Element createTable = (Element) databaseChangeLog.getContent().get(1);
        assertThat(createTable.getName(), equalTo("createTable"));
        assertThat(createTable.getAttributes(), hasSize(1));
        assertThat(createTable.getAttributes().get(0).getName(), equalTo("tableName"));
        assertThat(createTable.getAttributes().get(0).getValue(), equalTo("CREATE_NEW_CHANGE_SET"));

        assertThat(createTable.getContent(), hasSize(2));
        Element column1 = (Element) createTable.getContent().get(0);
        assertThat(column1.getName(), equalTo("column"));
        assertThat(column1.getAttributes(), hasSize(2));
        assertThat(column1.getAttributes().get(0).getName(), equalTo("name"));
        assertThat(column1.getAttributes().get(0).getValue(), equalTo("ID"));
        assertThat(column1.getAttributes().get(1).getName(), equalTo("type"));
        assertThat(column1.getAttributes().get(1).getValue(), equalTo("${numeric}"));

        Element column2 = (Element) createTable.getContent().get(1);
        assertThat(column2.getName(), equalTo("column"));
        assertThat(column2.getAttributes(), hasSize(2));
        assertThat(column2.getAttributes().get(0).getName(), equalTo("name"));
        assertThat(column2.getAttributes().get(0).getValue(), equalTo("FULL_NAME"));
        assertThat(column2.getAttributes().get(1).getName(), equalTo("type"));
        assertThat(column2.getAttributes().get(1).getValue(), equalTo("varchar(your_size_here)"));

        Element pkChangeSet = (Element) root.getContent().get(1);
        assertThat(pkChangeSet.getName(), equalTo("changeSet"));
        assertThat(pkChangeSet.getAttributes(), hasSize(2));
        assertThat(pkChangeSet.getAttributes().get(0).getName(), equalTo("id"));
        assertThat(pkChangeSet.getAttributes().get(0).getValue(), equalTo("PK_CREATE_NEW_CHANGE_SET"));
        assertThat(pkChangeSet.getAttributes().get(1).getName(), equalTo("author"));
        assertThat(pkChangeSet.getAttributes().get(1).getValue(), equalTo("abc"));


        Element preCondition = ((Element) pkChangeSet.getContent().get(0));
        assertThat(preCondition.getName(), equalTo("preConditions"));
        assertThat(preCondition.getAttributes(), hasSize(2));
        assertThat(preCondition.getAttributes().get(0).getName(), equalTo("onFail"));
        assertThat(preCondition.getAttributes().get(0).getValue(), equalTo("MARK_RAN"));
        assertThat(preCondition.getAttributes().get(1).getName(), equalTo("onFailMessage"));
        assertThat(preCondition.getAttributes().get(1).getValue(), equalTo("A PK_CREATE_NEW_CHANGE_SET já existe"));
        assertThat(preCondition.getContent(), hasSize(1));

        not = (Element) preCondition.getContent().get(0);
        assertThat(not.getName(), equalTo("not"));
        assertThat(not.getContent(), hasSize(1));

        Element primaryKeyExists = (Element) not.getContent().get(0);
        assertThat(primaryKeyExists.getName(), equalTo("primaryKeyExists"));

        assertThat(primaryKeyExists.getAttributes().size(), is(2));
        assertThat(primaryKeyExists.getAttributes().get(0).getName(), equalTo("tableName"));
        assertThat(primaryKeyExists.getAttributes().get(0).getValue(), equalTo("CREATE_NEW_CHANGE_SET"));

        assertThat(primaryKeyExists.getAttributes().get(1).getName(), equalTo("primaryKeyName"));
        assertThat(primaryKeyExists.getAttributes().get(1).getValue(), equalTo("PK_CREATE_NEW_CHANGE_SET"));

        Element addPrimaryKey = (Element) pkChangeSet.getContent().get(1);
        assertThat(addPrimaryKey.getName(), equalTo("addPrimaryKey"));
        assertThat(addPrimaryKey.getAttributes(), hasSize(3));
        assertThat(addPrimaryKey.getAttributes().get(0).getName(), equalTo("tableName"));
        assertThat(addPrimaryKey.getAttributes().get(0).getValue(), equalTo("CREATE_NEW_CHANGE_SET"));
        assertThat(addPrimaryKey.getAttributes().get(1).getName(), equalTo("columnNames"));
        assertThat(addPrimaryKey.getAttributes().get(1).getValue(), equalTo("ID"));
        assertThat(addPrimaryKey.getAttributes().get(2).getName(), equalTo("constraintName"));
        assertThat(addPrimaryKey.getAttributes().get(2).getValue(), equalTo("PK_CREATE_NEW_CHANGE_SET"));

    }

    @Test
    public void createSubClassesChangeSet() {
        ChangeSetCreator changeSetCreator = new ChangeSetCreator();
        changeSetCreator.create(ClassA.class, "abc");
        assertThat(changeSetCreator.getChangeSetList().size(), is(3));
    }

    @Test
    public void noPkClass() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("No primary key found for field noPkClass");

        ChangeSetCreator changeSetCreator = new ChangeSetCreator();
        changeSetCreator.create(NoPkClass2.class, "abc");
    }

    @Test
    public void createVariableWithMoreThan30Characters() throws Exception{
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Constraint abcdefghijklmnopqrstuvwxyz1234567890 excede o número máximo de caracteres\n" +
                "Total caracteres: 36\n" +
                "Máximo permitido 30");

        ChangeSetCreator changeSetCreator = new ChangeSetCreator();
        changeSetCreator.create(LongCharacter.class, "abc");
    }
}