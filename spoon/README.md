TTC 2015 Live Contest with Spoon
================================

This project is a solution for the TTC 2015 Live Contest with the tools [Spoon](http://spoon.gforge.inria.fr/).

How to launch the solution?
===========================

It is pretty simple. There is 3 annotations, so there is 3 arguments to launch each transformation:

> **Note:** It isn't necessary to generate the executable because the jar file is pushed in the
 target directory. But you can generate yourself with a `mvn package` command at the root
 of the project.

- `-retry` apply the transformation for the RetryOnFailure annotation.
- `-cache` apply the transformation for the Cacheable annotation.
- `-logging` apply the transformation for the Loggable annotation.

You can use them separately but also together and if you want execute all of them, use the argument
 `all`. It is just a simply way for `-retry -cache -logging` and it is the default argument if you
 specify nothing.

In bonus, there are two other arguments: `-input` and `-output`:

- By default, the input argument will launch our solution on the `FinalURLDownload` class but if
you want to apply the transformation on another class, you can specify it with this argument.
- By default, the output argument will print the result of the transformation on the target
directory of this project but you can specify your own output with this argument.

So, if you use the jar file in the target directory and you want to execute all transformations,
you must to execute this single line:

```
java -jar target/ttc15-tranj-1.0.0-SNAPSHOT.jar
```

Any problems?
=============

If you have any problems with this solution, you can create an issue on this [GitHub project](https://github.com/GerardPaligot/ttc15-live-contest).

Enjoy!