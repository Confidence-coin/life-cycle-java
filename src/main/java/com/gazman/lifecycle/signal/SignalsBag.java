package com.gazman.lifecycle.signal;

import com.gazman.lifecycle.Factory;

import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * Created by Ilya Gazman on 2/24/2015.
 */
public final class SignalsBag {

    static HashMap<Class<?>, Signal> map = new HashMap<>();

    private SignalsBag() {
    }

    /**
     * Will get you a signal from the given interface type, there will be only one instance of it in the system
     *
     * @param type the signal type
     * @return Signal from given type
     */
    public static <T> Signal<T> inject(Class<T> type) {
        @SuppressWarnings("unchecked")
        Signal<T> signal = map.get(type);
        if (signal == null) {
            signal = new Signal<T>(type);
            map.put(type, signal);
        }
        return signal;
    }

    /**
     * Create new signal from given type
     *
     * @param type the interface type
     * @return Signal from given type
     */
    public static <T> Signal<T> create(Class<T> type) {
        //noinspection unchecked
        return new Signal(type);
    }

    public static <T> T log(Class<T> tClass) {
        return log(tClass, Factory.inject(MethodLogger.class));
    }

    public static <T> T log(Class<T> tClass, MethodLoggable methodLoggable) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, (proxy, method, args) -> {
            methodLoggable.log(method, args);
            return null;
        });
    }

}
