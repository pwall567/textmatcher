# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

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
