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
            StringBuffer sb = new StringBuffer();
            sb.append(formatToken((String)tokens.get(0)));

            for(int i = 1; i < tokens.size(); ++i) {
                sb.append(' ');
                sb.append(formatToken((String)tokens.get(i)));
            }

            return sb.toString();
        }
    }

    private static String formatToken(String token) {
        token = token.trim();
        if (token.indexOf(32) < 0 && token.indexOf(9) < 0) {
            return token;
        } else {
            StringBuffer sb = new StringBuffer();
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
        StringBuffer sb = new StringBuffer();
        char[] arr$ = tokenString.toCharArray();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            char c = arr$[i$];
            if (c == ' ' && !isQuoted && !isEscaped) {
                String token = sb.toString().trim();
                if (token.length() > 0) {
                    tokens.add(token);
                }

                sb = new StringBuffer();
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

