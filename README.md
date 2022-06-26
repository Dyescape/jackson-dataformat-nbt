# Jackson NBT Data Format

Implements Mojang's [NBT](https://wiki.vg/NBT) format in [jackson](https://github.com/FasterXML/jackson).

## Usage

Using this format works just like regular jackson, but with the `ObjectMapper` replaced with `NBTMapper`.

### Java

```java
public class Example {
    public static void main(String[] args) {
        // creating the mapper
        NBTMapper mapper = new NBTMapper();

        // serialize
        byte[] bytes = mapper.writeValueAsBytes(value);

        // deserialize
        MyType value = mapper.readValue(bytes, MyType.class);
    }
}
```

### Kotlin

```kotlin
// creating the mapper
val mapper = NBTMapper()
mapper.registerKotlinModule()

// serialize
val bytes = mapper.writeValueAsBytes(value)

// deserialize
val value = mapper.readValue<MyType>(bytes)
```

> The functions `registerKotlinModule()` and `readValue<T>(...)` are from
> the [kotlin module](https://github.com/FasterXML/jackson-module-kotlin), which we highly recommend for working with
> jackson in kotlin.

## Dependency Information

You can find the latest version and the corresponding dependency
snippets [here](https://search.maven.org/artifact/com.dyescape/jackson-dataformat-nbt).

## Big Test

Something that must not be absent from any decent NBT library, the ["bigtest"](https://wiki.vg/NBT#bigtest.nbt).

This is a mapping that can serialize and deserialize the "bigtest" file:

```kotlin
class BigTest(
    val shortTest: Short,
    val longTest: Long,
    val byteTest: Byte,
    @JsonProperty("byteArrayTest (the first 1000 values of (n*n*255+n*7)%100, starting with n=0 (0, 62, 34, 16, 8, ...))")
    val byteArrayTest: ByteArray,
    @JsonProperty("listTest (long)")
    val longListTest: List<Long>,
    val floatTest: Float,
    val doubleTest: Double,
    val intTest: Int,
    @JsonProperty("listTest (compound)")
    val compoundListTest: List<CompoundListTest>,
    @JsonProperty("nested compound test")
    val nestedCompounds: Map<String, NestedCompound>,
    val stringTest: String,
) {
    class CompoundListTest(
        @JsonProperty("created-on")
        val createdOn: Long,
        val name: String,
    )

    class NestedCompound(
        val name: String,
        val value: Float,
    )
}
```

And this is how you can create the exact value:

```kotlin
BigTest(
    32767,
    9223372036854775807,
    127,
    ByteArray(1000) { ((it * it * 255 + it * 7) % 100).toByte() },
    listOf(11L, 12L, 13L, 14L, 15L),
    0.49823147f,
    0.4931287132182315,
    2147483647,
    listOf(
        BigTest.CompoundListTest(1264099775885, "Compound tag #0"),
        BigTest.CompoundListTest(1264099775885, "Compound tag #1"),
    ),
    buildMap {
        put("egg", BigTest.NestedCompound("Eggbert", 0.5f))
        put("ham", BigTest.NestedCompound("Hampus", 0.75f))
    },
    "HELLO WORLD THIS IS A TEST STRING ÅÄÖ!",
)
```

> **Note:** The root tag of the original "bigtest" file is named `Level`. Parsing this is should work, however the
> generator will write the root tag name as an empty string, as that is how minecraft also behaves.
