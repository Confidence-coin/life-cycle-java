package com.gazman.lifecycle.signal;

import java.lang.reflect.Method;

public interface MethodLoggable {
    void log(Method method, Object[] args);
}
