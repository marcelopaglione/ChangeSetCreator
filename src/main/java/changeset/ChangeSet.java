package changeset;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class ChangeSet {
    private List<Element> listFk;
    private List<Element> listPk;
    private Element databaseChangeLog;
    private Element createTable;
    private String author;
    private Document doc;

    ChangeSet(Class clazz, String author) {
        this.author = author;
        doc = new Document();
        listFk = new ArrayList<>();
        listPk = new ArrayList<>();
        databaseChangeLog = new Element("databaseChangeLog");
        createNewTable(getValue(Utils.getLastValue(clazz)));
    }

    private void createNewTable(String className) {
        Element changeSet = new Element("changeSet")
                .setAttribute("id", className)
                .setAttribute("author", author);

        createTable = new Element("createTable")
                .setAttribute("tableName", className);

        changeSet.addContent(getPreconditions(className));
        changeSet.addContent(createTable);
        databaseChangeLog.setContent(changeSet);
    }

    void createDatabaseColumns(boolean isUserData, Field[] fields) {
        stream(fields).forEach(field -> {
            Attribute[] attributes = {
                    new Attribute("name", getAttributeName(isUserData, field)),
                    new Attribute("type", getTypeName(isUserData, field))};
            addColumn(Utils.isNotNull(field), attributes);
            if (isUserData) addFk(field);
            if (Utils.isPk(field)) addPk(field);
        });
    }

    private Element getPreconditions(String className) {
        return new Element("preConditions")
                .setAttribute("onFail", "MARK_RAN")
                .setAttribute("onFailMessage", String.format("A tabela %s ja existe", className))
                .setContent(new Element("not")
                        .setContent(new Element("tableExists")
                                .setAttribute("tableName", className)));
    }

    private Element addColumn(Attribute... attributes) {
        Element element = new Element("column");
        stream(attributes).forEach(attribute ->
                element.setAttribute(Utils.toLower(attribute.getName()),
                        attribute.getName().equals("type") ?
                                Utils.toLower(attribute.getValue()) :
                                getValue(attribute.getValue())
                )
        );
        createTable.addContent(element);
        return element;
    }

    private void addColumn(boolean notNull, Attribute... attributes) {
        if (notNull) {
            addColumn(attributes).addContent(addNotNullConstraint());
        } else {
            addColumn(attributes);
        }
    }

    private Element addNotNullConstraint() {
        return new Element("constraints").setAttribute("nullable", "false");
    }

    private void addPk(Field field) {
        String tableName = getValue(field.getDeclaringClass().getSimpleName());
        String pkName = getValue(String.format("pk%s", field.getDeclaringClass().getSimpleName()));

        listPk.add(new Element("changeSet")
                .setAttribute("id", pkName)
                .setAttribute("author", author)
                .setContent(new Element("preConditions")
                        .setAttribute("onFail", "MARK_RAN")
                        .setAttribute("onFailMessage", String.format("A %s ja existe", pkName))
                        .setContent(new Element("not")
                                .setContent(new Element("primaryKeyExists")
                                        .setAttribute("tableName", tableName)
                                        .setAttribute("primaryKeyName", pkName)
                                )
                        )
                ).addContent(new Element("addPrimaryKey")
                        .setAttribute("tableName", tableName)
                        .setAttribute("columnNames", getValue(field.getName()))
                        .setAttribute("constraintName", pkName)
                )
        );
    }

    private void addFk(Field field) {
        AtomicReference<String> classToPkName = new AtomicReference<>("id");
        getClassToPkName(field, classToPkName);

        String classFromName = field.getDeclaringClass().getSimpleName();
        String classToName = Utils.getLastValue(field.getType());
        String fieldName = getJoinColumnName(field);

        String fkName = getValue(String.format("fkId%s%s", classToName, classFromName));
        String baseColumnName = getValue(fieldName);
        String baseTableName = getValue(classFromName);
        String referencedTableName = getValue(classToName);

        listFk.add(new Element("changeSet")
                .setAttribute("author", author)
                .setAttribute("id", fkName)
                .setContent(getFKPreConditions(field, fkName))
                .addContent(
                        new Element("addForeignKeyConstraint")
                                .setAttribute("baseColumnNames", baseColumnName)
                                .setAttribute("baseTableName", baseTableName)
                                .setAttribute("constraintName", fkName)
                                .setAttribute("referencedColumnNames", getValue(classToPkName.get()))
                                .setAttribute("referencedTableName", referencedTableName)
                )
        );
    }

    private String getJoinColumnName(Field field) {
        if (fieldHaveJoinColumnAnnotation(field)) {
            return Utils.getFieldJoinColumnAnnotationName(field);
        } else {
            return getAttributeName(true, field);
        }
    }

    private String getAttributeName(boolean isUserData, Field field) {
        if (Utils.fieldHaveColumnAnnotation(field)) {
            return Utils.getFieldColumnAnnotationName(field);
        } else {
            String name = isUserData ? String.format("id_%s", field.getName()) : field.getName();
            Utils.checkMaxStringSize(name);
            return name;
        }
    }

    private boolean fieldHaveJoinColumnAnnotation(Field field) {
        return field.getAnnotation(JoinColumn.class) != null;
    }

    private void getClassToPkName(Field field, AtomicReference<String> classToPkName) {
        List<Field> pkFieldsWithAnnotationId = stream(field.getType().getDeclaredFields())
                .filter(f -> f.getAnnotation(Id.class) != null)
                .collect(Collectors.toList());
        if (pkFieldsWithAnnotationId.size() == 1) {
            classToPkName.set(pkFieldsWithAnnotationId.get(0).getName());
            return;
        }
        throw new RuntimeException(String.format("No primary key found for field %s%n", field.getName()));
    }

    private Element getFKPreConditions(Field destination, String fkName) {
        return new Element("preConditions")
                .setAttribute("onFail", "MARK_RAN")
                .setAttribute("onFailMessage", String.format("A constraint %s ja existe", fkName))
                .setContent(new Element("not")
                        .setContent(new Element("foreignKeyConstraintExists")
                                .setAttribute("foreignKeyName", fkName)
                                .setAttribute("foreignKeyTableName", getValue(destination.getName()))
                        )
                );
    }


    private String getValue(String value) {
        return addUnderscore(value).toUpperCase();
    }

    Document getXmlDocument() {
        listPk.forEach(pk -> databaseChangeLog.addContent(pk));
        listFk.forEach(fk -> databaseChangeLog.addContent(fk));
        doc.setRootElement(databaseChangeLog);
        return doc;
    }

    private String addUnderscore(String currentString) {
        String separator = "_";
        if (currentString.contains(separator) || currentString.isEmpty()) {
            return currentString;
        }
        StringBuilder newString = new StringBuilder();
        char[] currentStringArray = currentString.toCharArray();
        char currentChar;
        newString.append(currentString.charAt(0));

        for (int i = 1; i < currentStringArray.length; i++) {
            currentChar = currentStringArray[i];
            if (Character.isUpperCase(currentChar)) {
                newString.append(separator).append(currentChar);
            } else {
                newString.append(currentChar);
            }

        }
        return newString.toString();
    }


    private String getTypeName(boolean isUserData, Field field) {
        return isUserData ? "${numeric}" : Type.getPersonalizedType(field);
    }

    public Element getDatabaseChangeLog() {
        return databaseChangeLog;
    }

}
