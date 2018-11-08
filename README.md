A gradle plugin to extract the dependency info (between classes, fields and methods) of your Android project. Save all the dependencies to a sqlite database file.

Database structure:

It has a `type` table for saving all type (class and interface) names, a `field` and a `method` table for saving all fields and methods inside classes, respectively. And lastly there is a `reference` table for saving references between all of them.

Usage:

```gradle
apply plugin: 'depan'
 
depan {
    outputDirFile = new File(project.buildDir, "depan")
    typeFilter { typeName ->
        typeName.startsWith("com.zhihu") &&
                !typeName.endsWith(".R") &&
                !typeName.contains(".R\$") &&
                !typeName.contains(".databinding.") &&
                !typeName.endsWith(".BR")
    }
}
```

Then, you will get a sqlite database file named `${buildType}.db` in the specified outputDir when you run the corresponding `assemble${buildType}` Task.
