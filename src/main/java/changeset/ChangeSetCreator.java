package changeset;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static java.lang.Class.*;
import static java.util.Arrays.*;

public class ChangeSetCreator {

    private ChangeSet changeSet;

    private Map<String, Document> changeSetList;

    public void create(Class clazz, String author) {
        changeSetList = new HashMap<>();
        Set<Field> subClassesOfClazz = new HashSet<>(Arrays.asList(getUserData(clazz, getSystemData(clazz))));
        try {
            createNewChangeSet(clazz, author, subClassesOfClazz);
            printDocuments();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printDocuments() {
        String dir = System.getProperty("user.dir") + File.separator + "changeSetFiles";
        new File(dir).mkdirs();

        changeSetList.forEach((key, value) -> {
            try {
                new XMLOutputter(Format.getPrettyFormat())
                        .output(value, new FileWriter(new File(dir + File.separator + key + ".xml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void createNewChangeSet(Class clazz, String author, Set<Field> subClassesOfClazz)
            throws ClassNotFoundException {
        Field[] systemData, userData;
        do {
            changeSet = new ChangeSet(clazz, author);
            systemData = getSystemData(clazz);
            userData = getUserData(clazz, systemData);

            subClassesOfClazz.addAll(Arrays.asList(userData));

            sort(systemData, Comparator.comparing(o -> o.getType().getSimpleName().length()));
            sort(userData, Comparator.comparing(o -> o.getType().getSimpleName().length()));

            changeSet.createDatabaseColumns(false, systemData);
            changeSet.createDatabaseColumns(true, userData);

            changeSetList.put(clazz.getSimpleName(), changeSet.getXmlDocument());

            if (!subClassesOfClazz.isEmpty()) clazz = getClazzByName(subClassesOfClazz.iterator().next());

        } while (isSubClassesNotEmpty(subClassesOfClazz));
    }

    private boolean isSubClassesNotEmpty(Set<Field> subClassesOfClazz) {
        return subClassesOfClazz.iterator().hasNext() && subClassesOfClazz.remove(subClassesOfClazz.iterator().next());
    }

    private Class getClazzByName(Field subClassesOfClazz) throws ClassNotFoundException {
        return forName(subClassesOfClazz.getType().getTypeName());
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
