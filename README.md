## earcut kotlin multiplatform

Earcut implementation as kotlin multiplaform library supporting jvm, js, and native.

This library is based on the java port https://github.com/the3deers/earcut-java of the javascript library https://github.com/mapbox/earcut.

## usage

The library can be added as maven project. It is hosted using the gitlab package registry. 

Add the project's maven repository (gradle):
```
repositories {
    maven {  
        url "https://gitlab.com/api/v4/projects/21979444/packages/maven"  
        name "GitLab"  
    }  
}
```
add the dependency:
```
dependencies {
 implementation("de.urbanistic:earcut-kotlin:1.0.0")
}
```