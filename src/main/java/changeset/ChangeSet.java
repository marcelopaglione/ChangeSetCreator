package changeset;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;
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
    private Element changeSet;
    private Element createTable;
    private String author;
    private Document doc;

    public ChangeSet(Class c, String author) {
        this.author = author;
        doc = new Document();
        listFk = new ArrayList<>();
        listPk = new ArrayList<>();
        setDatabaseChangeLogAttributes();
        createNewTable(getValue(getLastValue(c)));
    }

    private void createNewTable(String className) {
        changeSet = new Element("changeSet")
                .setAttribute("id", className)
                .setAttribute("author", author);

        createTable = new Element("createTable")
                .setAttribute("tableName", className);


        changeSet.addContent(getPreconditions(className));
        changeSet.addContent(createTable);
        databaseChangeLog.setContent(changeSet);
    }

    private Element getPreconditions(String className) {
        return new Element("preConditions")
                .setAttribute("onFail", "MARK_RAN")
                .setAttribute("onFailMessage", String.format("A tabela %s já existe", className))
                .setContent(new Element("not")
                        .setContent(new Element("tableExists")
                                .setAttribute("tableName", className)));
    }

    private void setDatabaseChangeLogAttributes() {
        databaseChangeLog = new Element("databaseChangeLog");
        //databaseChangeLog.setAttribute("xmlns","http://www.liquibase.org/xml/ns/dbchangelog");
        //databaseChangeLog.setAttribute("xsi:schemaLocation","http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.4.xsd");
        //databaseChangeLog.setAttribute("xmlns:xsi","http://www.liquibase.org/xml/ns/dbchangelog");
    }

    private Element addColumn(Attribute... attributes) {
        Element element = new Element("column");
        stream(attributes).forEach(attribute ->
                element.setAttribute(toLower(attribute.getName()),
                        attribute.getName().equals("type") ?
                                toLower(attribute.getValue()) :
                                getValue(attribute.getValue())
                )
        );
        createTable.addContent(element);
        return element;
    }

    public Element addColumn(boolean notNull, Attribute... attributes) {
        return notNull ? addColumn(attributes).addContent(addNotNullConstraint()) : addColumn(attributes);
    }

    private Element addNotNullConstraint() {
        return new Element("constraints").setAttribute("nullable", "false");
    }

    private void addPk(Field field) {
        String fieldName = getAttributeName(false, field);
        String tableName = getValue(field.getDeclaringClass().getSimpleName());
        String pkName = getValue(String.format("pk%s", field.getDeclaringClass().getSimpleName()));

        listPk.add(new Element("changeSet")
                .setAttribute("id", pkName)
                .setAttribute("author", author)
                .setContent(new Element("preConditions")
                        .setAttribute("onFail", "MARK_RAN")
                        .setAttribute("onFailMessage", String.format("A %s já existe", pkName))
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
        String classToName = getLastValue(field.getType());
        String fieldName = getJoinColumnName(true, field);

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

    private String getJoinColumnName(boolean isUserData, Field field) {
        if(fieldHaveJoinColumnAnnotation(field)){
            return getFieldJoinColumnAnnotationName(field);
        } else {
            return getAttributeName(isUserData, field);
        }
    }

    private String getAttributeName(boolean isUserData, Field field) {
        if(fieldHaveColumnAnnotation(field)){
            return getFieldColumnAnnotationName(field);
        } else {
            String name = isUserData ? String.format("id_%s", field.getName()) : field.getName();
            checkMaxStringSize(name);
            return name;
        }
    }

    private String getFieldColumnAnnotationName(Field field) {
        String name = field.getAnnotation(Column.class).name();
        checkMaxStringSize(name);
        return name;
    }

    public void createDatabaseColumns(boolean isUserData, Field[] fields) {
        stream(fields).forEach(field -> {
            Attribute[] attributes = {
                    new Attribute("name", getAttributeName(isUserData, field)),
                    new Attribute("type", getTypeName(isUserData, field))};
            addColumn(isNotNull(field), attributes);
            if (isUserData) addFk(field);
            if (isPk(field)) addPk(field);
        });
    }

    private boolean fieldHaveColumnAnnotation(Field field){
        return field.getAnnotation(Column.class) != null;
    }

    private String getFieldJoinColumnAnnotationName(Field field) {
        String name = field.getAnnotation(JoinColumn.class).name();
        checkMaxStringSize(name);
        return name;
    }

    private boolean fieldHaveJoinColumnAnnotation(Field field){
        return field.getAnnotation(JoinColumn.class) != null;
    }

    private void getClassToPkName(Field field, AtomicReference<String> classToPkName) {
        List<Field> pkFieldsWithAnnotationId = stream(field.getType().getDeclaredFields())
                .filter(f -> f.getAnnotation(Id.class) != null)
                .collect(Collectors.toList());
        if(pkFieldsWithAnnotationId.size() == 1) {
            classToPkName.set(pkFieldsWithAnnotationId.get(0).getName());
            return;
        }
        throw new RuntimeException(String.format("No primary key found for field %s%n", field.getName()));
    }

    private Element getFKPreConditions(Field destination, String fkName) {
        return new Element("preConditions")
                .setAttribute("onFail", "MARK_RAN")
                .setAttribute("onFailMessage", String.format("A constraint %s já existe", fkName))
                .setContent(new Element("not")
                        .setContent(new Element("foreignKeyConstraintExists")
                                .setAttribute("foreignKeyName", fkName)
                                .setAttribute("foreignKeyTableName", getValue(destination.getName()))
                        )
                );
    }

    private String getLastValue(Class<?> c) {
        return stream(c.getName().split("\\.")).reduce((first, second) -> second).orElse(null);
    }

    private String toLower(String value) {
        return value.toLowerCase();
    }

    private String getValue(String value) {
        return addUnderscore(value).toUpperCase();
    }

    public Document getXmlDocument() {
        listPk.forEach(pk -> databaseChangeLog.addContent(pk));
        listFk.forEach(fk -> databaseChangeLog.addContent(fk));
        doc.setRootElement(databaseChangeLog);
        return doc;
    }

    private String addUnderscore(String currentString) {
        String separator = "_";
        if(currentString.contains(separator)){
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

    private boolean isPk(Field field) {
        return field.getAnnotation(Id.class) != null;
    }

    private boolean isNotNull(Field field) {
        return field.getAnnotation(NotNull.class) == null;
    }

    private String getTypeName(boolean isUserData, Field field) {
        return isUserData ? "${numeric}" : Type.getPersonalizedType(field.getType());
    }

    private void checkMaxStringSize(String value) throws RuntimeException{
        int max = 30;
        if(value.length() > max){
            throw new RuntimeException(
                    String.format("Constraint %s excede o número máximo de caracteres" +
                                    "\nTotal caracteres: %s" +
                                    "\nMáximo permitido %s",
                            value, value.length(), max));
        }
    }

    public Element getDatabaseChangeLog() {
        return databaseChangeLog;
    }

    public Document getDoc() {
        return doc;
    }
}
