# MP string tools

> work in progress

# Why reinventing the wheel?

When I has started to write our applications and libraries in MP mode, as our code work the same on 3 of the plaforms we develop for, I have found that many tools our team is used to do not exist on all platforms, or exist with different interfaces. So, I've started to write protable interfaces to it that works everywhere and _with the same interface_ on all three platforms.

Please help me if you like the idea ;)

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
    implementation("net.sergeych:mp_stools:1.0.0-SNAPSHOT")
}
~~~
that's all. Now you have working `sprintf` on every MP platform ;)


Coming in few days!

# String tools:

## sprintf!

The most popular and knonwn stromg format tool exists only on late JVM platform, so I reimplement it in platofrm-portable way:

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
assertEquals("== +0003 ==","== %+05d ==".sprintf(3))

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

Texts works with any object, using it's `toString()`, also with numbers, wit the same positioning, size anf fill flags:

~~~kotlin
// regular strings
assertEquals("*****hello!","%*10s!".sprintf("hello"))
assertEquals("Hello, world!","%s, %s!".sprintf("Hello", "world"))
assertEquals("___centered___","%^_14s".sprintf("centered"))

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
assertEquals("-2.39E-3", "%.2E".sprintf(-2.39e-3) )
assertEquals("2.39E-3", "%.2E".sprintf(2.39e-3) )
assertEquals("+2.39E-3", "%+.2E".sprintf(2.39e-3) )

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

This sprintf/format implementation is safe on all platforms as it has not dependencies except standard `Number.toString()` wich is presumably safe. Despite of its name it does not call `C` library, uses controlled memory allocation and could not provide ovverruns (as kotlin arrays are all checked).

### Sprintf syntax summary

Generic fields has the following notation:

    %[flags][size][.decimals]<format>

flags and size are optional, there could be several flags. The size field is used to pad the result to specified size, padding is added with spaces before the value by default, this behavior could be changed with flags, see below. `decimals` where applicable takes precedence over size, and determine how many decimal digits will be included, e.g. `"%.3f".sprintf(1) == "1.000"`

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

| format     | meaning                                                             | consumed argument type  |
|------------|---------------------------------------------------------------------|-------------------------|
| `s`        | text representation (string or anything else)                       | `Any`                   |
| `d` or `i` | as integer number                                                   | any `Number`            |
| `x`        | hexadecimal number, lowercase characters                            | any integer type        |
| `X`        | hexadecimal number, uppercase characters                            | any integer type        |
| `f`        | float number, fixed decimal points, respects `decimals` field       | any `Number`            |
| `g`, `G`   | platorm-dependedn 'best fit' float number, ingnores `decimals`.     | any `Number`            |
| `e`, `E`    | flowt, scientific notation with exponent, respect `decimals` field  | any `Number`            |         
| `%`        | insert percent character                                            | no argument is consumer |

In `g`/`G` and `e`/`E` formats the case of the result exponent character is the same as for the format character. 

### Notes

_note that there is two variants `"%s".sprintf()` and `"%s".format` but the latter is already used in JVM and may confuse._

### Nearest plans

- Support for kotlinx time formats