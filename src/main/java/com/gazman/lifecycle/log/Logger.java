package com.gazman.lifecycle.log;


import com.gazman.lifecycle.Factory;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ilya Gazman on 06-Dec-14.
 */
@SuppressWarnings("unused")
public class Logger {

    private static final AtomicInteger id = new AtomicInteger();
    private String tag;
    private static long startingTime = System.currentTimeMillis();
    //    private long lastCall = System.currentTimeMillis();
    private LogSettings localSettings;
    private DecimalFormat timeFormat = new DecimalFormat("00.000");
    private DecimalFormat idFormat = new DecimalFormat("00");
    private String uniqueID = idFormat.format(id.incrementAndGet());

    /**
     * Creates logger using Factory and call the protected method init(tag);
     */
    public static Logger create(String tag) {
        Logger logger = Factory.inject(Logger.class);
        logger.init(tag);
        return logger;
    }

    protected void init(String tag) {
        localSettings = Factory.inject(LogSettings.class);
        localSettings.init();
        setTag(tag);
    }

    public void setTag(String tag) {
        String extra = "";
        for (int i = 0; i < localSettings.getMinTagLength() - tag.length(); i++) {
            extra += "_";
        }
        this.tag = tag + extra;
    }

    public LogSettings getSettings() {
        return localSettings;
    }

    protected String getClassAndMethodNames(int dept) {
        if (!localSettings.isPrintMethodName()) {
            return "";
        }
        StackTraceElement stackTraceElement = new Exception().getStackTrace()[dept];
        String className = stackTraceElement.getClassName();
        String[] classSplit = className.split("\\.");
        String classShortName = classSplit[classSplit.length - 1];
        return classShortName + "." + stackTraceElement.getMethodName();
    }

    /**
     * Default log
     *
     * @param parameters Will concat those parameters using toString method
     *                   separated by space char.
     */
    public void d(Object... parameters) {
        print("d", null, parameters);
    }

    /**
     * Default log
     *
     * @param parameters Will concat those parameters using toString method
     *                   separated by space char.
     * @param throwable  Will print stack trace of this throwable
     */
    public void d(Throwable throwable, Object... parameters) {
        print("d", throwable, parameters);
    }

    /**
     * Default log
     *
     * @param parameters Will concat those parameters using toString method
     *                   separated by space char.
     */
    public void i(Object... parameters) {
        print("i", null, parameters);
    }

    /**
     * Default log
     *
     * @param parameters Will concat those parameters using toString method
     *                   separated by space char.
     * @param throwable  Will print stack trace of this throwable
     */
    public void i(Throwable throwable, Object... parameters) {
        print("i", throwable, parameters);
    }

    /**
     * Warning log
     *
     * @param parameters Will concat those parameters using toString method
     *                   separated by space char.
     */
    public void w(Object... parameters) {
        print("w", null, parameters);
    }

    /**
     * Warning log
     *
     * @param parameters Will concat those parameters using toString method
     *                   separated by space char.
     * @param throwable  Will print stack trace of this throwable
     */
    public void w(Throwable throwable, Object... parameters) {
        print("w", throwable, parameters);
    }

    /**
     * Exception log
     *
     * @param parameters Will concat those parameters using toString method
     *                   separated by space char.
     */
    public void e(Object... parameters) {
        print("e", null, parameters);
    }

    /**
     * Exception log
     *
     * @param parameters Will concat those parameters using toString method
     *                   separated by space char.
     * @param throwable  Will print stack trace of this throwable
     */
    public void e(Throwable throwable, Object... parameters) {
        print("e", throwable, parameters);
    }

    private void print(String methodName, Throwable throwable, Object[] parameters) {
        if (!localSettings.isEnabled()) {
            return;
        }

        String color;
        PrintStream stream = methodName.equals("e") ? System.err : System.out;

        switch (methodName) {
            case "i":
                color = ConsoleColors.BLUE;
                break;
            case "w":
                color = ConsoleColors.BLUE_BOLD;
                break;
            case "e":
                color = ConsoleColors.RED_BOLD;
                break;
            default:
                color = ConsoleColors.RESET;
        }

        stream.println(color + buildMessage(parameters) + ConsoleColors.RESET);
    }

