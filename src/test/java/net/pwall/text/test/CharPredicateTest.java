/*
 * @(#) CharPredicateTest.java
 *
 * TextMatcher  Text matching functions
 * Copyright (c) 2022 Peter Wall
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.pwall.text.CharPredicate;

public class CharPredicateTest {

    @Test
    public void shouldCreateCharPredicate() {
        CharPredicate predicate = ch -> ch == 'A';
        assertTrue(predicate.test('A'));
        assertFalse(predicate.test('B'));
    }

    @Test
    public void shouldCreatePredicateWithNot() {
        CharPredicate predicate1 = ch -> ch == 'A';
        CharPredicate predicate2 = predicate1.negate();
        assertTrue(predicate2.test('B'));
        assertFalse(predicate2.test('A'));
    }

    @Test
    public void shouldCreateCombinedPredicateWithAnd() {
        CharPredicate predicate1 = ch -> ch >= 'A';
        CharPredicate predicate2 = predicate1.and(ch -> ch <= 'Z');
        assertTrue(predicate2.test('M'));
        assertFalse(predicate2.test('m'));
    }

    @Test
    public void shouldCreateCombinedPredicateWithOr() {
        CharPredicate predicate1 = ch -> ch < 'A';
        CharPredicate predicate2 = predicate1.or(ch -> ch > 'Z');
        assertTrue(predicate2.test('m'));
        assertFalse(predicate2.test('M'));
    }

}
