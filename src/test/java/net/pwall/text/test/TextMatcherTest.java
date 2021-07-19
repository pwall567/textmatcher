/*
 * @(#) TextMatcherTest.java
 *
 * TextMatcher  Text matching functions
 * Copyright (c) 2021 Peter Wall
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

package net.pwall.text.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.pwall.text.TextMatcher;

public class TextMatcherTest {

    @Test
    public void shouldCreateParserAndReturnStartAndIndex() {
        TextMatcher textMatcher = new TextMatcher("{}");
        assertEquals(2, textMatcher.getLength());
        assertEquals(0, textMatcher.getStart());
        assertEquals(0, textMatcher.getIndex());
        assertEquals("", textMatcher.getResult());
    }

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
    public void shouldSkipCharacters() {
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
    public void shouldMatchSequenceOfCharacters() {
        TextMatcher textMatcher = new TextMatcher("Hello, world!");
        assertTrue(textMatcher.match(20, Character::isAlphabetic));
        assertEquals(0, textMatcher.getStart());
        assertEquals(5, textMatcher.getIndex());
        assertTrue(textMatcher.match(','));
        assertTrue(textMatcher.match(' '));
        assertTrue(textMatcher.match(2, 2, Character::isLowerCase));
        assertEquals(7, textMatcher.getStart());
        assertEquals(9, textMatcher.getIndex());
        assertTrue(textMatcher.match((ch) -> ch >= 'a' && ch <= 'z'));
        assertEquals(9, textMatcher.getStart());
        assertEquals(12, textMatcher.getIndex());
        assertEquals(3, textMatcher.getResultLength());
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
    public void shouldConvertDecimalResult() {
        TextMatcher textMatcher = new TextMatcher("-123456");
        assertEquals(-123456, textMatcher.getInt(1, 7, true));
        assertEquals(-123456L, textMatcher.getLong(1, 7, true));
        assertTrue(textMatcher.match('-'));
        assertTrue(textMatcher.matchDec());
        assertEquals(-123456, textMatcher.getResultInt(true));
        assertEquals(-123456L, textMatcher.getResultLong(true));
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

}
