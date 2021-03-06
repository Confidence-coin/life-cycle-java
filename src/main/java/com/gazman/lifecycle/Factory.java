package com.gazman.lifecycle;

import java.util.HashMap;

/**
 * Inline tool for injecting classes.<br>
 * If your class implements Singleton, the same instance will be returned for all calls with the same family,
 * if family wasn't specified, default value will apply<br>
 * If your class implements Injector, Injector.injectionHandler() will be called right after the class contractor, providing you with information about which family been used to inject this class.
 */
public class Factory {

    /**
     * Retrieve instance of classType class
     *
     * @param classType the class to retrieve
     * @return instance of classType class
     */
    public static <T> T inject(Class<T> classType) {
        return inject(classType, Registrar.DEFAULT_FAMILY);
    }

    /**
     * Retrieve instance of classType class using MultiTon pattern with family as a
     * key
     *
     * @param classType the class to retrieve
     * @return instance of classType class
     * @see <a href="http://en.wikipedia.org/wiki/Multiton_pattern">Multiton
     * pattern on wiki</a>
     */
    public static <T> T inject(Class<T> classType, String family) {
        HashMap<String, Class<?>> hashMap = Registrar.classesMap.get(classType);
        @SuppressWarnings("unchecked")
        Class<? extends T> classToUse = hashMap != null ? (Class<? extends T>) hashMap.get(family) : classType;
        T instance;
        if (Singleton.class.isAssignableFrom(classToUse)) {
            instance = ClassConstructor.constructSingleTon(family, classToUse);
        } else {
            instance = ClassConstructor.constructWithNoParams(classToUse);
            if (instance instanceof Injector) {
                ((Injector) instance).injectionHandler(family);
            }
        }

        return instance;
    }

    /**
     * Retrieve instance of classType class <br><br>
     * - In case your constructor has one string parameter call injectWithParams(classType, null, your string parameter)}
     *
     * @param classType the class to retrieve
     * @param params    contractor parameters
     * @return instance of classType class
     */
    public static <T> T injectWithParams(Class<T> classType, Object... params) {
        return injectWithParamsAndFamily(classType, Registrar.DEFAULT_FAMILY, params);
    }

    /**
     * Retrieve instance of claz class <br><br>
     *
     * @param claz   the class to inject
     * @param family class family for Multiton pattern
     * @param params the constructor parameters
     * @return instance of claz class
     * @see <a href="http://en.wikipedia.org/wiki/Multiton_pattern">Multiton
     * pattern on wiki</a>
     */

    public static <T> T injectWithParamsAndFamily(Class<T> claz, String family,
                                                  Object... params) {
        if (family == null) {
            family = Registrar.DEFAULT_FAMILY;
        }
        HashMap<String, Class<?>> hashMap = Registrar.classesMap.get(claz);
        @SuppressWarnings("unchecked")
        Class<? extends T> classToUse = hashMap != null ? (Class<? extends T>) hashMap.get(family) : claz;

        T instance;
        if (Singleton.class.isAssignableFrom(classToUse)) {
            instance = ClassConstructor.constructSingleTon(family, classToUse, params);
        } else {
            instance = ClassConstructor.construct(classToUse, params);
            if (instance instanceof Injector) {
                ((Injector) instance).injectionHandler(family);
            }
        }

        return instance;
    }
}
