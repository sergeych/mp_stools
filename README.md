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

The most popular and knonwn stromg format tool exists only on late JVM platform, so I reimplement it in prtable way. To be short, see example:

_note that there is two variants `"%s".sprintf()` and `"%s".format` but the latter is already used in JVM and may confuse._ 

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

### Safety notes

This sprintf/format implementation is safe on all platforms as it has not dependencies except standard `Number.toString()` wich is presumably safe. Despite of its name it does not call `C` library, uses controlled memory allocation and could not provide ovverruns (as kotlin arrays are all checked).

### Sprintf syntax summary

Generic fields has the following notation:

    %[flags][size]<format>

flags and size are optional, there could be several flags. The size field is used to pad the result to specified size, padding is added with spaces before the value by default, this behavior could be changed with flags, see below.

If the argument is wider than the `size`, it is inserted as it is ignoring positioning flags and `size` field.  

#### flags

| flag        | sample  | meaning                                         | applicable              |
|-------------|---------|-------------------------------------------------|-------------------------|
| `-`         | `%-5d`  | adjust to left                                  | with size               |
| `^`         | `%12s`  | center                                          | with size               |
| `*` `#` `_` | `%*10s` | fill with specified character                   | with size               |
| `0`         | `%010d` | fill with leading zeroes                        | with size, only numbers |
| `+`         | `$+d`   | explicitly show `+` sign with _positive numbers | with numbers only       |

#### Supported format specificators

As for now:

| format     | meaning                                       | consumed argument type  |
|------------|-----------------------------------------------|-------------------------|
| `s`        | text representation (string or anything else) | `Any`                   |
| `d` or `i` | as integer number                             | any `Number`            |
| `x`        | hexadecimal number, lowercase characters      | any integer type        |
| `X`        | hexadecimal number, uppercase characters      | any integer type        |
| `%`        | insert percent character                      | no argument is consumer |

### Nearest plans

- Support for kotlinx time formats