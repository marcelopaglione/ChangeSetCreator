package changeset;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;

import static java.util.Arrays.stream;

class Utils {

    static boolean isMaxLength(Field field) {
        return field.getAnnotation(Column.class) != null;
    }

    static boolean isPk(Field field) {
        return field.getAnnotation(Id.class) != null;
    }

    static boolean isNotNull(Field field) {
        return field.getAnnotation(NotNull.class) == null;
    }

    static boolean fieldHaveColumnAnnotation(Field field) {
        return field.getAnnotation(Column.class) != null;
    }

    static String getFieldColumnAnnotationName(Field field) {
        String name = field.getAnnotation(Column.class).name();
        checkMaxStringSize(name);
        return name;
    }

    static String getFieldJoinColumnAnnotationName(Field field) {
        String name = field.getAnnotation(JoinColumn.class).name();
        checkMaxStringSize(name);
        return name;
    }

    static void checkMaxStringSize(String value) throws RuntimeException {
        int max = 30;
        if (value.length() > max) {
            throw new RuntimeException(
                    String.format("Constraint %s excede o número maximo de caracteres" +
                                    "\nTotal caracteres: %s" +
                                    "\nMaximo permitido %s",
                            value, value.length(), max));
        }
    }

    static String getLastValue(Class<?> clazz) {
        return stream(clazz.getName().split("\\."))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new RuntimeException(String.format("No value found for %s", clazz.toString())));
    }

    static String toLower(String value) {
        return value.toLowerCase();
    }
}
