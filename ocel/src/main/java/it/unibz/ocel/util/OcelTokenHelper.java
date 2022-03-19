/*
 * ocel
 *
 * OcelTokenHelper.java
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

package it.unibz.ocel.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OcelTokenHelper {

    public OcelTokenHelper() {
    }

    public static String formatTokenString(List<String> tokens) {
        if (tokens.size() <= 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(formatToken(tokens.get(0)));

            for (int i = 1; i < tokens.size(); ++i) {
                sb.append(' ');
                sb.append(formatToken(tokens.get(i)));
            }

            return sb.toString();
        }
    }

    private static String formatToken(String token) {
        token = token.trim();
        if (token.indexOf(32) < 0 && token.indexOf(9) < 0) {
            return token;
        } else {
            StringBuilder sb = new StringBuilder();
            token = token.replaceAll("'", "\\'");
            sb.append('\'');
            sb.append(token);
            sb.append('\'');
            return sb.toString();
        }
    }

    public static List<String> extractTokens(String tokenString) {
        List<String> tokens = new ArrayList();
        boolean isEscaped = false;
        boolean isQuoted = false;
        StringBuilder sb = new StringBuilder();
        char[] arr$ = tokenString.toCharArray();

        for (char c : arr$) {
            if (c == ' ' && !isQuoted && !isEscaped) {
                String token = sb.toString().trim();
                if (token.length() > 0) {
                    tokens.add(token);
                }

                sb = new StringBuilder();
            } else if (c == '\\') {
                isEscaped = true;
            } else if (c == '\'') {
                if (!isEscaped) {
                    isQuoted = !isQuoted;
                } else {
                    sb.append(c);
                }

                isEscaped = false;
            } else {
                sb.append(c);
                isEscaped = false;
            }
        }

        String token = sb.toString().trim();
        if (token.length() > 0) {
            tokens.add(token);
        }

        return Collections.unmodifiableList(tokens);
    }
}

