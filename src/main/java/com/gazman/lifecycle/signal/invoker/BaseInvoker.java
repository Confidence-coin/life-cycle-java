package com.gazman.lifecycle.signal.invoker;

import com.gazman.lifecycle.utils.UnhandledExceptionHandler;

import java.lang.reflect.Method;

/**
 * Created by Ilya Gazman on 3/18/2016.
 */
public class BaseInvoker implements Runnable {
    public Method method;
    public Object[] args;
    public Object listener;


    public BaseInvoker() {
    }

    public BaseInvoker(Method method, Object[] args, Object listener) {
        this.method = method;
        this.args = args;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            method.invoke(listener, args);
        } catch (Throwable e) {
            if (UnhandledExceptionHandler.callback == null) {
                throw new Error("Unhandled Exception, consider providing UnhandledExceptionHandler.callback", e);
            } else {
                UnhandledExceptionHandler.callback.onApplicationError(e);
            }
        }
    }
}
