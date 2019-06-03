package changeset;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

import static java.lang.Class.forName;
import static java.util.Arrays.sort;
import static java.util.Arrays.stream;

public class ChangeSetCreator {

    private ChangeSet changeSet;

    private Map<String, Document> changeSetList;

    public void create(Class clazz, String author) {
        changeSetList = new HashMap<>();
        Set<Field> subClassesOfClazz = new HashSet<>(Arrays.asList(getUserData(clazz, getSystemData(clazz))));
        try {
            createNewChangeSet(clazz, author, subClassesOfClazz);
            printChangeSetDocuments();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printChangeSetDocuments() {
        String dir = System.getProperty("user.dir") + File.separator + "ResultChangeSetFiles";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        changeSetList.forEach((key, value) -> {
            try {
                value.getContent().get(0).getDocument();
                OutputStream xmlValue = getOutputStream();
                new XMLOutputter(Format.getPrettyFormat()).output(value, xmlValue);

                FileWriter fileWriter = new FileWriter(new File(dir + File.separator + key + ".xml"));
                fileWriter.write(addXmlns(xmlValue));
                fileWriter.flush();
                fileWriter.close();

                System.out.printf("Generated file %s%s%s.xml%n", dir, File.separator, key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String addXmlns(OutputStream xmlValue) {
        return xmlValue.toString().replace("<databaseChangeLog>",
                "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.4.xsd\">");
    }

    private OutputStream getOutputStream() {
        return new OutputStream() {
            private StringBuilder stringBuilder = new StringBuilder();

            @Override
            public void write(int b) {
                stringBuilder.append((char) b);
            }

            @Override
            public String toString() {
                return stringBuilder.toString();
            }
        };
    }

    private void createNewChangeSet(Class clazz, String author, Set<Field> subClassesOfClazz)
            throws ClassNotFoundException {
        Field[] systemData, userData;
        do {
            changeSet = new ChangeSet(clazz, author);
            systemData = getSystemData(clazz);
            userData = getUserData(clazz, systemData);

            subClassesOfClazz.addAll(Arrays.asList(userData));

            sort(systemData, Comparator.comparing(o -> o.getName().length()));
            sort(userData, Comparator.comparing(o -> o.getName().length()));

            changeSet.createDatabaseColumns(false, systemData);
            changeSet.createDatabaseColumns(true, userData);

            changeSetList.put(clazz.getSimpleName(), changeSet.getXmlDocument());

            if (!subClassesOfClazz.isEmpty()) clazz = getClassByName(subClassesOfClazz.iterator().next());

        } while (isSubClassesNotEmpty(subClassesOfClazz));
    }

    private boolean isSubClassesNotEmpty(Set<Field> subClassesOfClazz) {
        return subClassesOfClazz.iterator().hasNext() && subClassesOfClazz.remove(subClassesOfClazz.iterator().next());
    }

    private Class getClassByName(Field subClassOfClazz) throws ClassNotFoundException {
        return forName(subClassOfClazz.getType().getTypeName());
    }

    private Field[] getUserData(Class clazz, Field[] systemData) {
        return stream(clazz.getDeclaredFields()).filter(data ->
                !CollectionUtils.arrayToList(systemData).contains(data)).toArray(Field[]::new);
    }

    private Field[] getSystemData(Class clazz) {
        return stream(clazz.getDeclaredFields()).filter(data -> Type.isPersonalizedType(data.getType())).toArray(Field[]::new);
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public Map<String, Document> getChangeSetList() {
        return changeSetList;
    }
}
