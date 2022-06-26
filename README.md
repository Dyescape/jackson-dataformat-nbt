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

```kt
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
