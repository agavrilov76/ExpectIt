[![Build Status](https://travis-ci.org/Alexey1Gavrilov/expectit.png?branch=master)](https://travis-ci.org/Alexey1Gavrilov/expectit)
Overview
========
Yet another pure Java 1.6+ implementation of the Unix Expect tool. It designed to be simple, easy to
use and extensible. Written from scratch. Here are the features:

* Fluent-style API.
* No third-party dependencies.
* NIO based implementation using pipes and non-blocking API.
* Extensible matcher framework. Support regular expressions and group operations.
* Support multiple input streams.
* Extensible filter framework to modify input, for example, to remove non-printable ANSI terminal characters.
* Apache license.

Quick start
===========
The library is available on the Maven central. Add the following Maven dependency to your project:

```xml
<groupId>net.sf.expectit</groupId>
<artifactId>expectit-core</artifactId>
<version>0.2.0</version>
```
Create an instance of ``net.java.expect.Expect`` as follows:

```java
    // the stream from where you read your input data
    InputStream inputStream = ...;
    // the stream to where you send commands
    OutputStream outputStream = ...;
    Expect expect = new ExpectBuilder()
        .withInputs(inputStream)
        .withOutput(outputStream)
        .build();
    expect.sendLine("ls -l").expect(contains("string"));
    Result result = expect.expect(regexp("(.*)----(.*)"));
    // accessing the matching group
    String group = result.group(2);
```
How it works
============
Once an Expect object is created the library starts background threads for every input stream. The threads read
bytes from the streams and copy them into NIO pipes. The pipes are configured to use non-blocking source channel.

The expect object holds a String buffer for each input. The user calls one of the expect methods to wait until the
given matcher object matches the corresponding buffer contents. If the input buffer doesn't satisfy the matcher
criteria, then the method blocks for configurable a timeout of milliseconds until new data is available on the
input stream NIO pipe.

The result object indicates whether the match operation was successful or not. It holds the context of the match. It
implement the ``java.util.regexp.MatchResult`` interface which provides access to the result of regular
expression matching results. If the match was successful, then the corresponding input buffer is update, all
characters before the match including the match are removed.

Interacting with OS process
---------------------------
Here is an example of interacting with a spawn process:
```java
        ProcessBuilder builder = new ProcessBuilder("/bin/sh");
        Process process = builder.start();
        Expect expect = new ExpectBuilder()
                .withTimeout(1000)
                .withInputs(process.getInputStream(), process.getErrorStream())
                .withOutput(process.getOutputStream())
                .build();
        expect.sendLine("echo Hello World!");
        Result result = expect.expect(regexp("Wor.."));
        System.out.println("Before: " + result.getBefore());
        System.out.println("Match: " + result.group());
        expect.sendLine("exit");
        expect.close();
```
Interacting via SSH
--------------------
Here is an example how to talk to a public SSH service on http://sdf.org using the JSch library.
Note: you will to add [the jsch library](http://www.jcraft.com/jsch/) to your project dependencies.
```java
        JSch jSch = new JSch();
        Session session = jSch.getSession("new", "sdf.org");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        Channel channel = session.openChannel("shell");
        // jsch is ready
        Expect expect = new ExpectBuilder()
                .withOutput(channel.getOutputStream())
                .withInputs(channel.getInputStream(), channel.getExtInputStream())
                // trace all the input and output activity to the standard output stream
                .withEchoOutput(System.out)
                // filter the input: remove ANSI color escape sequences and non-printable chars
                .withInputFilter(chain(removeColors(), printableOnly()))
                .build();
        channel.connect();
        expect.expect(contains("[RETURN]"));
        expect.sendLine();
        String ipAddress = expect.expect(regexp("Trying (.*)\\.\\.\\.")).group(1);
        System.out.println("Captured IP: " + ipAddress);
        session.disconnect();
        expect.close();
```
Using composition of matchers
-----------------------------
In the following example you can see how to combine different matchers:
```java
    // match any of predicates
    expect.expect(anyOf(contains("string"), regexp("abc.*def")));
    // match all
    expect.expect(allOf(regexp("xyz"), regexp("abc.*def")));
    // varargs method arguments are equivalent to 'allOf'
    expect.expect(contains("string1"), contains("string2"));
    // expect to match three times in a row
    expect.expect(times(3, contains("string")));
```
More examples
-------------
* [Complete SSH example](expectit-core/src/test/java/net/sf/expectit/SshExample.java)
* [Interacting with Apache Karaf remote shell](expectit-core/src/test/java/net/sf/expectit/KarafExample.java)

License
=======
[Apache License, Version 2.0](LICENSE.txt)

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/Alexey1Gavrilov/expectit/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

