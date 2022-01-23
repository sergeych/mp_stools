# MP string tools

> work in progress

# Why reinventing the wheel?

When I has started to write our applications and libraries in MP mode, sp our code works on most of the plaforms we do
developmnet for, I have found that many tools I am used to do not exist on all platforms, or exist with different
interfaces. So, I've started to write protable interfaces to it that works fine and, the most important, _the same_ on
all three platforms.

Please help me if you like the idea ;)

## Maven deps?

Coming in few days!

## printf!

The most popular and knonwn stromg format tool exists only on late JVM platform, so I reimplement it in prtable way. To be short, see example:

_note that there is two variants `"%s".sprintf()` and `"%s".format` but the latter is already used in JVM and may confuse. 

## Integers

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

## Texts

Texts works with any object, using it's `toString()`, also with numbers, wit the same positioning, size anf fill flags:

~~~kotlin
assertEquals("== 3 ==", "== %s ==".sprintf(3))
assertEquals("==   3 ==", "== %3s ==".sprintf(3))
assertEquals("== 3   ==", "== %-3s ==".sprintf(3))
assertEquals("==  3  ==", "== %^3s ==".sprintf(3))
assertEquals("== **3** ==", "== %*^5s ==".sprintf(3))
assertEquals("== __3__ ==", "== %_^5s ==".sprintf(3))
~~~


