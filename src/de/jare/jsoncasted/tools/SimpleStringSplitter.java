/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The SimpleStringSplitter interface provides utility methods for splitting,
 * replacing, and manipulating strings. It supports basic tokenization using
 * characters, substrings, or lists of delimiters.
 *
 * @author Janusch Rentenatus
 */
public interface SimpleStringSplitter {

    /**
     * Splits a string into a list of substrings using a single character
     * delimiter.
     *
     * @param src The source string to split.
     * @param ch The delimiter character.
     * @return A list of substrings split by the given character.
     */
    public default List<String> simpleSplit(String src, char ch) {
        int off = 0;
        int next;
        ArrayList<String> list = new ArrayList<>();
        while ((next = src.indexOf(ch, off)) != -1) {
            list.add(src.substring(off, next));
            off = next + 1;
        }
        // Add remaining segment 
        // If no match was found, return all in one,
        list.add(src.substring(off));
        return list;
    }

    /**
     * Splits a string into a list of substrings using a specific substring as
     * the delimiter.
     *
     * @param src The source string to split.
     * @param splitter The substring delimiter.
     * @return A list of substrings split by the specified delimiter.
     * @param caseSens The search might be case-sensitive? False == src and
     * splitter are converted to lowercase first.
     */
    public default List<String> simpleSplit(String src, String splitter, boolean caseSens) {
        ArrayList<String> list = new ArrayList<>();
        simpleSplitInto(src, splitter, list, caseSens);
        return list;
    }

    /**
     * Splits a string into a list of substrings using a specific substring as
     * the delimiter (case sensitive).
     *
     * @param src The source string to split.
     * @param splitter The substring delimiter.
     * @return A list of substrings split by the specified delimiter.
     */
    public default List<String> simpleSplit(String src, String splitter) {
        ArrayList<String> list = new ArrayList<>();
        simpleSplitInto(src, splitter, list, true);
        return list;
    }

    /**
     * Splits a list of strings using a specific substring delimiter.
     *
     * @param srcList The list of strings to process.
     * @param splitter The substring delimiter.
     * @return A new list containing the split values.
     * @param caseSens The search might be case-sensitive? False == src and
     * splitter are converted to lowercase first.
     */
    public default List<String> simpleSplit(List<String> srcList, String splitter, boolean caseSens) {
        ArrayList<String> list = new ArrayList<>();
        for (String src : srcList) {
            if (src == null) {
                continue;
            }
            simpleSplitInto(src, splitter, list, caseSens);
        }
        return list;
    }

    /**
     * Splits a list of strings using a specific substring delimiter (case
     * sensitive).
     *
     * @param srcList The list of strings to process.
     * @param splitter The substring delimiter.
     * @return A new list containing the split values.
     */
    public default List<String> simpleSplit(List<String> srcList, String splitter) {
        return simpleSplit(srcList, splitter, true);
    }

    /**
     * Splits a string using multiple delimiters from a provided list.
     *
     * @param src The source string to split.
     * @param splitterList A list of delimiter substrings.
     * @return A list of substrings obtained after applying each delimiter
     * sequentially.
     * @param caseSens The search might be case-sensitive? False == src and
     * splitter are converted to lowercase first.
     */
    public default List<String> simpleSplit(String src, List<String> splitterList, boolean caseSens) {
        Iterator<String> it = splitterList.iterator();
        if (!it.hasNext()) {
            ArrayList<String> list = new ArrayList<>();
            list.add(src);
            return list;
        }
        List<String> list = simpleSplit(src, it.next(), caseSens);
        while (it.hasNext()) {
            list = simpleSplit(list, it.next(), caseSens);
        }
        return list;
    }

    /**
     * Splits a string using multiple delimiters from a provided list (case
     * sensitive).
     *
     * @param src The source string to split.
     * @param splitterList A list of delimiter substrings.
     * @return A list of substrings obtained after applying each delimiter
     * sequentially.
     */
    public default List<String> simpleSplit(String src, List<String> splitterList) {
        return simpleSplit(src, splitterList, true);
    }

    /**
     * Splits a string into a provided list using a substring delimiter.
     *
     * @param src The source string.
     * @param splitter The substring delimiter.
     * @param list The list where split values will be stored.
     * @param caseSens The search might be case-sensitive? False == src and
     * splitter are converted to lowercase first.
     */
    default void simpleSplitInto(String src, String splitter,
            ArrayList<String> list, boolean caseSens) {
        if (src == null) {
            return;
        }
        int off = 0;
        int next;
        if (caseSens) {
            while ((next = src.indexOf(splitter, off)) != -1) {
                list.add(src.substring(off, next));
                off = next + splitter.length();
            }
        } else {
            final String src_up = src.toLowerCase();
            final String splitter_up = splitter.toLowerCase();
            while ((next = src_up.indexOf(splitter_up, off)) != -1) {
                list.add(src.substring(off, next));
                off = next + splitter.length();
            }
        }
        // Add remaining segment
        // If no match was found, return all in one,
        list.add(src.substring(off));
    }

