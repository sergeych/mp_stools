# KMP Sergeych's tools

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> Important: Versions 1.5.1+ is built with kotlin 2 and is compatible with ALL KMP platforms including experimentlas
> wasmJS. It also contains important fix on displaying negative integers in some formats.

## 1.5.2 is built for all target, ios and wasmjs included!

> See also [dokka docs](https://code.sergeych.net/docs/mp_stools/)

Kotlin Multiplatform important missing tools, like sprintf with a wide variety of formats, portable base64

# Why reinventing the wheel?

When I started to write our applications and libraries in KMP mode, my code worked the same on all of the plaforms we
develop for. Many tools our team is used to do not exist on all platforms or exist with different
interfaces. So, I've started to write portable interfaces to it that work everywhere and _with the same interface_ on
all three platforms.

## In short, this library provides:

All platforms (macosX64, macosArm64, iosX64, iosArm64, iosSimulatorArm64,
linuxX6, mingwX64, JVM, JS, wasmJS), in the same way:

- `Stirng.sprintf` - something like C `sprinf` or JVM `String.format` but with more features and multiplatform

- base64: `ByteArray.encodeToBase64()`, `ByteArray.encodeToBase64Compact()`, `String.decodeBase64()`
  and `ByteArray.decodeBase64Compact()`. Also, URL-friendly forms: `ByteArray.encodeToBase64Url`
  and `String.decodeBase64Url`.

- Boyer-Moore based fast `ByteArray.indexOf`

- ByteArray tools: `getInt`, `putInt` and fast `indexOf`
- Tools to cache recalculable expressions: `CachedRefreshingValue`, `CachedExpression` and `CachedSyncExpression` for
  JVM (as a good multithreading is there)
- Missing `ReenterantMutex` for coroutines
- Smart, fast and effective _asynchronous logging_, coroutine-based, using flows ti subscribe to logs and coroutines and
  closures not to waste time on preparing strings where logging level filters are out anyway.

## Installation

Use gradle maven dependency. First add our repository:

~~~
repositories {
    // ...
    maven("https://maven.universablockchain.com/")
}
~~~

then add dependency:

~~~
dependencies {
    //...  
    // see versions explained below, use latest release from
    // 'releases' or whatever you need:
    implementation("net.sergeych:mp_stools:1.5.2")
}
~~~

That's all. Now you have working `sprintf` on every KMP platform ;)

# String tools:

## printf / sprintf!

The most popular and known stromg format tool exists only on the modern JVM platforms,
so I reimplement it in a platform-independent way.
Here are some examples, the reference is below it.
I reporoduced
the [Java 11 String.format() notation](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Formatter)
as much as possible, with the following notable differences:

- for argument number (`%1$12s`) is possible also to use `!` instead of `$` (as the latter should be escaped in kotlin),
  e.g. `%1!12s` in that case is _also valid_
- date/time per-platform locales are not yet supported, everything is in English
- time zone abbreviations are missing (system returns valid tz id like +01:00 instead), as kotlinx.time does not
  provide (yet?)

see reference below

### Integers

~~~kotlin
// Integers
assertEquals("== 3 ==", "== %d ==".sprintf(3))
assertEquals("==   3 ==", "== %3d ==".sprintf(3))
assertEquals("== 3   ==", "== %-3d ==".sprintf(3))
assertEquals("==  3  ==", "== %^3d ==".sprintf(3))

// fill
assertEquals("== 003 ==", "== %03d ==".sprintf(3))
assertEquals("== **3** ==", "== %*^5d ==".sprintf(3))
assertEquals("== __3__ ==", "== %_^5d ==".sprintf(3))

// leading plus
assertEquals("== +3 ==", "== %+d ==".sprintf(3))
assertEquals("== +0003 ==", "== %+05d ==".sprintf(3))

// hex
assertEquals("== 1e ==", "== %x ==".sprintf(0x1e))
assertEquals("== 1E ==", "== %X ==".sprintf(0x1e))

// filled hex
assertEquals("== ###1e ==", "== %#5x ==".sprintf(0x1e))
assertEquals("== 1e### ==", "== %#-5x ==".sprintf(0x1e))
assertEquals("== ##1E## ==", "== %#^6X ==".sprintf(0x1e))

// escaping percent
assertEquals("10%", "10%%".sprintf())
~~~

### Strings (or anything as string)

Texts works with any object, using it's `toString()`, also with numbers, wit the same positioning, size and fill flags:

~~~kotlin
// regular strings
assertEquals("*****hello!", "%*10s!".sprintf("hello"))
assertEquals("Hello, world!", "%s, %s!".sprintf("Hello", "world"))
assertEquals("___centered___", "%^_14s".sprintf("centered"))

// number as anything else are processed using `toString()`:
assertEquals("== 3 ==", "== %s ==".sprintf(3))
assertEquals("==   3 ==", "== %3s ==".sprintf(3))
assertEquals("== 3   ==", "== %-3s ==".sprintf(3))
assertEquals("==  3  ==", "== %^3s ==".sprintf(3))
assertEquals("== **3** ==", "== %*^5s ==".sprintf(3))
assertEquals("== __3__ ==", "== %_^5s ==".sprintf(3))
~~~

### Floats

Any `Number` instances (we tried integer, long, float and double) can be formatted with `%g`, `%f` and `%e` specifiers:

~~~kotlin
// best fit, platofrm-dependent "good" representation:
assertEquals("17.234", "%g".sprintf(17.234))
assertEquals("**17.234", "%*8g".sprintf(17.234))
assertEquals("+017.234", "%+08g".sprintf(17.234))

// Scientific format:
assertEquals("-2.39E-3", "%.2E".sprintf(-2.39e-3))
assertEquals("2.39E-3", "%.2E".sprintf(2.39e-3))
assertEquals("+2.39E-3", "%+.2E".sprintf(2.39e-3))

assertEquals("2.4E-3", "%6E".sprintf(2.39e-3))
assertEquals("0002.4E-3", "%09.1E".sprintf(2.39e-3))
assertEquals("+002.4E-3", "%+09.1E".sprintf(2.39e-3))

// format with decimal part of fixed with (no exponent):
assertEquals("1.000", "%.3f".sprintf(1))
assertEquals("221.122", "%.3f".sprintf(221.1217))
assertEquals("__221.1", "%_7.1f".sprintf(221.1217))
assertEquals("_+221.1", "%+_7.1f".sprintf(221.1217))
assertEquals("+0221.1", "%+07.1f".sprintf(221.1217))
assertEquals("00221.1", "%07.1f".sprintf(221.1217))
~~~

### Safety notes

This sprintf/format implementation is safe on all platforms
as it has no dependencies except standard `Number.toString()`,
which is presumably safe.
Despite its name, it does not call `C` library,
uses controlled memory allocation and could not provide overruns, as kotlin arrays are all checked.

### Sprintf syntax summary

Generic format field has the following notation:

    %[flags][size][.decimals]<format>

flags and size are optional, there could be several flags. The size field is used to pad the result to specified size,
padding is added with spaces before the value by default; this behavior could be changed with flags, see
below. `decimals` where applicable takes precedence over size, and determines how many decimal digits will be included,
e.g. `"%.3f".sprintf(1) == "1.000"`

If the argument is wider than the `size`, it is inserted as it is ignoring positioning flags and `size` field.

#### flags

| flag        | sample  | meaning                                         | applicable              |
|-------------|---------|-------------------------------------------------|-------------------------|
| `-`         | `%-5d`  | adjust to left                                  | with size               |
| `^`         | `%12s`  | center                                          | with size               |
| `*` `#` `_` | `%*10s` | fill with specified character                   | with size               |
| `0`         | `%010d` | fill with leading zeroes                        | with size, only numbers |
| `+`         | `%+d`   | explicitly show `+` sign with _positive numbers | with numbers only       |

#### Supported format specificators

As for now:

| format     | meaning                                                            | consumed argument type  |
|------------|--------------------------------------------------------------------|-------------------------|
| `s`        | text representation (string or anything else)                      | `Any`                   |
| `c`        | signle character                                                   | `Char`                  |
| `d` or `i` | as integer number                                                  | any `Number`            |
| `x`        | hexadecimal number, lowercase characters                           | any integer type        |
| `X`        | hexadecimal number, uppercase characters                           | any integer type        |
| `o`        | octal number                                                       | any integer type        |
| `f`        | float number, fixed decimal points, respects `decimals` field      | any `Number`            |
| `g`, `G`   | platorm-dependedn 'best fit' float number, ingnores `decimals`.    | any `Number`            |
| `e`, `E`   | float, scientific notation with exponent, respect `decimals` field | any `Number`            |         
| `t*`       | date time, see below                                               | differnet time objects  |         
| `%`        | insert percent character                                           | no argument is consumer |

In `g`/`G` and `e`/`E` formats the case of the result exponent character is the same as for the format character.

## Date/time formatting

We support
the [Java 11 String.format() notation](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Formatter.html#syntax)
as much as possible here too.

To format a time object, it is possible to use:

- multiplatform (recommended!) `kotlinx.datetime` classes: `Instant` and `LocalDateTime`.
- on JS platoform also javascript `Date` class instances are also ok
- on JVM platofm you can also use `java.time` classes: `java.time.Instant`, java.time.LocalDateTime` and `
  java.time.ZonedDateTime` as well. Zoned date time will be converted to the system's default time zone (e.g., its time
  zone
  information will be lost).

Supported are all standard format specifiers.

#### Time formats

| format | meaning                                                                                                                                                                                                                                             |
|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tH`   | Hour of the day for the 24-hour clock, formatted as two digits with a leading zero as necessary i.e. 00 - 23.                                                                                                                                       |
| `tI`   | Hour for the 12-hour clock, formatted as two digits with a leading zero as necessary, i.e. 01 - 12.                                                                                                                                                 |
| `tk`   | Hour of the day for the 24-hour clock, i.e. 0 - 23.                                                                                                                                                                                                 |
| `tl`   | Hour for the 12-hour clock, i.e. 1 - 12.                                                                                                                                                                                                            |
| `tM`   | Minute within the hour formatted as two digits with a leading zero as necessary, i.e. 00 - 59.                                                                                                                                                      |
| `tS`   | Seconds within the minute, formatted as two digits with a leading zero as necessary, i.e. 00 - 59                                                                                                                                                   |
| `tL`   | Millisecond within the second formatted as three digits with leading zeros as necessary, i.e. 000 - 999.                                                                                                                                            |
| `tN`   | Nanosecond within the second, formatted as nine digits with leading zeros as necessary, i.e. 000000000 - 999999999.                                                                                                                                 |
| `tP`   | Locale-specific morning or afternoon marker in lower case, e.g."am" or "pm". Use of the conversion prefix 'T' forces this output to upper case.                                                                                                     |
| `tz`   | RFC 822 style numeric time zone offset from GMT, e.g. -0800. This value will be adjusted as necessary for Daylight Saving Time. For long, Long, and Date the time zone used is the default time zone for this instance of the Java virtual machine. |
| `tZ`   | A string representing the abbreviation for the time zone. Not fully supported                                                                                                                                                                       |
| `ts`   | Seconds since the beginning of the epoch starting at 1 January 1970 00:00:00 UTC, i.e. Long.MIN_VALUE/1000 to Long.MAX_VALUE/1000.                                                                                                                  |
| `tQ`   | Milliseconds since the beginning of the epoch starting at 1 January 1970 00:00:00 UTC, i.e. Long.MIN_VALUE to Long.MAX_VALUE.                                                                                                                       |

#### Date formats

Note. If the locale is not implemented for the platform, English names are used automatically.

| format | meaning                                                                                                                     |
|--------|-----------------------------------------------------------------------------------------------------------------------------|
| `tB`   | Locale-specific full month name, e.g. "January", "February".                                                                |
| `tb`   | Locale-specific abbreviated month name, e.g. "Jan", "Feb".                                                                  |
| `th`   | same as `tb`                                                                                                                |
| `tA`   | Locale-specific full name of the day of the week, e.g. "Sunday", "Monday"                                                   |
| `ta`   | Locale-specific short name of the day of the week, e.g. "Sun", "Mon"                                                        |
| `tC`   | __Not implemented. Please use `ty`__                                                                                        |
| `tY`   | Year, formatted as at least four digits with leading zeros as necessary, e.g. 0092 equals 92 CE for the Gregorian calendar. |
| `ty`   | Last two digits of the year, formatted with leading zeros as necessary, i.e. 00 - 99.                                       |
| `tj`   | Day of year, formatted as three digits with leading zeros as necessary, e.g. 001 - 366 for the Gregorian calendar.          |
| `tm`   | Month, formatted as two digits with leading zeros as necessary, i.e. 01 - 13.                                               |
| `td`   | Day of month, formatted as two digits with leading zeros as necessary, i.e. 01 - 31                                         |
| `te`   | Day of month, formatted as two digits, i.e. 1 - 31.                                                                         |

#### Date+time compositions

| format | meaning                                                                                                                                     |
|--------|---------------------------------------------------------------------------------------------------------------------------------------------|
| `tR`   | Time formatted for the 24-hour clock as "%tH:%tM"                                                                                           |
| `tT`   | Time formatted for the 24-hour clock as "%tH:%tM:%tS".                                                                                      |
| `tr`   | Time formatted for the 12-hour clock as "%tI:%tM:%tS %Tp". The location of the morning or afternoon marker ('%Tp') may be locale-dependent. |
| `tD`   | Date formatted as "%tm/%td/%ty".                                                                                                            |
| `tF`   | ISO 8601 complete date formatted as "%tY-%tm-%td"                                                                                           |
| `tc`   | Date and time formatted as "%ta %tb %td %tT %tZ %tY", e.g. "Thu May 06 05:45:11 +01:00 1970".                                               |

#### Extensions

| format | meaning                                                                                                                                                                              |
|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tO`   | Popular ISO8601 variant, like 1970-06-05T05:41:11+03:00. Not a digit, letter `O`                                                                                                     |
| `t#`   | 140letter "serial time" in UTC zone, like `20220414132756` year, month, day, hour, minute and second all together with leading zeroes ib 24h mode, for example, to use in file names |

### Notes

_note that there is two variants `"%s".sprintf()` and `"%s".format` but the latter is already used in JVM and may
confuse._

## Base64

Why?
Because, for example, in JS,
there is no good way to convert to/from ByteArray
(or Uint8Array)that always works well and does not require NPM dependencies that work synchronously.

I know how it could be made almost portable with promises, though. So, here is an implementation that works well
everywhere with the same interface.
The wheel is reinvented one more time.

~~~
val src = byteArrayOf(1,3,4,4)
assertEquals(src.encodeToBase64Compact(), "AQMEBA")
assertEquals(src.encodeToBase64(), "AQMEBA==")
assertContentEquals(src, "AQMEBA".decodeBase64Compact())
assertContentEquals(src, "AQMEBA==".decodeBase64())
~~~

__Compact__ vartiant simply does not use trailing filling '=' characters, these are practically useless but taking
space.

## Minimal logger

_logging is not working in the kotlin.native platform as it is yet single-threaded in the core and does not support
shared objects sucj as flow (as for now)_.

Library provides an extremely compact and effective platform-independent asyncronous logger that uses coroutines to provide
little performance impact. The idea behind is that the logging data is collected and formatted _conditionally_: instead
of providing strings with substitutions we provide callables that returns strings or string to exception pairs:

~~~kotlin
debug { "this is a trace: ${Math.sin(Math.PI)}" }
~~~

The string is rather slow in interpolation as it uses `Math.sin`. But, (1) it will not be interpolated if effective log
level is above the `Log.Level.Debug`, and (2) if it is, it will be interpolated asyncronously, maybe in a separate
thread or when this thread become idle. A coroutine context is used to prepare the data to be logged.

To start logging, implement an [Loggable] interface in your class, and connect some log sinks:

~~~kotlin
val x = object : Loggable by LogTag("TSTOB") {}
x.info { "that should not be missing because of the replay buffer" }
Log.connectConsole()
~~~

To receive log messages (asynchronously) use `Log.logFlow` shared flow, or connect some stabdard receiver like console
one as in the sample above.

# Future

The library is actively maintained and is going to be maintained for a long time; it is already used in many commercial projects).

# Mac-compatible releases

I release Apple compatible releases when I have access to Macs. Normally I do not use Apple or Microsoft OS, sticking with Linux;
I have serious concerns about privacy and safety on these. Everybody now see what happened to all poor apple customers 
born in improper country as for their lets see far too mature president ;) Therefore I stick to linux, and traveling with two notebooks is against customs regulations everywhere, so non-snapshot full releases are not too frequent. 

Meanwhile I encourage using snapshot releases except for Apple targets, these are frequent and I increase minor versions
on every notable change, so it should be safe. Thanks for understanding -- wnd I recommend considering wide use of free open source platforms.
