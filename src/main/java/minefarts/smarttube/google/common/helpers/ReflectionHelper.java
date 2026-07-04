package minefarts.smarttube.google.common.helpers;

import android.content.Context;
import minefarts.smarttube.utils.helpers.AppInfoHelpers;
import minefarts.smarttube.utils.helpers.FileHelpers;
import minefarts.smarttube.utils.helpers.PermissionHelpers;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.google.common.converters.FieldNullable;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionHelper {

    public static Type[] getGenericParams(Field field) {
        Type genericFieldType = field.getGenericType();

        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType aType = (ParameterizedType) genericFieldType;

            return aType.getActualTypeArguments();
        }

        return null;
    }

    public static boolean isAssignableFrom(Field field, Class<?> targetType) {
        return field.getType().isAssignableFrom(targetType);
    }

    public static List<Field> getAllFields(Class<?> type) {
        Field[] fields = type.getDeclaredFields();

        List<Field> result = new ArrayList<>(Arrays.asList(fields));

        while (type.getSuperclass() != null) { // null if superclass is object
            type = type.getSuperclass();
            result.addAll(Arrays.asList(type.getDeclaredFields()));
            // ??? Speedup json parsing by putting on top important fields.
            //Collections.sort(result, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        }

        return result;
    }

    public static void setField(Field field, Object obj, Object val) {
        try {
            field.set(obj, val);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void dumpDebugInfo(Class<?> type, String content) {
        dumpDebugInfo(type, FileHelpers.toStream(content));
    }

    public static void dumpDebugInfo(Class<?> type, InputStream content) {
        // Thread probably has been interrupted. Do skip.
        if (content == null || !GlobalPreferences.isInitialized()) return;

        Context context = GlobalPreferences.sInstance.getContext();

        if (context == null) return;

        String fileName = String.format("%s_%s", type.getSimpleName(), AppInfoHelpers.getAppVersionName(context));

        File filesDir;

        if (PermissionHelpers.hasStoragePermissions(context)) {
            filesDir = FileHelpers.getBackupDir(context);
        } else {
            filesDir = FileHelpers.getExternalFilesDir(context);
        }

        FileHelpers.deleteByPrefix(filesDir, type.getSimpleName());
        File destination = new File(filesDir, fileName);
        FileHelpers.streamToFile(content, destination);

    }

    public static boolean isNullable(Field field) {
        Annotation[] annotations = field.getAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation instanceof FieldNullable) {
                return true;
            }
        }

        return false;
    }
    
}