    private String buildMessage(Object[] parameters) {
        return getPrefix() + join(parameters, " ") + localSettings.getSuffix();
    }

    private String getPrefix() {
        String methodPrefix = getClassAndMethodNames(5);
        String timePrefix = getTimePrefix();
        return join(
                localSettings.getPrefixDelimiter(),
                localSettings.getAppPrefix(),
                uniqueID,
                timePrefix,
                methodPrefix
        );
    }

    private void printMessage(Throwable throwable, Method method, String message)
            throws IllegalAccessException,
            InvocationTargetException {
        if (message.length() > 4000) {
            printChuckedMessage(throwable, method, message);
        } else {
            invoke(throwable, method, message);
        }
    }

    private void printChuckedMessage(Throwable throwable, Method method, String message) throws IllegalAccessException, InvocationTargetException {
        int chunkCount = message.length() / 4000;     // integer division
        for (int i = 0; i <= chunkCount; i++) {
            int max = 4000 * (i + 1);
            String chunkMessage;
            if (max >= message.length()) {
                chunkMessage = "chunk " + i + " of " + chunkCount + ": " + message.substring(4000 * i);
            } else {
                chunkMessage = "chunk " + i + " of " + chunkCount + ": " + message.substring(4000 * i, max);
            }
            invoke(throwable, method, chunkMessage);
        }
    }

    private void invoke(Throwable throwable, Method method, String message) throws IllegalAccessException, InvocationTargetException {
        if (throwable != null) {
            method.invoke(null, tag, message, throwable);
        } else {
            method.invoke(null, tag, message);
        }
    }

    public static String join(String delimiter, Object... parameters) {
        return join(parameters, delimiter);
    }

    public static String join(Object[] parameters, String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : parameters) {
            String objectString = extractObject(object);
            stringBuilder.append(objectString);
            if (objectString.length() > 0) {
                stringBuilder.append(delimiter);
            }
        }

        return stringBuilder.toString();
    }

    private static String extractObject(Object object) {
        if (object == null) {
            return "null";
        }
        if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            Object[] objects = new Object[length];
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(object, 0, objects, 0, length);
            return "[" + join(objects, ",") + "]";
        }
        return object.toString();
    }

    public String getTimePrefix() {
        if (!localSettings.isPrintTime()) {
            return "";
        }
        long currentTimeMillis = System.currentTimeMillis();
        double totalTimePass = (currentTimeMillis - startingTime) / 1000d;

        return timeFormat.format(totalTimePass);
    }


    public class ConsoleColors {
        // Reset
        public static final String RESET = "\033[0m";  // Text Reset

        // Regular Colors
        public static final String BLACK = "\033[0;30m";   // BLACK
        public static final String RED = "\033[0;31m";     // RED
        public static final String GREEN = "\033[0;32m";   // GREEN
        public static final String YELLOW = "\033[0;33m";  // YELLOW
        public static final String BLUE = "\033[0;34m";    // BLUE
        public static final String PURPLE = "\033[0;35m";  // PURPLE
        public static final String CYAN = "\033[0;36m";    // CYAN
        public static final String WHITE = "\033[0;37m";   // WHITE

        // Bold
        public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
        public static final String RED_BOLD = "\033[1;31m";    // RED
        public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
        public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
        public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
        public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
        public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
        public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

        // Underline
        public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
        public static final String RED_UNDERLINED = "\033[4;31m";    // RED
        public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
        public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
        public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
        public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
        public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
        public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

        // Background
        public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
        public static final String RED_BACKGROUND = "\033[41m";    // RED
        public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
        public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
        public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
        public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
        public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
        public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

        // High Intensity
        public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
        public static final String RED_BRIGHT = "\033[0;91m";    // RED
        public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
        public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
        public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
        public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
        public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
        public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

        // Bold High Intensity
        public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
        public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
        public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
        public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
        public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
        public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
        public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
        public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

        // High Intensity backgrounds
        public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
        public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
        public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
        public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
        public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
        public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
        public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
        public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE
    }
}
