The application receives two arguments: "path/to/original.java" and "path/to/injected.java". The original file is a Java file with methods annotated with any of the supported annotations. After execution, the injected file would be a copy of the original java class with the annotations removed and requried code injected.

Example:
java -jar J2JInjection.jar /me/Documents/URLDownload.java /me/Documents/Gen/URLDownload.java

Caveat: If the injectd file has a different name than the original, the class inside would not be consistent with the new name (i.e. it will retain the name of the original file). It is recomended to locate the generated files in a different folder in which the same name can be used. 
