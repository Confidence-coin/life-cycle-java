package com.gazman.lifecycle;

import java.io.File;
import java.net.URL;

/**
 * Created by Ilya Gazman on 2/28/2018.
 */
public class G {

    static Class mainClass;

    public static File getResource(String path) {
        URL resource = mainClass.getClassLoader().getResource(path);
        if (resource == null) {
            return null;
        }
        return new File(resource.getFile());
    }
}
