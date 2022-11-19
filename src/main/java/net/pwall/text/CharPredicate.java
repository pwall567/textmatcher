/*
 * @(#) CharPredicate.java
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

package net.pwall.text;

import java.util.Objects;

/**
 * A functional interface to test a character value.  The default functions {@code negate}, {@code and} and {@code or}
 * are included for consistency with the standard Java library functions.
 *
 * @author  Peter Wall
 */
@FunctionalInterface
public interface CharPredicate {

    /**
     * Evaluate the predicate against the given argument.
     *
     * @param   value   the {@code char} value
     * @return          {@code true} iff the predicate matches the argument
     */
    boolean test(char value);

    /**
     * Create a predicate representing the logical NOT of this predicate.
     *
     * @return          a predicate that returns {@code true} iff this predicate does NOT match the value
     */
    default CharPredicate negate() {
        return (value) -> !test(value);
    }

    /**
     * Create a composed predicate combining this predicate and another testing the same value in an AND relationship.
     *
     * @param   other   the other {@code CharPredicate}
     * @return          a predicate that returns {@code true} iff this predicate AND the other predicate match the value
     */
    default CharPredicate and(CharPredicate other) {
        Objects.requireNonNull(other);
        return (value) -> test(value) && other.test(value);
    }

    /**
     * Create a composed predicate combining this predicate and another testing the same value in an OR relationship.
     *
     * @param   other   the other {@code CharPredicate}
     * @return          a predicate that returns {@code true} iff this predicate OR the other predicate match the value
     */
    default CharPredicate or(CharPredicate other) {
        Objects.requireNonNull(other);
        return (value) -> test(value) || other.test(value);
    }

}
