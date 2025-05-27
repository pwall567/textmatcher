# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [3.1] - 2025-05-27
### Changed
- `TextMatcher`: added `appendSubstringTo(Appendable)` function
- `TextMatcher`: added `appendResultTo(StringBuilder)` and `appendSubstringTo(StringBuilder)` functions
- `pom.xml`: switched to use `jstuff-maven` parent POM

## [3.0] - 2025-01-28
### Added
- `build.yml`, `deploy.yml`: converted project to GitHub Actions
### Changed
- `pom.xml`: moved to `io.jstuff` (package amd Maven group)
- `TextMatcher`: introduced table to speed up hex digit tests and conversions
- `TextMatcher`: added `skip(char)`, `skipTo(char)` and `skipto(CharSequence)` functions
- `TextMatcher`: added `getText()` function
- `TextMatcher`: added `appendResultTo(Appendable)` function
### Removed
- `.travis.yml`

## [2.4] - 2023-12-01
### Changed
- `TextMatcher`: change to use string instead of char array
- `TextMatcher`: improved performance of int and long conversions
- `pom.xml`, tests: switched from Junit 5 to Junit 4 (more stable)

## [2.3] - 2022-11-23
### Changed
- `TextMatcher`: changed `isDigit()` and `isHexDigit()` to take `char`, not `int`

## [2.2] - 2022-11-21
### Changed
- `TextMatcher`: made `isDigit()` and `isHexDigit()` methods public
- `TextMatcher`: changed `matchContinue()` to revert index on failed match
- `TextMatcher`: `matchContinue()` parameters `maxChars` and `minChars` no longer include previous match

## [2.1] - 2022-11-19
### Changed
- `TextMatcher`: added `match()` single character using predicate

## [2.0] - 2022-11-19
### Added
- `CharPredicate`: new functional interface
### Changed
- `TextMatcher`: switched to use `CharPredicate`
- `TextMatcher`: renamed `match()` functions that take predicate to `matchSeq()` (match sequence) - breaking change
- `TextMatcher`: added `matchContinue()`

## [1.1] - 2021-09-16
### Changed
- `TextMatcher`: added `getChar()`, `revert()`, `skipToEnd()` and `skipFixed()`

## [1.0] - 2021-07-19
### Added
- all files: initial versions
