/*
 * @(#) TextMatcherTest.java
 *
 * TextMatcher  Text matching functions
 * Copyright (c) 2021, 2023, 2025 Peter Wall
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

package io.jstuff.text.test;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import io.jstuff.text.TextMatcher;

public class TextMatcherTest {

    @Test
    public void shouldCreateParserAndReturnTextAndStartAndIndex() {
        TextMatcher textMatcher = new TextMatcher("{}");
        assertEquals("{}", textMatcher.getText());
        assertEquals(2, textMatcher.getLength());
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
        assertEquals("", textMatcher.getResult());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void shouldComplainWhenTextIsNull() {
        assertThrows(NullPointerException.class, () -> new TextMatcher(null));
    }

    @Test
    public void shouldCorrectlyReturnAtEnd() {
        TextMatcher textMatcher1 = new TextMatcher("{}");
        assertFalse(textMatcher1.isAtEnd());

        TextMatcher textMatcher2 = new TextMatcher("");
        assertTrue(textMatcher2.isAtEnd());
    }

    @Test
    public void shouldSetStartIndex() {
        TextMatcher textMatcher = new TextMatcher("ACE");
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
        textMatcher.setStart(2);
        assertEquals(2, textMatcher.getStart());
        assertEquals(2, textMatcher.getIndex());
        textMatcher.setStart(1);
        assertEquals(1, textMatcher.getStart());
        assertEquals(2, textMatcher.getIndex());
        assertThrows(IndexOutOfBoundsException.class, () -> textMatcher.setStart(4));
    }

    @Test
    public void shouldSetIndex() {
        TextMatcher textMatcher = new TextMatcher("ACE");
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
        textMatcher.setIndex(2);
        assertEquals(0, textMatcher.getStart());
        assertEquals(2, textMatcher.getIndex());
        textMatcher.setStart(2);
        textMatcher.setIndex(1);
        assertEquals(1, textMatcher.getStart());
        assertEquals(1, textMatcher.getIndex());
        assertThrows(IndexOutOfBoundsException.class, () -> textMatcher.setIndex(4));
    }

    @Test
    public void shouldMatchCharacter() {
        TextMatcher textMatcher = new TextMatcher("{}");
        assertFalse(textMatcher.match('}'));
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
        assertTrue(textMatcher.match('{'));
        assertEquals(0, textMatcher.getStart());
        assertEquals(1, textMatcher.getIndex());
        assertFalse(textMatcher.match('{'));
        assertTrue(textMatcher.match('}'));
        assertEquals(1, textMatcher.getStart());
        assertEquals(2, textMatcher.getIndex());
        assertTrue(textMatcher.isAtEnd());
    }

    @Test
    public void shouldMatchString() {
        TextMatcher textMatcher = new TextMatcher("Hello world");
        assertFalse(textMatcher.match("world"));
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
        assertTrue(textMatcher.match("Hello"));
        assertEquals(0, textMatcher.getStart());
        assertEquals(5, textMatcher.getIndex());
        textMatcher.revert();
        assertEquals(0, textMatcher.getIndex());
    }

    @Test
    public void shouldMatchAnyCharacter() {
        TextMatcher textMatcher = new TextMatcher("{}");
        assertFalse(textMatcher.matchAny("[]()"));
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
        assertTrue(textMatcher.matchAny("{}"));
        assertEquals(0, textMatcher.getStart());
        assertEquals(1, textMatcher.getIndex());
        assertEquals('{', textMatcher.getResultChar());
        assertFalse(textMatcher.match('{'));
        assertTrue(textMatcher.match('}'));
        assertEquals(1, textMatcher.getStart());
        assertEquals(2, textMatcher.getIndex());
        assertTrue(textMatcher.isAtEnd());
    }

    @Test
    public void shouldSkipCharactersInSet() {
        TextMatcher textMatcher1 = new TextMatcher("   {}");
        textMatcher1.skipAny(" \t\n\r");
        assertEquals(0, textMatcher1.getStart());
        assertEquals(3, textMatcher1.getIndex());
        assertEquals("   ", textMatcher1.getResult());
        assertTrue(textMatcher1.match('{'));
        assertFalse(textMatcher1.isAtEnd());

        TextMatcher textMatcher2 = new TextMatcher("54321A");
        textMatcher2.skipAny("0123456789");
        assertEquals(0, textMatcher2.getStart());
        assertEquals(5, textMatcher2.getIndex());
        assertEquals("54321", textMatcher2.getResult());
        assertTrue(textMatcher2.match('A'));
        assertTrue(textMatcher2.isAtEnd());
    }

    @Test
    public void shouldSkipCharactersByValue() {
        TextMatcher textMatcher = new TextMatcher("   {}");
        textMatcher.skip(' ');
        assertEquals(0, textMatcher.getStart());
        assertEquals(3, textMatcher.getIndex());
        assertEquals("   ", textMatcher.getResult());
        textMatcher.skip(' ');
        assertEquals(3, textMatcher.getStart());
        assertEquals(3, textMatcher.getIndex());
        assertTrue(textMatcher.match('{'));
        assertFalse(textMatcher.isAtEnd());
    }

    @Test
    public void shouldSkipToCharacter() {
        TextMatcher textMatcher = new TextMatcher("//   \n");
        assertTrue(textMatcher.match("//"));
        textMatcher.skipTo('\n');
        assertEquals(2, textMatcher.getStart());
        assertEquals(5, textMatcher.getIndex());
        assertEquals("   ", textMatcher.getResult());
        assertFalse(textMatcher.isAtEnd());
    }

    @Test
    public void shouldSkipToString() {
        TextMatcher textMatcher = new TextMatcher("/*****/");
        assertTrue(textMatcher.match("/*"));
        textMatcher.skipTo("*/");
        assertEquals(2, textMatcher.getStart());
        assertEquals(5, textMatcher.getIndex());
        assertEquals("***", textMatcher.getResult());
        assertFalse(textMatcher.isAtEnd());

        textMatcher.setIndex(2);
        textMatcher.skipTo("**%");
        assertEquals(2, textMatcher.getStart());
        assertTrue(textMatcher.isAtEnd());

        textMatcher.setIndex(2);
        textMatcher.skipTo("");
        assertEquals(2, textMatcher.getStart());
        assertEquals(2, textMatcher.getIndex());

        textMatcher.setIndex(2);
        textMatcher.skipTo("/");
        assertEquals(2, textMatcher.getStart());
        assertEquals(6, textMatcher.getIndex());
    }

    @Test
    public void shouldSkipCharactersByPredicate() {
        TextMatcher textMatcher1 = new TextMatcher("   {}");
        textMatcher1.skip(ch -> ch == ' ');
        assertEquals(0, textMatcher1.getStart());
        assertEquals(3, textMatcher1.getIndex());
        assertEquals("   ", textMatcher1.getResult());
        assertTrue(textMatcher1.match('{'));
        assertFalse(textMatcher1.isAtEnd());

        TextMatcher textMatcher2 = new TextMatcher("54321A");
        textMatcher2.skip(ch -> ch >= '0' && ch <= '9');
        assertEquals(0, textMatcher2.getStart());
        assertEquals(5, textMatcher2.getIndex());
        assertEquals("54321", textMatcher2.getResult());
        assertTrue(textMatcher2.match('A'));
        assertTrue(textMatcher2.isAtEnd());
    }

    @Test
    public void shouldGetNominatedCharacter() {
        TextMatcher textMatcher = new TextMatcher("ace");
        assertEquals('e', textMatcher.getChar(2));
        assertEquals('a', textMatcher.getChar(0));
        assertEquals('c', textMatcher.getChar(1));
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
        assertThrows(IndexOutOfBoundsException.class, () -> textMatcher.getChar(3));
        assertThrows(IndexOutOfBoundsException.class, () -> textMatcher.getChar(-1));
    }

    @Test
    public void shouldGetSingleCharacter() {
        TextMatcher textMatcher = new TextMatcher("ace");
        assertEquals('a', textMatcher.nextChar());
        assertEquals('c', textMatcher.nextChar());
        assertEquals('e', textMatcher.nextChar());
        assertTrue(textMatcher.isAtEnd());
    }

    @Test
    public void shouldGetSubstring() {
        TextMatcher textMatcher = new TextMatcher("Hello, world!");
        assertEquals("lo", textMatcher.getString(3, 5));
        assertEquals("or", textMatcher.getString(8, 10));
        assertEquals("or", textMatcher.getCharSeq(8, 10).toString());
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
    }

    @Test
    public void shouldThrowExceptionWhenStringCoordinatesWrong() {
        TextMatcher textMatcher = new TextMatcher("Hello, world!");
        assertThrows(StringIndexOutOfBoundsException.class, () -> textMatcher.getString(3, 2));
        assertThrows(StringIndexOutOfBoundsException.class, () -> textMatcher.getString(3, 14));
        assertThrows(StringIndexOutOfBoundsException.class, () -> textMatcher.getString(-1, 6));
    }

    @Test
    public void shouldSkipToEnd() {
        TextMatcher textMatcher = new TextMatcher("Hello, world!");
        assertTrue(textMatcher.match("Hello"));
        textMatcher.skipToEnd();
        assertEquals(", world!", textMatcher.getResult());
    }

    @Test
    public void shouldSkipFixedAmount() {
        TextMatcher textMatcher = new TextMatcher("Hello, world!");
        assertTrue(textMatcher.match("Hello"));
        textMatcher.skipFixed(2);
        assertEquals(", ", textMatcher.getResult());
    }

    @Test
    public void shouldMatchSequenceOfCharacters() {
        TextMatcher textMatcher = new TextMatcher("Hello, world!");
        assertTrue(textMatcher.matchSeq(20, Character::isAlphabetic));
        assertEquals(0, textMatcher.getStart());
        assertEquals(5, textMatcher.getIndex());
        assertTrue(textMatcher.match(','));
        assertTrue(textMatcher.match(' '));
        assertTrue(textMatcher.matchSeq(2, 2, Character::isLowerCase));
        assertEquals(7, textMatcher.getStart());
        assertEquals(9, textMatcher.getIndex());
        assertTrue(textMatcher.matchSeq(ch -> ch >= 'a' && ch <= 'z'));
        assertEquals(9, textMatcher.getStart());
        assertEquals(12, textMatcher.getIndex());
        assertEquals(3, textMatcher.getResultLength());
        assertTrue(textMatcher.matchSeq(0, 0, Character::isDigit)); // minimum zero always returns true
    }

    @Test
    public void shouldMatchDecimalDigits() {
        TextMatcher textMatcher1 = new TextMatcher("123456");
        assertTrue(textMatcher1.matchDec(8, 1));
        assertEquals(0, textMatcher1.getStart());
        assertEquals(6, textMatcher1.getIndex());

        TextMatcher textMatcher2 = new TextMatcher("123456.");
        assertTrue(textMatcher2.matchDec());
        assertEquals(0, textMatcher2.getStart());
        assertEquals(6, textMatcher2.getIndex());

        TextMatcher textMatcher3 = new TextMatcher("123456");
        assertTrue(textMatcher3.matchDec(4));
        assertEquals(0, textMatcher3.getStart());
        assertEquals(4, textMatcher3.getIndex());

        TextMatcher textMatcher4 = new TextMatcher("x123456");
        assertFalse(textMatcher4.matchDec());

        TextMatcher textMatcher5 = new TextMatcher("123-456");
        assertFalse(textMatcher5.matchDec(4, 4));
    }

    @Test
    public void shouldGetResultString() {
        TextMatcher textMatcher = new TextMatcher("12345ABC");
        assertTrue(textMatcher.matchDec());
        assertEquals(0, textMatcher.getStart());
        assertEquals(5, textMatcher.getIndex());
        assertEquals("12345", textMatcher.getResult());
        assertEquals("12345", textMatcher.getResultCharSeq().toString());
    }

    @Test
    public void shouldAppendResultToAppendable() throws IOException {
        TextMatcher textMatcher = new TextMatcher("12345ABC");
        assertTrue(textMatcher.matchDec());
        assertEquals(0, textMatcher.getStart());
        assertEquals(5, textMatcher.getIndex());
        StringWriter sw = new StringWriter();
        textMatcher.appendResultTo(sw);
        assertEquals("12345", sw.toString());
    }

    @Test
    public void shouldAppendResultToStringBuilder() {
        TextMatcher textMatcher = new TextMatcher("12345ABC");
        assertTrue(textMatcher.matchDec());
        assertEquals(0, textMatcher.getStart());
        assertEquals(5, textMatcher.getIndex());
        StringBuilder sb = new StringBuilder();
        textMatcher.appendResultTo(sb);
        assertEquals("12345", sb.toString());
    }

    @Test
    public void shouldAppendSubstringToAppendable() throws IOException {
        TextMatcher textMatcher = new TextMatcher("12345ABC");
        StringWriter sw = new StringWriter();
        textMatcher.appendSubstringTo(sw, 2, 6);
        assertEquals("345A", sw.toString());
    }

    @Test
    public void shouldAppendSubstringToStringBuilder() {
        TextMatcher textMatcher = new TextMatcher("12345ABC");
        StringBuilder sb = new StringBuilder();
        textMatcher.appendSubstringTo(sb, 3, 6);
        assertEquals("45A", sb.toString());
    }

    @Test
    public void shouldConvertDecimalResult() {
        TextMatcher textMatcher = new TextMatcher("-123456");
        assertEquals(-123456, textMatcher.getInt(1, 7, true));
        assertEquals(-123456L, textMatcher.getLong(1, 7, true));
        assertTrue(textMatcher.match('-'));
        assertTrue(textMatcher.matchDec());
        assertEquals(-123456, textMatcher.getResultInt(true));
        assertEquals(-123456L, textMatcher.getResultLong(true));
        TextMatcher maxInt = new TextMatcher("2147483647");
        assertEquals(Integer.MAX_VALUE, maxInt.getInt(0, 10));
        TextMatcher maxIntPlus1 = new TextMatcher("2147483648");
        assertThrows(NumberFormatException.class, () -> maxIntPlus1.getInt(0, 10));
        TextMatcher minInt = new TextMatcher("-2147483648");
        assertEquals(Integer.MIN_VALUE, minInt.getInt(1, 11, true));
        TextMatcher minIntMinus1 = new TextMatcher("-2147483649");
        assertThrows(NumberFormatException.class, () -> minIntMinus1.getInt(1, 11, true));
        TextMatcher maxLong = new TextMatcher("9223372036854775807");
        assertEquals(Long.MAX_VALUE, maxLong.getLong(0, 19));
        TextMatcher maxLongPlus1 = new TextMatcher("9223372036854775808");
        assertThrows(NumberFormatException.class, () -> maxLongPlus1.getLong(0, 19));
        TextMatcher minLong = new TextMatcher("-9223372036854775808");
        assertEquals(Long.MIN_VALUE, minLong.getLong(1, 20, true));
        TextMatcher minLongMinus1 = new TextMatcher("-9223372036854775809");
        assertThrows(NumberFormatException.class, () -> minLongMinus1.getInt(1, 20, true));
    }

    @Test
    public void shouldConvertSignedDecimalResult() {
        TextMatcher textMatcher = new TextMatcher("123456");
        assertEquals(123456, textMatcher.getInt(0, 6));
        assertEquals(123456L, textMatcher.getLong(0, 6));
        assertEquals(234, textMatcher.getInt(1, 4));
        assertTrue(textMatcher.matchDec(4, 1));
        assertEquals(1234, textMatcher.getResultInt());
        assertTrue(textMatcher.matchDec(0, 1));
        assertEquals(56, textMatcher.getResultLong());
    }

    @Test
    public void shouldMatchHexDigits() {
        TextMatcher textMatcher1 = new TextMatcher("123abc");
        assertTrue(textMatcher1.matchHex(8, 1));
        assertEquals(0, textMatcher1.getStart());
        assertEquals(6, textMatcher1.getIndex());

        TextMatcher textMatcher2 = new TextMatcher("123abc.");
        assertTrue(textMatcher2.matchHex());
        assertEquals(0, textMatcher2.getStart());
        assertEquals(6, textMatcher2.getIndex());

        TextMatcher textMatcher3 = new TextMatcher("123abc");
        assertTrue(textMatcher3.matchHex(4));
        assertEquals(0, textMatcher3.getStart());
        assertEquals(4, textMatcher3.getIndex());

        TextMatcher textMatcher4 = new TextMatcher("x123abc");
        assertFalse(textMatcher4.matchHex());

        TextMatcher textMatcher5 = new TextMatcher("123-abc");
        assertFalse(textMatcher5.matchHex(4, 4));
    }

    @Test
    public void shouldConvertHexResult() {
        TextMatcher textMatcher = new TextMatcher("123abc");
        assertEquals(0x123abc, textMatcher.getHexInt(0, 6));
        assertEquals(0x23a, textMatcher.getHexInt(1, 4));
        assertTrue(textMatcher.matchHex(4, 1));
        assertEquals(0x123a, textMatcher.getResultHexInt());
        assertTrue(textMatcher.matchHex(0, 1));
        assertEquals(0xbc, textMatcher.getResultHexLong());
    }

    @Test
    public void shouldMatchContinuation() {
        TextMatcher textMatcher = new TextMatcher("abc123.x");
        assertTrue(textMatcher.match(Character::isJavaIdentifierStart) &&
                textMatcher.matchContinue(Character::isJavaIdentifierPart));
        assertEquals("abc123", textMatcher.getResult());
        assertTrue(textMatcher.match('.'));
        assertTrue(textMatcher.match(Character::isJavaIdentifierStart) &&
                textMatcher.matchContinue(Character::isJavaIdentifierPart));
        assertEquals("x", textMatcher.getResult());
        textMatcher = new TextMatcher("abc123.x");
        assertTrue(textMatcher.match(Character::isJavaIdentifierStart));
        assertTrue(textMatcher.matchContinue(4, Character::isJavaIdentifierPart));
        assertEquals("abc12", textMatcher.getResult());
        assertTrue(textMatcher.match('3'));
    }

    @Test
    public void shouldLeaveIndexAtStartIfMatchContinueFails() {
        TextMatcher textMatcher = new TextMatcher("%20%AX");
        assertTrue(textMatcher.match('%') && textMatcher.matchContinue(2, 2, TextMatcher::isHexDigit));
        assertEquals(0, textMatcher.getStart());
        assertEquals(3, textMatcher.getIndex());
        assertFalse(textMatcher.match('%') && textMatcher.matchContinue(2, 2, TextMatcher::isHexDigit));
        assertEquals(3, textMatcher.getIndex());
    }

}
