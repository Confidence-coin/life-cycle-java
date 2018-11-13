package com.gazman.lifecycle.signal;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MethodLogger implements MethodLoggable {
    @Override
    public void log(Method method, Object[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        if (args != null) {
            for (Object arg : args) {
                if(arg != null){
                    stringBuilder.append(arg.getClass().getSimpleName()).append("(").append(arg).append(")");
                }
                else{
                    stringBuilder.append(arg);
                }
                stringBuilder.append(arg).append(" ");
            }
        }
        Logger.getLogger(MethodLogger.class.getSimpleName()).log(Level.FINE, method.getName() + "(" + stringBuilder + ")");
    }
}
