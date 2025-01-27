/*
 * @(#) TextMatcher.java
 *
 * TextMatcher  Text matching functions
 * Copyright (c) 2021, 2022, 2024, 2025 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.jstuff.text;

import java.io.IOException;

/**
 * A text matching class to help with parsing text strings.  It maintains a current pointer within a string and updates
 * this pointer on a successful match.
 *
 * <p>The {@code TextMatcher} has four main types of functions:</p>
 * <dl>
 *     <dt>{@code Match} functions</dt>
 *     <dd>These test the characters at the current index, and if successful, update the start index and the current
 *     index to reflect the matched characters</dd>
 *     <dt>{@code Skip} functions</dt>
 *     <dd>These advance the current pointer past one or more or a specified type of character (e.g. whitespace)</dd>
 *     <dt>{@code GetResult} functions</dt>
 *     <dd>These get the result of the last match in a number of forms</dd>
 *     <dt>General {@code Get} functions</dt>
 *     <dd>These get arbitrary data from the string</dd>
 * </dl>
 *
 * @author  Peter Wall
 */
public class TextMatcher {

    private static final int MAX_INT_MASK = 0xF << 28;
    private static final long MAX_LONG_MASK = ((long)0xF) << 60;

    private static final byte[] hexValues = new byte[] {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
             0,  1,  2,  3,  4,  5,  6,  7,  8,  9, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private final String text;
    private final int length;
    private int start;
    private int index;

    /**
     * Construct a {@code TextMatcher} with the specified text.
     *
     * @param   text        the text
     * @throws  NullPointerException    if the text is {@code null}
     */
    public TextMatcher(String text) {
        if (text == null)
            throw new NullPointerException("TextMatcher text must not be null");
        this.text = text;
        length = text.length();
        start = 0;
        index = 0;
    }

    /**
     * Get the entire text.
     *
     * @return      the text
     */
    public String getText() {
        return text;
    }

    /**
     * Get the character at the nominated index.
     *
     * @param   index       the index
     * @return              the character at that index
     * @throws  IndexOutOfBoundsException   if the index is invalid
     */
    public char getChar(int index) {
        return text.charAt(index);
    }

    /**
     * Get the length of the entire text.
     *
     * @return              the text length
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the start index (the index of the start of the last matched sequence).
     *
     * @return              the start index
     */
    public int getStart() {
        return start;
    }

    /**
     * Set the start index.  If the current index is less than the new start index, make them equal.
     *
     * @param   start       the new start index
     * @throws  IndexOutOfBoundsException   if the new start index is less than 0 or greater than the text length
     */
    public void setStart(int start) {
        if (start < 0 || start > length)
            throw new IndexOutOfBoundsException(String.valueOf(start));
        this.start = start;
        if (index < start)
            index = start;
    }

    /**
     * Get the current index (the offset within the text).
     *
     * @return              the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the current index.  If the new current index is less than the start index, make them equal.
     *
     * @param   index       the new current index
     * @throws  IndexOutOfBoundsException   if the new current index is less than 0 or greater than the text length
     */
    public void setIndex(int index) {
        if (index < 0 || index > length)
            throw new IndexOutOfBoundsException(String.valueOf(index));
        this.index = index;
        if (index < start)
            start = index;
    }

    /**
     * Test whether the {@code TextMatcher} object is exhausted (the index has reached the end of the text).
     *
     * @return          {@code true} if the index has reached the end of the text
     */
    public boolean isAtEnd() {
        return index >= length;
    }

    /**
     * Undo the effect of the last match operation.
     */
    public void revert() {
        index = start;
    }

    /**
     * Match the current character in the text against a given character.  Following a successful match the start index
     * will point to the matched character and the index will be incremented past it.
     *
     * @param   ch      the character to match against
     * @return          {@code true} if the character in the text matches the given character
     */
    public boolean match(char ch) {
        if (index >= length || text.charAt(index) != ch)
            return false;
        start = index++;
        return true;
    }

    /**
     * Match the characters at the index against a given {@link CharSequence} ({@link String}, {@link StringBuilder}
     * etc.).  Following a successful match the start index will point to the first character of the matched sequence
     * and the index will be incremented past it.
     *
     * @param target    the target {@link CharSequence}
     * @return          {@code true} if the characters in the text at the index match the target
     */
    public boolean match(CharSequence target) {
        int len = target.length();
        if (index + len > length)
            return false;
        int i = index;
        for (int j = 0; j < len; j++)
            if (text.charAt(i++) != target.charAt(j))
                return false;
        start = index;
        index = i;
        return true;
    }

    /**
     * Match the current character in the text using the specified comparison function.  Following a successful match
     * the start index will point to the matched character and the index will be incremented past it.
     *
     * @param   comparison  the comparison function
     * @return              {@code true} if the character in the text matches using the comparison function
     */
    public boolean match(CharPredicate comparison) {
        if (index >= length || !comparison.test(text.charAt(index)))
            return false;
        start = index++;
        return true;
    }

    /**
     * Match the current character in the text against any of the characters in a given {@link String}.  Following a
     * successful match the start index will point to the matched character and the index will be incremented past it.
     *
     * @param   any     the characters to match against (as a {@link String})
     * @return          {@code true} if the character in the text at the index matches any of the characters in the
     *                  string
     */
    public boolean matchAny(String any) {
        if (index >= length)
            return false;
        if (any.indexOf(text.charAt(index)) < 0)
            return false;
        start = index++;
        return true;
    }

    /**
     * Match the characters at the index using the specified comparison function, with a given minimum number of
     * characters and an optional maximum.  To match a fixed number of characters, the maximum and minimum should be set
     * to the same value.
     *
     * @param   maxChars    the maximum number of characters to match (or 0 to indicate no limit)
     * @param   minChars    the minimum number of characters for a successful match
     * @param   comparison  the comparison function
     * @return              {@code true} if the characters in the text at the index satisfy the comparison function
     *                      (subject to the specified minimum and maximum number of characters)
     */
    public boolean matchSeq(int maxChars, int minChars, CharPredicate comparison) {
        int i = index;
        int stopper = maxChars > 0 ? Math.min(length, i + maxChars) : length;
        while (i < stopper && comparison.test(text.charAt(i)))
            i++;
        if (i - index < minChars)
            return false;
        start = index;
        index = i;
        return true;
    }

    /**
     * Match the characters at the index using the specified comparison function, with a minimum of 1 character and an
     * optional maximum.
     *
     * @param   maxChars    the maximum number of characters to match (or 0 to indicate no limit)
     * @param   comparison  the comparison function
     * @return              {@code true} if one or more characters in the text at the index satisfy the comparison
     *                      function (subject to the specified maximum number of characters)
     */
    public boolean matchSeq(int maxChars, CharPredicate comparison) {
        return matchSeq(maxChars, 1, comparison);
    }

    /**
     * Match the characters at the index using the specified comparison function, with a minimum of 1 character and no
     * maximum.
     *
     * @param   comparison  the comparison function
     * @return              {@code true} if one or more characters in the text at the index satisfy the comparison
     *                      function
     */
    public boolean matchSeq(CharPredicate comparison) {
        return matchSeq(0, 1, comparison);
    }

    /**
     * Match the characters at the index as decimal digits, with a given minimum number of digits and an optional
     * maximum.  To match a fixed number of digits, the maximum and minimum should be set to the same value.
     *
     * @param   maxDigits   the maximum number of digits to match (or 0 to indicate no limit)
     * @param   minDigits   the minimum number of digits for a successful match
     * @return              {@code true} if the characters in the text at the index are decimal digits (subject to the
     *                      specified minimum and maximum number of digits)
     */
    public boolean matchDec(int maxDigits, int minDigits) {
        return matchSeq(maxDigits, minDigits, TextMatcher::isDigit);
    }

    /**
     * Match the characters at the index as decimal digits, with a minimum of 1 digit and an optional maximum.
     *
     * @param   maxDigits   the maximum number of digits to match (or 0 to indicate no limit)
     * @return              {@code true} if one or more characters in the text at the index are decimal digits (subject
     *                      to the specified maximum number of digits)
     */
    public boolean matchDec(int maxDigits) {
        return matchSeq(maxDigits, 1, TextMatcher::isDigit);
    }

    /**
     * Match the characters at the index as decimal digits, with a minimum of 1 digit and no maximum.
     *
     * @return              {@code true} if one or more characters in the text at the index are decimal digits
     */
    public boolean matchDec() {
        return matchSeq(0, 1, TextMatcher::isDigit);
    }

    /**
     * Match the characters at the index as hexadecimal digits, with a given minimum number of digits and an optional
     * maximum.  To match a fixed number of digits, the maximum and minimum should be set to the same value.
     *
     * @param   maxDigits   the maximum number of digits to match (or 0 to indicate no limit)
     * @param   minDigits   the minimum number of digits for a successful match
     * @return              {@code true} if the characters in the text at the index are hexadecimal digits (subject to
     *                      the specified minimum and maximum number of digits)
     */
    public boolean matchHex(int maxDigits, int minDigits) {
        return matchSeq(maxDigits, minDigits, TextMatcher::isHexDigit);
    }

    /**
     * Match the characters at the index as hexadecimal digits, with a minimum of 1 digit and an optional maximum.
     *
     * @param   maxDigits   the maximum number of digits to match (or 0 to indicate no limit)
     * @return              {@code true} if one or more characters in the text at the index are hexadecimal digits
     *                      (subject to the specified maximum number of digits)
     */
    public boolean matchHex(int maxDigits) {
        return matchSeq(maxDigits, 1, TextMatcher::isHexDigit);
    }

    /**
     * Match the characters at the index as hexadecimal digits, with a minimum of 1 digit and no maximum.
     *
     * @return              {@code true} if one or more characters in the text at the index are hexadecimal digits
     */
    public boolean matchHex() {
        return matchSeq(0, 1, TextMatcher::isHexDigit);
    }

    /**
     * Match the characters at the index as a continuation using the specified comparison function, with a given minimum
     * number of characters and an optional maximum, but do not set the start index on success, and on fail, set the
     * index back to the start index (as if the original match had failed).  To match a fixed number of characters, the
     * maximum and minimum should be set to the same value.
     *
     * @param   maxChars    the maximum number of characters to match (or 0 to indicate no limit)
     * @param   minChars    the minimum number of characters for a successful match
     * @param   comparison  the comparison function
     * @return              {@code true} if the characters in the text at the index satisfy the comparison function
     *                      (subject to the specified minimum and maximum number of characters)
     */
    public boolean matchContinue(int maxChars, int minChars, CharPredicate comparison) {
        int i = index;
        int stopper = maxChars > 0 ? Math.min(length, i + maxChars) : length;
        while (i < stopper && comparison.test(text.charAt(i)))
            i++;
        if (i - index < minChars) {
            index = start;
            return false;
        }
        index = i;
        return true;
    }

    /**
     * Match the characters at the index as a continuation using the specified comparison function, with no minimum
     * number of characters and an optional maximum, but do not set the start index on success, and on fail, set the
     * index back to the start index (as if the original match had failed).
     *
     * @param   maxChars    the maximum number of characters to match (or 0 to indicate no limit)
     * @param   comparison  the comparison function
     * @return              {@code true} (with a minimum of zero the function can not fail; the only effect is to set
     *                      the index past the matching characters)
     */
    public boolean matchContinue(int maxChars, CharPredicate comparison) {
        return matchContinue(maxChars, 0, comparison);
    }

    /**
     * Match the characters at the index as a continuation using the specified comparison function, with no minimum or
     * maximum number of characters, but do not set the start index on success, and on fail, set the index back to the
     * start index (as if the original match had failed).
     *
     * @param   comparison  the comparison function
     * @return              {@code true} (with a minimum of zero the function can not fail; the only effect is to set
     *                      the index past any matching characters)
     */
    public boolean matchContinue(CharPredicate comparison) {
        return matchContinue(0, 0, comparison);
    }

    /**
     * Increment the index past any of the characters in a given string.
     *
     * @param   any     the characters to be skipped, as a {@link String}
     */
    public void skipAny(String any) {
        start = index;
        while (index < length && any.indexOf(text.charAt(index)) >= 0)
            index++;
    }

    /**
     * Increment the index past any instances of the given character.
     *
     * @param   ch      the character to be skipped
     */
    public void skip(Character ch) {
        start = index;
        while (index < length && text.charAt(index) == ch)
            index++;
    }

    /**
     * Increment the index past any characters matching a given comparison function.
     *
     * @param   comparison  the comparison function
     */
    public void skip(CharPredicate comparison) {
        start = index;
        while (index < length && comparison.test(text.charAt(index)))
            index++;
    }

    /**
     * Increment the index to the next instance of the given character, or to end of the text if character not found.
     * If it is important to know whether the character has been found, a {@link #match(char)} function may be used.
     *
     * @param   ch      the character to be skipped to
     */
    public void skipTo(Character ch) {
        start = index;
        while (index < length && text.charAt(index) != ch)
            index++;
    }

    /**
     * Increment the index to the next instance of the given {@link CharSequence} ({@link String}, {@link StringBuilder}
     * etc.), or to end of the text if target not found.  If it is important to know whether the target has been found,
     * a {@link #match(CharSequence)} function may be used.
     *
     * @param   target  the string to be skipped to
     */
    public void skipTo(CharSequence target) {
        start = index;
        int targetLength = target.length();
        if (targetLength == 0)
            return;
        if (targetLength == 1)
            skipTo(target.charAt(0));
        else {
            char firstChar = target.charAt(0);
            int stopper = length - targetLength;
            while (index <= stopper) {
                if (text.charAt(index) == firstChar) {
                    int i = index + 1;
                    int j = 1; // we know there will be at least one additional character
                    while (text.charAt(i++) == target.charAt(j++)) {
                        if (j == targetLength)
                            return;
                    }
                }
                index++;
            }
            index = length;
        }
    }

    /**
     * Increment the index directly to the end of the text.
     */
    public void skipToEnd() {
        start = index;
        index = length;
    }

    /**
     * Increment the index by a fixed amount.
     *
     * @param   n       the number of characters to skip (must be positive)
     * @throws  IllegalArgumentException if the increment is negative
     * @throws  StringIndexOutOfBoundsException if the incremented index is beyond end of string
     */
    public void skipFixed(int n) {
        if (n < 0)
            throw new IllegalArgumentException(String.valueOf(n));
        int newIndex = index + n;
        if (newIndex > length)
            throw new StringIndexOutOfBoundsException(String.valueOf(newIndex));
        start = index;
        index = newIndex;
    }

    /**
     * Get the character at the current index and increment the index.
     *
     * @return          the current character
     * @throws  StringIndexOutOfBoundsException if the index is at or beyond end of string
     */
    public char nextChar() {
        start = index;
        if (index >= length)
            throw new StringIndexOutOfBoundsException(String.valueOf(index));
        return text.charAt(index++);
    }

    /**
     * Get a substring of the text.
     *
     * @param   start   the start offset
     * @param   end     the end offset (exclusive)
     * @return          the substring
     * @throws  StringIndexOutOfBoundsException if the start offset is lees than zero, the end offset is less than the
     *                                          start offset, or the end offset is greater than the length
     */
    public String getString(int start, int end) {
        return text.substring(start, end);
    }

    /**
     * Get a substring of the text as a {@link CharSequence}.  This will be slightly more efficient than getting a
     * {@link String}, for those cases where a {@link CharSequence} is just as useful.
     *
     * @param   start   the start offset
     * @param   end     the end offset (exclusive)
     * @return          the {@link CharSequence}
     * @throws  IndexOutOfBoundsException   if the start offset is lees than zero, the end offset is less than the
     *                                      start offset, or the end offset is greater than the length
     */
    public CharSequence getCharSeq(int start, int end) {
        if (start < 0 || end > length || end < start)
            throw new IndexOutOfBoundsException(String.valueOf(start) + ':' + end);
        return new CharSeq(text, start, end);
    }

    /**
     * Get the result of the last match operation (or the first character of a longer match) as a single character.
     *
     * @return          the first character of the result of the last match
     * @throws  IndexOutOfBoundsException if the start index at or beyond the end of the text
     */
    public char getResultChar() {
        return text.charAt(start);
    }

    /**
     * Get the result of the last match operation as a {@link String}.
     *
     * @return          the result of the last match
     */
    public String getResult() {
        return text.substring(start, index);
    }

    /**
     * Append the result of the last match to an {@link Appendable}.  This is more efficient than {@link #getResult()}
     * since it doesn't require the creation of an intermediate {@link String}.
     *
     * @param   a       the {@link Appendable}
     * @throws IOException if thrown by the {@link Appendable}
     */
    public void appendResultTo(Appendable a) throws IOException {
        a.append(text, start, index);
    }

    /**
     * Get the result of the last match operation as a {@link CharSequence}.  This will be slightly more efficient than
     * getting a {@link String}, for those cases where a {@link CharSequence} is just as useful.
     *
     * @return          the result of the last match
     */
    public CharSequence getResultCharSeq() {
        return new CharSeq(text, start, index);
    }

    /**
     * Get the length of the result of the last match operation.
     *
     * @return          the length of the result of the last match
     */
    public int getResultLength() {
        return index - start;
    }

    /**
     * Get the result of the last match operation as an {@code int}.
     *
     * @return          the result of the last match as an {@code int} (always positive)
     * @throws  NumberFormatException   if the start and end indices do not describe a valid {@code int}
     */
    public int getResultInt() {
        return getInt(start, index, false);
    }

    /**
     * Get the result of the last match operation as an {@code int}.
     *
     * @param   negative    {@code true} to indicate that the value must be negated
     * @return              the result of the last match as an {@code int} (always positive)
     * @throws  NumberFormatException   if the start and end indices do not describe a valid {@code int}
     */
    public int getResultInt(boolean negative) {
        return getInt(start, index, negative);
    }

    /**
     * Get a positive {@code int} from the text.
     *
     * @param   from    the start offset
     * @param   to      the end offset (exclusive)
     * @return          the {@code int} (always positive)
     * @throws  NumberFormatException       if the start and end indices do not describe a valid {@code int}
     * @throws  IndexOutOfBoundsException   if the start and end indices are not contained within the text
     */
    public int getInt(int from, int to) {
        if (to <= from)
            throw new NumberFormatException();
        int i = from;
        while (i < to && text.charAt(i) == '0') {
            if (++i == to)
                return 0;
        }
        int result = convertDecDigit(text.charAt(i++));
        while (i < to) {
            result = result * 10 + convertDecDigit(text.charAt(i++));
            if (result < 0)
                throw new NumberFormatException();
        }
        return result;
    }

    /**
     * Get a signed {@code int} from the text.
     *
     * @param   from        the start offset
     * @param   to          the end offset (exclusive)
     * @param   negative    {@code true} to indicate that the value must be negated
     * @return              the {@code int}
     * @throws  NumberFormatException       if the start and end indices do not describe a valid {@code int}
     * @throws  IndexOutOfBoundsException   if the start and end indices are not contained within the text
     */
    public int getInt(int from, int to, boolean negative) {
        if (to <= from)
            throw new NumberFormatException();
        int i = from;
        while (i < to && text.charAt(i) == '0') {
            if (++i == to)
                return 0;
        }
        int result = 0;
        if (negative) {
            while (i < to) {
                result = result * 10 - convertDecDigit(text.charAt(i++));
                if (result >= 0)
                    throw new NumberFormatException();
            }
        }
        else {
            while (i < to) {
                result = result * 10 + convertDecDigit(text.charAt(i++));
                if (result < 0)
                    throw new NumberFormatException();
            }
        }
        return result;
    }

    /**
     * Get the result of the last match operation as a {@code long}.
     *
     * @return          the result of the last match as a {@code long} (always positive)
     * @throws  NumberFormatException   if the start and end indices do not describe a valid {@code long}
     */
    public long getResultLong() {
        return getLong(start, index, false);
    }

    /**
     * Get the result of the last match operation as a {@code long}.
     *
     * @param   negative    {@code true} to indicate that the value must be negated
     * @return              the result of the last match as a {@code long} (always positive)
     * @throws  NumberFormatException   if the start and end indices do not describe a valid {@code long}
     */
    public long getResultLong(boolean negative) {
        return getLong(start, index, negative);
    }

    /**
     * Get a positive {@code long} from the text.
     *
     * @param   from    the start offset
     * @param   to      the end offset (exclusive)
     * @return          the {@code long} (always positive)
     * @throws  NumberFormatException       if the start and end indices do not describe a valid {@code long}
     * @throws  IndexOutOfBoundsException   if the start and end indices are not contained within the text
     */
    public long getLong(int from, int to) {
        if (to <= from)
            throw new NumberFormatException();
        int i = from;
        while (i < to && text.charAt(i) == '0') {
            if (++i == to)
                return 0;
        }
        long result = convertDecDigit(text.charAt(i++));
        while (i < to) {
            result = result * 10 + convertDecDigit(text.charAt(i++));
            if (result < 0)
                throw new NumberFormatException();
        }
        return result;
    }

    /**
     * Get a signed {@code long} from the text.
     *
     * @param   from        the start offset
     * @param   to          the end offset (exclusive)
     * @param   negative    {@code true} to indicate that the value must be negated
     * @return              the {@code long}
     * @throws  NumberFormatException       if the start and end indices do not describe a valid {@code long}
     * @throws  IndexOutOfBoundsException   if the start and end indices are not contained within the text
     */
    public long getLong(int from, int to, boolean negative) {
        if (to <= from)
            throw new NumberFormatException();
        int i = from;
        while (i < to && text.charAt(i) == '0') {
            if (++i == to)
                return 0;
        }
        long result = 0;
        if (negative) {
            while (i < to) {
                result = result * 10 - convertDecDigit(text.charAt(i++));
                if (result >= 0)
                    throw new NumberFormatException();
            }
        }
        else {
            while (i < to) {
                result = result * 10 + convertDecDigit(text.charAt(i++));
                if (result < 0)
                    throw new NumberFormatException();
            }
        }
        return result;
    }

    /**
     * Get the result of the last match operation as an unsigned {@code int}, treating the digits as hexadecimal.
     *
     * @return          the result of the last match as an {@code int}
     * @throws  NumberFormatException   if the start and end indices do not describe a valid {@code int}
     */
    public int getResultHexInt() {
        return getHexInt(start, index);
    }

    /**
     * Get an unsigned {@code int} from the text, treating the digits as hexadecimal.
     *
     * @param   from    the start offset
     * @param   to      the end offset (exclusive)
     * @return          the hexadecimal {@code int}
     * @throws  NumberFormatException       if the start and end indices do not describe a valid {@code int}
     * @throws  IndexOutOfBoundsException   if the start and end indices are not contained within the text
     */
    public int getHexInt(int from, int to) {
        if (to <= from)
            throw new NumberFormatException();
        int result = convertHexDigit(text.charAt(from));
        for (int i = from + 1; i < to; i++) {
            if ((result & MAX_INT_MASK) != 0)
                throw new NumberFormatException();
            result = result << 4 | convertHexDigit(text.charAt(i));
        }
        return result;
    }

    /**
     * Get the result of the last match operation as an unsigned {@code long}, treating the digits as hexadecimal.
     *
     * @return          the result of the last match as a {@code long}
     * @throws  NumberFormatException   if the start and end indices do not describe a valid {@code long}
     */
    public long getResultHexLong() {
        return getHexLong(start, index);
    }

    /**
     * Get an unsigned {@code long} from the text, treating the digits as hexadecimal.
     *
     * @param   from    the start offset
     * @param   to      the end offset (exclusive)
     * @return          the hexadecimal {@code long}
     * @throws  NumberFormatException       if the start and end indices do not describe a valid {@code long}
     * @throws  IndexOutOfBoundsException   if the start and end indices are not contained within the text
     */
    public long getHexLong(int from, int to) {
        if (to <= from)
            throw new NumberFormatException();
        long result = convertHexDigit(text.charAt(from));
        for (int i = from + 1; i < to; i++) {
            if ((result & MAX_LONG_MASK) != 0)
                throw new NumberFormatException();
            result = result << 4 | convertHexDigit(text.charAt(i));
        }
        return result;
    }

    /**
     * Test whether the given character is a digit.
     *
     * @param   ch      the character
     * @return          {@code true} if the character is a digit
     */
    public static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Test whether the given character is a hexadecimal digit.
     *
     * @param   ch      the character
     * @return          {@code true} if the character is a hexadecimal digit
     */
    public static boolean isHexDigit(char ch) {
        return ch < 0x80 && hexValues[ch] >= 0;
    }

    /**
     * Convert a decimal digit to the integer value of the digit.
     *
     * @param   ch      the decimal digit
     * @return          the integer value (0 - 9)
     * @throws          NumberFormatException if the digit is not valid
     */
    public static int convertDecDigit(char ch) {
        if (ch >= '0' && ch <= '9')
            return ch - '0';
        throw new NumberFormatException("Illegal decimal digit");
    }

    /**
     * Convert a hexadecimal digit to the integer value of the digit.
     *
     * @param   ch      the hexadecimal digit
     * @return          the integer value (0 - 15)
     * @throws          NumberFormatException if the digit is not valid
     */
    public static int convertHexDigit(char ch) {
        if (ch < 0x80) {
            int hex = hexValues[ch];
            if (hex >= 0)
                return hex;
        }
        throw new NumberFormatException("Illegal hexadecimal digit");
    }

    /**
     * An implementation of {@link CharSequence} to return data from {@code TextMatcher}.
     */
    public static class CharSeq implements CharSequence {

        private final String text;
        private final int start;
        private final int end;

        /**
         * Construct a {@code CharSeq} with the given text, start offset and end offset.  (Package-local constructor
         * limits access to this package, and removes necessity to validate parameters.)
         *
         * @param   text    the underlying text array
         * @param   start   the start offset
         * @param   end     the end offset
         */
        CharSeq(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }

        /**
         * Get the length of the {@code CharSeq}.
         *
         * @return      the length
         */
        @Override
        public int length() {
            return end - start;
        }

        /**
         * Get the character at the given offset.
         *
         * @param   index   the offset
         * @return          the character
         */
        @Override
        public char charAt(int index) {
            if (index >= 0) {
                int i = index + start;
                if (i < end)
                    return text.charAt(i);
            }
            throw new IndexOutOfBoundsException();
        }

        /**
         * Get a sub-sequence of this {@code CharSeq}.
         *
         * @param   start   the start offset
         * @param   end     the end offset
         * @return          the sub-sequence
         */
        @Override
        public CharSequence subSequence(int start, int end) {
            if (start < 0 || end > length() || end < start)
                throw new IndexOutOfBoundsException();
            return new CharSeq(text, start + this.start, end + this.start);
        }

        /**
         * Get the {@link String} representation of this {@code CharSeq}.
         *
         * @return      the string
         */
        @Override
        public String toString() {
            return text.substring(start, end);
        }

    }

}
