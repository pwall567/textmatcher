# textmatcher

[![Build Status](https://github.com/pwall567/textmatcher/actions/workflows/build.yml/badge.svg)](https://github.com/pwall567/textmatcher/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/io.jstuff/textmatcher?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.jstuff%22%20AND%20a:%22textmatcher%22)

Text matching functions

## Background

This library is an evolution of the `ParseText` class in the [Java Utilities](https://github.com/pwall567/javautil)
library.
It provides text matching and parsing capabilities to simplify decoding and interpreting text.

## Concepts

The `TextMatcher` object is initialised with a `String`; this is the text to be matched, and it is never modified.
The text may be a single line, or it may be an entire file.

The `TextMatcher` has two index values, which are updated with the results of match operations.
- the `index` points to the current location within the text, and it will be updated as a result of successful match or
  skip operations
- the `start` index points to the start of the most recently matched or skipped characters

An example may help clarify this:
1. a `TextMatcher` is initialised with the string "`apples, pears`"; `index` and `start` are both set to zero
2. a `match` operation testing for "`oranges`" will return `false`; `index` and `start` are unchanged
3. a `match` operation testing for "`apples`" will return `true`; `index` will be set to 6 and `start` will be set to
   zero (the start of the matched sequence)
4. a `match` operation testing for comma will return `true`; `index` will be set to 7 and `start` will be set to 6

### Match Operations

There are a number of match functions, all of which have names beginning with `match`, and all returning a boolean to
indicate success or failure.
They all set the `index` to the position following the last matched character on success, and the `start` to the start
of the matched characters.
If the match is unsuccessful, the `index` and `start` are unchanged.

All match operations will return `false` if there are not sufficient characters left in the string to satisfy the match;
there is no need to check that the index is not at end before comparing.

### Skip Operations

There are also a set of skip operations &ndash; a simpler form of match with no boolean result.
These may be used, for example, to skip over zero or more optional spaces in a line.

The skip operations always set `start` to point to the first character skipped, with `index` incremented beyond the
skipped characters.

### Result Operations

There are several ways of getting a result following a successful match (although obviously, when matching against a
specific string, the result will be already be known).

The result may be obtained as:
- a `String`
- a `CharSequence` (this is slightly more efficient than getting the result as a `String`, for those cases where a
  `CharSequence` is equally useful)
- an `Integer` (converting characters from decimal or hexadecimal)
- a `Long` (converting characters from decimal or hexadecimal)

There is also `getResultLength`, which returns the length of the matched string.

### Utility Operations

In addition, there are a number of general operations to get data from any point in the text, and to get and set the
`index` and `start` values.
When setting these values, the new value must not be negative, and must not exceed the length of the string.

When setting the `index`, if the new value is less than the `start`, the `start` is set to the same value.
Similarly, when setting the `start`, if the new value is greater than the `index`, the `index` is set to the same value.
In other words, the `start` will never be greater than the `index`.

## User Guide

### Constructor

The `TextMatcher` constructor takes a single parameter, the text to be matched.
It will throw an exception if the string is `null`.

### Getters and Setters

There are getters and setters for the `index` and `start` fields:

- `int getIndex()`
- `void setIndex(int index)`
- `int getStart()`
- `void setStart(int start)`

As described above, the `start` may never be set to a value greater than the `index`.

### `match`

The `match` function has three overloaded forms, each performing a match on one or more characters at the current
`index`:

- `boolean match(char ch)`: match a single character
- `boolean match(CharSequence s)`: match a `CharSequence` (for example, a `String`)
- `boolean match(CharPredicate test)`: match a single character using a [`CharPredicate`](#charpredicate) test

### `matchAny`

This function matches a single character at `index` as being any of the characters in a `String`:

- `boolean matchAny(String characters)`

For example, to test whether the character at the `index` is a plus or minus sign:
```java
    if (tm.matchAny("+-")) {
        // the character is a sign; getResultChar() returns the sign character
    }
```

### `matchSeq`

The `matchSeq` function matches a sequence of characters using a [`CharPredicate`](#charpredicate).
This allows testing the characters against arbitrary criteria, as shown in the example below.

- `boolean matchSeq(int max, int min, CharPredicate test)`: match the characters at `index` using a `CharPredicate`
  test, with a specified minimum and maximum (where zero maximum means no limit)
- `boolean matchSeq(int max, CharPredicate test)`: match the characters at `index` using a `CharPredicate` test, with a
  minimum of 1 and a specified maximum (again, zero means no limit)
- `boolean matchSeq(CharPredicate test)`: match the characters at `index` using a `CharPredicate` test, with a minimum
  of 1 and no maximum

For example, to match the characters at the `index` against any number of vowels, returning `true` only if there is at
least one vowel at the current `index`:
```java
    if (tm.matchSeq(ch -> "aeiouAEIOU".indexOf(ch) >= 0)) {
        // match is successful; getResult() will return the matched characters
    }
```

Note that `matchSeq` with a minimum length of zero will always return `true`.

### `matchContinue`

There is often a need to string match operations together.
For example, when matching a computer language identifier, the first character is generally required to be alphabetic,
while the remaining characters may be alphabetic or numeric.
The `matchContinue` function, when invoked following a successful match, will match additional characters using a
[`CharPredicate`](#charpredicate), but on completion, will leave the start index to the location set by the previous
match.
Also, the maximum number of characters limit will include those already found in the previous match.

- `boolean matchContinue(int max, int min, CharPredicate test)`: match the characters at `index` using a
  [`CharPredicate`](#charpredicate) test, with a specified minimum and maximum (where zero maximum means no limit)
- `boolean matchContinue(int max, CharPredicate test)`: match the characters at `index` using a
  [`CharPredicate`](#charpredicate) test, with no minimum and a specified maximum (again, zero means no limit)
- `boolean matchContinue(CharPredicate test)`: match the characters at `index` using a [`CharPredicate`](#charpredicate)
  test, with no minimum or maximum

For example, to match a Java identifier using the `isJavaIdentifierStart` and `isJavaIdentifierPart` functions of the
`Character` class:
```java
    if (tm.match(Character::isJavaIdentifierStart) && tm.matchContinue(Character::isJavaIdentifierPart)) {
        // match is successful; getResult() will return the identifier
    }
```

Like `matchSeq`, a `matchContinue` with a minimum length of zero will always return `true`.
The forms of `matchContinue` that do not take a minimum length parameter use a default of zero, so they will always
return `true`; the only effect is to increment the pointer beyond the characters that match the function.

### `matchDec`

There are three overloaded forms of the `matchDec` (match decimal) function, similar to the three forms of `matchSeq`:

- `boolean matchDec(int max, int min)`: match the characters at `index` as decimal digits, with a specified minimum and
  maximum (where zero maximum means no limit)
- `boolean matchDec(int max)`: match the characters at `index` as decimal digits, with a minimum of 1 and a specified
  maximum (again, zero means no limit)
- `boolean matchDec()`: match the characters at `index` as decimal digits, with a minimum of 1 and no maximum

To match exactly two digits (_e.g._ for a date or time field), use `matchDec(2, 2)`, supplying the same number for
minimum and maximum.

### `matchHex`

There are three overloaded forms of the `matchHex` (match hexadecimal) function, similar to the three forms of
`matchDec`:

- `boolean matchHex(int max, int min)`: match the characters at `index` as hexadecimal digits, with a specified minimum
  and maximum (where zero maximum means no limit)
- `boolean matchHex(int max)`: match the characters at `index` as hexadecimal digits, with a minimum of 1 and a
  specified maximum (again, zero means no limit)
- `boolean matchHex()`: match the characters at `index` as hexadecimal digits, with a minimum of 1 and no maximum

To match exactly four hexadecimal digits, use `matchHex(4, 4)`, supplying the same number for minimum and maximum.

### `skip`

The `skip()` function has two overloaded forms.

- `void skip(char ch)`
- `void skip(CharPredicate test)`

The first takes a single character, and causes the `TextMatcher` to skip any instances of that character:
```java
    tm.skip(' ');
```

The second form skips characters that match a [`CharPredicate`](#charpredicate):
```java
    tm.skip(Character::isWhitespace);
```

### `skipTo`

The `skipTo()` function also has two overloaded forms.

- `void skipTo(char ch)`
- `void skipTo(CharSequence seq)`

The first form skips to the next instance of a nominated character:
```java
    tm.skipTo('\n');
```

The second form skips to the next instance of a string (specified as a `CharSequence`):
```java
    tm.skipTo("*/");
```

Both forms will stop at end of text; if it is important to know whether the target was actually seen, the caller can use
a `match()` operation to check the next character(s), or test `isAtEnd()`.

### `skipFixed`

This skips a fixed number of characters, and throws an exception if the new index would be out of range:

- `void skipFixed(int n)`

### `skipAny`

This skips past any characters in the set represented by the string:

- `void skipAny(String characters)`

For example:
```java
    tm.skipAny(" \t");
```

### `skipToEnd`

This skips to the end of the text:

- `void skipToEnd()`

This may be used, for example, on encountering a "`#`" indicating the start of a comment.

### `getResult`

This gets the result of the most recent match or skip operation as a string:

- `String getResult()`

### `appendResultTo`

This will append the result of the most recent match or skip operation to an `Appendable`, avoiding the need to create
an intermediate `String`.
A separate version is provided for the common case of appending to a `SringBuilder`, since operations on an `Appendable`
may throw `IOException` but those on `StringBuilder` do not.

- `void appendResultTo(Appendable a) throws IOException`
- `void appendResultTo(StringBuilder sb)`

### `appendSubstringTo`

This will append a substring of the text defined by the `from` offset (inclusive) and the `to` offset (exclusive) to an
`Appendable`, avoiding the need to create an intermediate `String`.
As above, a separate version is provided for appending to a `StringBuilder`.

- `void appendSubstringTo(Appendable a, int from, int to) throws IOException`
- `void appendSubstringTo(StringBuilder sb, int from, int to)`

### `getResultChar`

This gets the first character of the result of the most recent match or skip as a character:

- `char getResultChar()`

### `getResultCharSeq`

This gets the first character of the most recent match or skip as a `CharSequence` (this is slightly more efficient than
getting the result as a `String`, for those cases where a `CharSequence` is equally useful):

- `CharSequence getResultCharSeq()`

### `getResultLength`

This gets the length of the most recent match or skip:

- `int getResultLength()`

### `getResultInt`

When a match operation has just matched a string of digits, `getResultInt` will return the value of those digits as an
`int`.
The integer conversion will throw an exception if the value does not fit in an `int`.

There are two forms of `getResultInt`, and the reason requires a little explanation.
The absolute value of the largest negative integer is one greater than the largest positive integer, so if a parsing
operation detected a sign, and then attempted to parse a set of digits following the sign, it would not be able to
handle a string representing `Integer.MIN_VALUE`.
To get around this problem, a version of `getResultInt` allows a sign to be passed as a `boolean` parameter, and if the
parameter is `true` (to indicate negative), the value is calculated by subtracting from zero, allowing values down to
`Integer.MIN_VALUE` to be returned.

- `int getResultInt()`
- `int getResultInt(boolean negative)`

For example:
```java
    boolean negative = tm.matchAny("+-") ? tm.getResultChar == '-' : false;
    if (tm.matchDec()) {
        int value = tm.getResultInt(negative);
        // do something with it...
    }
```

### `getResultLong`

There are similarly two forms of `getResultLong`:

- `long getResultLong()`
- `long getResultLong(boolean negative)`

### `getResultHexInt`

When a match operation has just matched a string of hexadecimal digits, `getResultHexInt` will return the value of those
digits as an `int`.

### `getResultHexLong`

When a match operation has just matched a string of hexadecimal digits, `getResultHexInt` will return the value of those
digits as an `long`.

### `isAtEnd`

The `isAtEnd` function returns `true` when the `index` is at the end of the text:

- `boolean isAtEnd()`

### `revert`

The `revert` function repositions the `index` to the `start` of the most recent match:

- `void revert()`

### `CharPredicate`

The `CharPredicate` interface describes an object which performs a test on a single character, for example, to check
whether is a decimal digit.
Several `TextMatcher` functions take a `CharPredicate` as an argument; this allows matching on any required criteria.

`CharPredicate` is a functional interface, so that lambda constructs may be used in the functions that take it as an
argument.
It includes the default functions `negate`, `and` and `or` for consistency with the standard Java library functions.

## Dependency Specification

The latest version of the library is 3.1, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>io.jstuff</groupId>
      <artifactId>textmatcher</artifactId>
      <version>3.1</version>
    </dependency>
```
### Gradle
```groovy
    testImplementation 'io.jstuff:textmatcher:3.1'
```
### Gradle (kts)
```kotlin
    testImplementation("io.jstuff:textmatcher:3.1")
```

Peter Wall

2025-05-27
