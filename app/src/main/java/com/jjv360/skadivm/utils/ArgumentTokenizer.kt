package com.jjv360.skadivm.utils

import java.util.LinkedList

// From: https://sourceforge.net/p/drjava/git_repo/ci/master/tree/drjava/src/edu/rice/cs/util/ArgumentTokenizer.java

/**
 * Utility class which can tokenize a String into a list of String arguments,
 * with behavior similar to parsing command line arguments to a program.
 * Quoted Strings are treated as single arguments, and escaped characters
 * are translated so that the tokenized arguments have the same meaning.
 * Since all methods are static, the class is declared abstract to prevent
 * instantiation.
 * @version $Id$
 */
object ArgumentTokenizer {
    private const val NO_TOKEN_STATE = 0
    private const val NORMAL_TOKEN_STATE = 1
    private const val SINGLE_QUOTE_STATE = 2
    private const val DOUBLE_QUOTE_STATE = 3

    /** Tokenizes the given String into String tokens.
     * @param arguments A String containing one or more command-line style arguments to be tokenized.
     * @param stringify whether or not to include escape special characters
     * @return A list of parsed and properly escaped arguments.
     */
    /** Tokenizes the given String into String tokens
     * @param arguments A String containing one or more command-line style arguments to be tokenized.
     * @return A list of parsed and properly escaped arguments.
     */
    @JvmOverloads
    fun tokenize(arguments: String, stringify: Boolean = false): List<String> {
        val argList = LinkedList<String>()
        var currArg = StringBuilder()
        var escaped = false
        var state = NO_TOKEN_STATE // start in the NO_TOKEN_STATE
        val len = arguments.length


        // Loop over each character in the string
        var i = 0
        while (i < len) {
            val c = arguments[i]
            if (escaped) {
                // Escaped state: just append the next character to the current arg.
                escaped = false
                currArg.append(c)
            } else {
                when (state) {
                    SINGLE_QUOTE_STATE -> if (c == '\'') {
                        // Seen the close quote; continue this arg until whitespace is seen
                        state = NORMAL_TOKEN_STATE
                    } else {
                        currArg.append(c)
                    }

                    DOUBLE_QUOTE_STATE -> if (c == '"') {
                        // Seen the close quote; continue this arg until whitespace is seen
                        state = NORMAL_TOKEN_STATE
                    } else if (c == '\\') {
                        // Look ahead, and only escape quotes or backslashes
                        i++
                        val next = arguments[i]
                        if (next == '"' || next == '\\') {
                            currArg.append(next)
                        } else {
                            currArg.append(c)
                            currArg.append(next)
                        }
                    } else {
                        currArg.append(c)
                    }

                    NO_TOKEN_STATE, NORMAL_TOKEN_STATE -> when (c) {
                        '\\' -> {
                            escaped = true
                            state = NORMAL_TOKEN_STATE
                        }

                        '\'' -> state = SINGLE_QUOTE_STATE
                        '"' -> state = DOUBLE_QUOTE_STATE
                        else -> if (!Character.isWhitespace(c)) {
                            currArg.append(c)
                            state = NORMAL_TOKEN_STATE
                        } else if (state == NORMAL_TOKEN_STATE) {
                            // Whitespace ends the token; start a new one
                            argList.add(currArg.toString())
                            currArg = StringBuilder()
                            state = NO_TOKEN_STATE
                        }
                    }

                    else -> throw IllegalStateException("ArgumentTokenizer state $state is invalid!")
                }
            }
            i++
        }


        // If we're still escaped, put in the backslash
        if (escaped) {
            currArg.append('\\')
            argList.add(currArg.toString())
        } else if (state != NO_TOKEN_STATE) {
            argList.add(currArg.toString())
        }
        // Format each argument if we've been told to stringify them
        if (stringify) {
            for (i in argList.indices) {
                argList[i] = "\"" + _escapeQuotesAndBackslashes(argList[i]) + "\""
            }
        }
        return argList
    }

    /** Inserts backslashes before any occurrences of a backslash or
     * quote in the given string.  Also converts any special characters
     * appropriately.
     */
    internal fun _escapeQuotesAndBackslashes(s: String): String {
        val buf = StringBuilder(s)


        // Walk backwards, looking for quotes or backslashes.
        //  If we see any, insert an extra backslash into the buffer at
        //  the same index.  (By walking backwards, the index into the buffer
        //  will remain correct as we change the buffer.)
        for (i in s.length - 1 downTo 0) {
            val c = s[i]
            if ((c == '\\') || (c == '"')) {
                buf.insert(i, '\\')
            } else if (c == '\n') {
                buf.deleteCharAt(i)
                buf.insert(i, "\\n")
            } else if (c == '\t') {
                buf.deleteCharAt(i)
                buf.insert(i, "\\t")
            } else if (c == '\r') {
                buf.deleteCharAt(i)
                buf.insert(i, "\\r")
            } else if (c == '\b') {
                buf.deleteCharAt(i)
                buf.insert(i, "\\b")
            }/* else if (c == '\f') {
                buf.deleteCharAt(i)
                buf.insert(i, "\\f")
            }*/
        }
        return buf.toString()
    }
}