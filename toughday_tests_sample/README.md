# Running new tests in toughday2
We will soon have nice support and our own classloader.
Until then, adding tests by running the executable jar is not supported.
That's not really a problem, you can just add it to the classpath when running the TD2 main class:
```
java -cp toughday2-0.2.0.jar:toughday2-tests-sample-0.1.0-SNAPSHOT.jar com.adobe.qe.toughday.Main --help_tests
```

... which should show `MyDemoTest` fully integrated.
