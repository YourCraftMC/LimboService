/*
 * This file is part of Limbo.
 *
 * Copyright (C) 2024. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2024. Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loohp.limbo.utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * list resources available from the classpath @ *
 */
public class ClasspathResourcesUtils {

    /**
     * for all elements of java.class.path get a Collection of resources
     *
     * @param word the word to contains match
     * @return the resources in the order they are found
     */
    public static Collection<String> getResources(String word, boolean dir) {
        List<String> retval = new ArrayList<>();
        String classPath = System.getProperty("java.class.path", ".");
        String[] classPathElements = classPath.split(File.pathSeparator);
        for (String element : classPathElements) {
            retval.addAll(getResources(element, word, dir));
        }
        Collections.sort(retval);
        return retval;
    }

    private static Collection<String> getResources(String element, String word, boolean dir) {
        List<String> retval = new ArrayList<>();
        File file = new File(element);
        if (file.isDirectory()) {
            if (dir)
                retval.addAll(getResourcesFromDirectory(file, word));
        } else {
            retval.addAll(getResourcesFromJarFile(file, word, dir));
        }
        return retval;
    }

    private static Collection<String> getResourcesFromJarFile(final File file, final String word, boolean dir) {
        List<String> retval = new ArrayList<>();
        ZipFile zf;
        try {
            zf = new ZipFile(file);
        } catch (IOException e) {
            throw new Error(e);
        }
        Enumeration<? extends ZipEntry> e = zf.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = e.nextElement();
            String fileName = ze.getName();
            if (ze.isDirectory() && !dir) continue;
            boolean accept = fileName.replace("\\", "/").contains(word);
            if (accept) {
                retval.add(fileName);
            }
        }
        try {
            zf.close();
        } catch (IOException e1) {
            throw new Error(e1);
        }
        return retval;
    }

    private static Collection<String> getResourcesFromDirectory(File directory, String word) {
        List<String> retval = new ArrayList<>();
        File[] fileList = directory.listFiles();
        for (File file : fileList) {
            if (file.isDirectory()) {
                retval.addAll(getResourcesFromDirectory(file, word));
            } else {
                try {
                    String fileName = file.getCanonicalPath();
                    boolean accept = fileName.replace("\\", "/").contains(word);
                    if (accept) {
                        retval.add(fileName);
                    }
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        }
        return retval;
    }
}
