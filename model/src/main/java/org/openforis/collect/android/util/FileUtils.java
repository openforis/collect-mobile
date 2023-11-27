package org.openforis.collect.android.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public abstract class FileUtils {

    public static List<File> listFilesRecursively(File dir) {
        List<File> result = new ArrayList<File>();
        File[] files = dir.listFiles();
        Stack<File> stack = new Stack<File>();
        stack.addAll(Arrays.asList(files));
        while (!stack.isEmpty()) {
            File file = stack.pop();
            if (file.isFile()) {
                result.add(file);
            } else {
                stack.addAll(Arrays.asList(file.listFiles()));
            }
        }
        return result;
    }

}
