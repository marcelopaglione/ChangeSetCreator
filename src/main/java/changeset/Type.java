package changeset;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Type {

    private static Set<Class> userDatabaseDefinedTypes;

    static {
        userDatabaseDefinedTypes = new HashSet<>();
        userDatabaseDefinedTypes.add(String.class);
        userDatabaseDefinedTypes.add(LocalDate.class);
        userDatabaseDefinedTypes.add(BigDecimal.class);
        userDatabaseDefinedTypes.add(Long.class);
        userDatabaseDefinedTypes.add(boolean.class);
        userDatabaseDefinedTypes.add(Boolean.class);
    }

    public static String getPersonalizedType(Field field) {

        if (field.getType() == String.class) {
            if (Utils.isMaxLength(field)) {
                return "varchar(" + field.getAnnotation(Column.class).length() + ")";
            }
            throw new RuntimeException(String.format(
                    "Size not defined for field %s at Table %s\nSuggestion: Add annotation @Column(length = 25)",
                    field.getName(), field.getDeclaringClass().getName())
            );
        }
        if (field.getType() == LocalDate.class) return "${data}";
        if (field.getType() == BigDecimal.class || field.getType() == Long.class) return "${numeric}";
        if (field.getType() == Boolean.class || field.getType() == boolean.class) return "varchar(1)";

        return "non-defined";
    }


    public static boolean isPersonalizedType(Class<?> clazz) {
        return getUserDatabaseDefinedTypes().contains(clazz);
    }

    public static Set<Class> getUserDatabaseDefinedTypes() {
        return userDatabaseDefinedTypes;
    }
}
