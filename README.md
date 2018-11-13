A gradle plugin to extract the dependency info (between classes, fields and methods) of your Android project. Save all the dependencies to a sqlite database file.

Database structure:

It has a `type` table for saving all type (class and interface) names, a `field` and a `method` table for saving all fields and methods inside classes, respectively. And lastly there is a `reference` table for saving references between all of them.

References between elements:

| Action | Releation |
| :- | :- |
| Type `A` inherits, implements or is annotated with `B` | 	`A(TYPE) REFERENCES B(TYPE)` |
| Field `D`'s type is `B`, or be annotated with `B` | `D(FIELD) REFERENCES B(TYPE)` |
| Method `E` is annotated with `B`| `E(METHOD) REFERENCES B(TYPE)` |
| Method `E`'s parameter or return value's type is `B` | `E(METHOD) REFERENCES B(TYPE)` |
| Method `E`'s parameter or return value is annotated with `B`| `E(METHOD) REFERENCES B(TYPE)` |
| Method `E` uses type `A` in the code block | `E(METHOD) REFERENCES A(TYPE)` |
| Method `E` uses field `D` in the code block | `E(METHOD) REFERENCES D(FIELD)` |
| Method `E` calls method `F` in the code block | `E(METHOD) REFERENCES F(METHOD)` |

Usage:

The `${lastest-version}` of this plugin is [![](https://jitpack.io/v/nekocode/DepAnPlugin.svg)](https://jitpack.io/#nekocode/DepAnPlugin). Copy below code to the `build.gradle` of your android application project.

```gradle
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath "com.github.nekocode:DepAnPlugin:${lastest-verion}"
    }
}

apply plugin: 'depan'
 
depan {
    outputDirFile = new File(project.buildDir, "depan")
//  enabled = true
    typeFilter { typeName ->
        !typeName.endsWith(".R") &&
                !typeName.contains(".R\$") &&
                !typeName.contains(".databinding.") &&
                !typeName.endsWith(".BR")
    }
}
```

Then, you will get a sqlite database file named `${buildType}.db` in the specified outputDir when you run the corresponding `assemble${buildType}` Task.

Now, you can query this database file by any sqlite client: 

![query_result](images/query_result.png)