    /**
     * Splits a string into a provided list using a substring delimiter (case
     * sensitive).
     *
     * @param src The source string.
     * @param splitter The substring delimiter.
     * @param list The list where split values will be stored.
     */
    default void simpleSplitInto(String src, String splitter, ArrayList<String> list) {
        simpleSplitInto(src, splitter, list, true);
    }

    /**
     * Counts occurrences of a substring within a given string.
     *
     * @param src The source string.
     * @param splitter The substring to count.
     * @return The number of occurrences of the substring.
     */
    default int simpleCount(String src, String splitter) {
        if (src == null) {
            return 0;
        }
        int ret = 0;
        int off = 0;
        int next;
        while ((next = src.indexOf(splitter, off)) != -1) {
            off = next + splitter.length();
            ret++;
        }
        return ret;
    }

    /**
     * Replaces occurrences of a specified character in a string with a given
     * replacement string.
     *
     * @param src The source string to process.
     * @param ch The character to replace.
     * @param wish The replacement string.
     * @return The modified string with replacements applied, or null if the
     * source string is null.
     */
    public default String simpleReplace(String src, char ch, String wish) {
        if (src == null) {
            return null;
        }
        List<String> split = simpleSplit(src, ch);
        return simpleConcat(split, wish);
    }

    /**
     * Replaces occurrences of a specified substring in a string with a given
     * replacement string.
     *
     * @param src The source string to process.
     * @param splitter The substring to replace.
     * @param wish The replacement string.
     * @return The modified string with replacements applied, or null if the
     * source string is null.
     */
    public default String simpleReplace(String src, String splitter, String wish, boolean caseSens) {
        if (src == null) {
            return null;
        }
        List<String> split = simpleSplit(src, splitter, caseSens);
        return simpleConcat(split, wish);
    }

    /**
     * Replaces occurrences of a specified substring in a string with a given
     * replacement string.
     *
     * @param src The source string to process.
     * @param splitter The substring to replace.
     * @param wish The replacement string.
     * @return The modified string with replacements applied, or null if the
     * source string is null.
     */
    public default String simpleReplace(String src, String splitter, String wish) {
        if (src == null) {
            return null;
        }
        List<String> split = simpleSplit(src, splitter, true);
        return simpleConcat(split, wish);
    }

    /**
     * Replaces occurrences of multiple specified substrings in a string with a
     * given replacement string (case sensitive).
     *
     * @param src The source string to process.
     * @param splitterList A list of substrings to replace.
     * @param wish The replacement string.
     * @return The modified string with replacements applied, or null if the
     * source string is null.
     */
    public default String simpleReplace(String src, List<String> splitterList, String wish) {
        if (src == null) {
            return null;
        }
        List<String> split = simpleSplit(src, splitterList, true);
        return simpleConcat(split, wish);
    }

    /**
     * Concatenates a list of strings with a specified separator.
     *
     * @param split The list of strings to concatenate.
     * @param wish The separator to insert between elements.
     * @return A single concatenated string.
     */
    public default String simpleConcat(List<String> split, String wish) {
        Iterator<String> it = split.iterator();
        if (!it.hasNext()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(it.next());
        while (it.hasNext()) {
            final String next = it.next();
            sb.append(wish).append(next == null ? "" : next);
        }
        return sb.toString();
    }

    /**
     * Concatenates an array of strings with a specified separator.
     *
     * @param split The array of strings to concatenate.
     * @param wish The separator to insert between elements.
     * @return A single concatenated string.
     */
    public default String simpleConcat(String[] split, String wish) {
        return simpleConcat(Arrays.asList(split), wish);
    }

    /**
     * Formats a string into multiple lines, ensuring that each line does not
     * exceed the specified width. The method attempts to break lines at spaces
     * when possible.
     *
     * @param substring The input string to format.
     * @param width The maximum width of each line.
     * @return A formatted block of text with line breaks.
     */
    public default String simpleBlock(String substring, int width) {
        List<String> lines = simpleSplit(substring, '\n');
        List<String> ret = new ArrayList<>();
        int width90 = width - width / 10;

        for (String line : lines) {
            String rest = line;
            while (rest.length() > width) {
                int index = rest.indexOf(' ', width90);
                if (index > 0) {
                    ret.add(rest.substring(0, index));
                    rest = rest.substring(index);
                } else {
                    ret.add(rest);
                    rest = "";
                }
            }
            if (!rest.isEmpty()) {
                ret.add(rest);
            }
        }
        return simpleConcat(ret, "\n");
    }

}
