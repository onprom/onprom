/*
 * logextractor
 *
 * ToolUtil.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.onprom.logextractor.util;

import org.deckfour.xes.model.XTrace;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;

public class ToolUtil {
    public static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static Collection<XTrace> sortTrace(Collection<XTrace> collection, String field) {
        collection.forEach(
                trace -> trace.sort(Comparator.comparing(e -> e.getAttributes().get(field)))
        );
        return collection;
    }

    /*
      File I/O
     */
    //create File
    public static void createFile(File filename) {
        try {
            if (!filename.exists()) {
                filename.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //write to file, append
    public static boolean fileChaseWrite(String content, String filePath) {
        boolean flag = false;
        try {
            FileWriter fw = new FileWriter(filePath, true);
            fw.write(content);
            fw.flush();
            fw.close();
            flag = true;
        } catch (Exception e) {
            //
            e.printStackTrace();
        }
        return flag;
    }


    //write to file, overwrite
    public static boolean writeToFile(String content, String filePath) {
        boolean flag = false;
        try {
            //file path
            PrintWriter pw = new PrintWriter(filePath);
            //content will be written
            pw.write(content);
            pw.flush();
            pw.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    //read the conent from file
    public static String readFromFile(File file) {
        StringBuilder sResult = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(reader);
            String s;
            while ((s = br.readLine()) != null) {
                sResult.append(s).append("\n");
//                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sResult.toString();
    }

    public static String shortenId(String str) {
        String prefix = "http://onprom.inf.unibz.it/";
        int index = str.indexOf(prefix);
        return str.substring(index + prefix.length());
    }

}
