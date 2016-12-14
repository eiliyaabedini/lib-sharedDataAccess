package ir.iact.sharedData;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SharedDataAccess {

    public static final String TYPE_STRING = "String";
    public static final String TYPE_INT = "int";
    public static final String TYPE_BIG_INT = "Integer";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_BIG_LONG = "Long";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_BIG_FLOAT = "Float";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_BIG_BOOLEAN = "Boolean";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_BIG_DOUBLE = "Double";

    public static final String DEFAULT_STRING = "";
    public static final int DEFAULT_INT = -1;
    public static final long DEFAULT_LONG = -1;
    public static final float DEFAULT_FLOAT = -1.0f;
    public static final boolean DEFAULT_BOOLEAN = false;

    private Context context;

    public SharedDataAccess(Context context) {

        this.context = context;
    }


    public <T extends SharedDefaultValues> T create(final Class<T> service) {
        validateServiceInterface(service);
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object... args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        return invokeMethod(method, args);
                    }
                });
    }

    private Object invokeMethod(Method method, Object[] args) {
        String methodName = method.getName();
        String getSet = methodName.substring(0, 3);
        String valueName = methodName.substring(3);

//        String paramsType = method.getParameterTypes();

        // Check its setter or getter
        if (getSet.equals("set")) {
            set(method, valueName, args);
        } else if (getSet.equals("get")) {
            return get(method, valueName);
        } else if (getSet.equals("def")) {
            return defaultValues(methodName);
        } else throw new IllegalArgumentException("Only Getter And Setter is allowed");
        return null;
    }

    static <T> void validateServiceInterface(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
        // Prevent API interfaces from extending other interfaces. This not only avoids a bug in
        // Android (http://b.android.com/58753) but it forces composition of API declarations which is
        // the recommended pattern.
        if (service.getInterfaces().length > 1) {
            throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
        }
    }

    private Object defaultValues(String methodName) {
        if (methodName.contains("String")) {
            return DEFAULT_STRING;
        } else if (methodName.contains("Long")) {
            return DEFAULT_LONG;
        } else if (methodName.contains("Integer")) {
            return DEFAULT_INT;
        } else if (methodName.contains("Float")) {
            return DEFAULT_FLOAT;
        } else if (methodName.contains("Boolean")) {
            return DEFAULT_BOOLEAN;
        } else
            return DEFAULT_INT;
    }


    private void set(Method method, String key, Object[] args) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        String firstParameterType = parameterTypes[0].getSimpleName();

        set(firstParameterType, key, args[0]);

    }

    private void set(String type, String key, Object value) {
        if (value == null)
            return;
        if (type.equals(TYPE_STRING)) {
            setSharedPreferenceString(key, (String) value);
        } else if (type.equals(TYPE_INT) || type.equals(TYPE_BIG_INT)) {
            setSharedPreferencesInt(key, (int) value);
        } else if (type.equals(TYPE_LONG) || type.equals(TYPE_BIG_LONG))
            setSharedPreferencesLong(key, (long) value);
        else if (type.equals(TYPE_FLOAT) || type.equals(TYPE_BIG_FLOAT))
            setSharedPreferencesFloat(key, (float) value);
        else if (type.equals(TYPE_BOOLEAN) || type.equals(TYPE_BIG_BOOLEAN))
            setSharedPreferencesBoolean(key, (boolean) value);
        else if (type.equals(TYPE_DOUBLE) || type.equals(TYPE_BIG_DOUBLE))
            throw new IllegalArgumentException("Double not supported by SharedPreferences");
        else {
            saveSharedDataObject(key, value);
        }
    }

    private void saveSharedDataObject(String methodName, Object object) {
        Class<?> clazz = object.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            try {
                String fieldName = declaredField.getName();
                String type = declaredField.getType().getSimpleName();
                Object value = object.getClass().getField(fieldName).get(object);

                if (value != null) {
                    set(type, methodName + "-" + fieldName, value);
                }
            } catch (IllegalAccessException e) {
            } catch (NoSuchFieldException e) {
            }
        }

    }

    private void setSharedPreferenceString(String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void setSharedPreferencesInt(String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private void setSharedPreferencesLong(String key, long value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private void setSharedPreferencesFloat(String key, float value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    private void setSharedPreferencesBoolean(String key, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    private Object get(Method method, String key) {
        String returnType = method.getReturnType().getSimpleName();
        if (returnType.equals(TYPE_STRING)) {
            return getSharedPreferencesString(key);
        } else if (returnType.equals(TYPE_INT)) {
            return getSharedPreferencesInt(key);
        } else if (returnType.toLowerCase().equals(TYPE_LONG)) {
            return getSharedPreferencesLong(key);
        } else if (returnType.toLowerCase().equals(TYPE_FLOAT)) {
            return getSharedPreferencesFloat(key);
        } else if (returnType.toLowerCase().equals(TYPE_BOOLEAN)) {
            return getSharedPreferencesBoolean(key);
        } else if (returnType.toLowerCase().equals(TYPE_DOUBLE)) {
            throw new IllegalArgumentException("Double not supported by SharedPreferences");
        } else {
            return getObject(method, key);
        }

    }



    private Object getObject(Method method, String key) {
        try {
            Class<?> clazz  = method.getReturnType();
            Object dataObject = clazz.newInstance();
            for (Field field : clazz.getFields()) {
                Class<?> type = field.getType();

                Object value = null;
                if (type.equals(Integer.class) || type.equals(int.class))
                    value = getSharedPreferencesInt(key + "-" + field.getName());
                else if (type.equals(String.class))
                    value = getSharedPreferencesString(key + "-" + field.getName());
                else if (type.equals(boolean.class) || type.equals(Boolean.class))
                    value = getSharedPreferencesBoolean(key + "-" + field.getName());
                else if (type.equals(float.class) || type.equals(Float.class))
                    value = getSharedPreferencesFloat(key + "-" + field.getName());
                else if (type.equals(long.class) || type.equals(Long.class))
                    value = getSharedPreferencesLong(key + "-" + field.getName());
                field.set(dataObject, value);
            }
            return dataObject;
        } catch (Exception e) {
            return null;
        }
    }

    private String getSharedPreferencesString(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, DEFAULT_STRING);
    }

    private Integer getSharedPreferencesInt(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, DEFAULT_INT);
    }

    private Long getSharedPreferencesLong(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(key, DEFAULT_LONG);
    }

    private Float getSharedPreferencesFloat(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat(key, DEFAULT_FLOAT);
    }

    private Boolean getSharedPreferencesBoolean(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, DEFAULT_BOOLEAN);
    }
}
