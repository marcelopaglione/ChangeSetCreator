package changeset;

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

    public static String getPersonalizedType(Class clazz) {
        if (clazz == String.class) return "varchar(YOUR_SIZE_HERE)";
        if (clazz == LocalDate.class) return "${data}";
        if (clazz == BigDecimal.class || clazz == Long.class) return "${numeric}";
        if (clazz == Boolean.class || clazz == boolean.class) return "varchar(1)";

        return "non-defined";
    }

    public static boolean isPersonalizedType(Class<?> clazz) {
        return getUserDatabaseDefinedTypes().contains(clazz);
    }

    public static Set<Class> getUserDatabaseDefinedTypes() {
        return userDatabaseDefinedTypes;
    }
}
