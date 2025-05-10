# Description
An easy-to-use command-line argument parser for Kotlin apps. Interprets an array of `String` arguments formatted in a familiar syntax and serializes a valid, stable object of arguments to be coveniently read in any given program.

#### Features
* Declare required, optional, flag, and positional args
* Supports multiple key-value delimiters for your preferred use, eg: `-n 3`, `-n=3`, or `-n3`
* Set default values for optional and flags args
* Convenient mapping arg for restricted values
* Transform parameter to convert `String` value to arbitrary type
* Stacked flag arguments eg, `-i` and `-v` => `-iv`
* Concise one-line feedback for parsing errors 
* Builtin `--help` command to print neatly-formatted and comprehensive usage statement
* Builtin `--version` and `--quit` commands
* `--` delimiter to separate options and positional args 
* Subcommands support for implementing commands like `git commit [options]` or `git add [options]` 

---

# Usage example

# Argument nomenclature
**Options**: Encompass arguments before positionals including: requireds, optionals, and flags

**Optionals**: Key-value option that may or may not be declared in `args` and may or may not have a default fallback value

**Required**: Key-value option that must be provided in `args` to successfully parse

**Flag**: Optional without a key that maps to a `Boolean`. `False` by default and can be overriden to be `true` by default.

**Mapping**: Key-value option with a restricted set of values. Can be made either required or optional.

**Positional**: Argument(s) found after the option declarations in `args`. Their 'position' in `args` matters relative to thier declaration order.

# Formatting `--help` output
Formatting the `--help` output is limited in the project's current state. However, you may set a prologue or an epilogue statement like so: 

# Exceptions

# Tests

# Importing the library

# Todo
- [ ] Optional positonals
- [ ] Arbitrary # of positional args
- [ ] Arbitrary # of optional values
- [ ] Bash-style regex string parsing
- [ ] Customizable `--help` command formatting
- [ ] Range or other arg validation info in `--help`
- [ ] Embedded subcommands eg, 

# Contributing
MRs and creating issues are more than welcome 

# Aknowledgement
Thanks to [kotlin-argparser](https://github.com/xenomachina/kotlin-argparser) for the inspiration of this project
