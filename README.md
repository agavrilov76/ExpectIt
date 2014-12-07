[![Build Status](https://travis-ci.org/Alexey1Gavrilov/ExpectIt.png?branch=master)](https://travis-ci.org/Alexey1Gavrilov/ExpectIt)
[![Coverage Status](https://img.shields.io/coveralls/Alexey1Gavrilov/ExpectIt.svg)](https://coveralls.io/r/Alexey1Gavrilov/ExpectIt?branch=master)
# Yet Another Expect for Java

## Overview

ExpectIt - is yet another pure Java 1.6+ implementation of the [Expect](http://en.wikipedia.org/wiki/Expect) tool.
It is designed to be simple, easy to use and extensible. Written from scratch. Here are the features:

* Fluent-style API.
* No third-party dependencies.
* NIO based implementation using pipes and non-blocking API.
* Extensible matcher framework. Support regular expressions and group operations.
* Support multiple input streams.
* Extensible filter framework to modify input, for example, to remove non-printable ANSI terminal characters.
* [Custom Expect Ant Task](../..//wiki/Expect-for-Ant).
* Tested on Andriod.
* Apache License.

The ExpectIt project is a modern alternative to other popular 'Expect for Java' implementations, such as:

* [ExpectJ](http://expectj.sourceforge.net/)
* [Expect4J](https://github.com/cverges/expect4j)
* [Expect-for-Java](https://github.com/ronniedong/Expect-for-Java)

I believe that none of the projects above has all the features that ExpectIt has. So if you are looking for a Java
expect library please give ExpectIt a try.

The API javadoc documentation is available [here](http://alexey1gavrilov.github.io/ExpectIt/0.6.1/apidocs/).
## Quick start

The library is available on [the Maven central](http://search.maven.org/#search|gav|1|g%3A%22net.sf.expectit%22%20AND%20a%3A%22expectit-core%22).
Add the following Maven dependency to your project:

```xml
    <dependency>
        <groupId>net.sf.expectit</groupId>
        <artifactId>expectit-core</artifactId>
        <version>0.6.1</version>
    </dependency>
```
You can also download the ``expectit-core.jar`` file from the release project page at
[sourceforge.net](https://sourceforge.net/projects/expectit/files/releases/) and add it to your classpath.

To begin with you need to construct an instance of ``net.sf.expectit.Expect`` and set the input and output streams as
follows:

```java
    // the stream from where you read your input data
    InputStream inputStream = ...;
    // the stream to where you send commands
    OutputStream outputStream = ...;
    Expect expect = new ExpectBuilder()
        .withInputs(inputStream)
        .withOutput(outputStream)
        .build();
    expect.sendLine("command").expect(contains("string"));
    Result result = expect.expect(regexp("(.*)--?--(.*)"));
    // accessing the matching group
    String group = result.group(2);
```
Note that you may need to add static import of the matcher factory methods in your code.

## How it works

Once an Expect object is created the library starts background threads for every input stream. The threads read
bytes from the streams and copy them into NIO pipes. The pipes are configured to use non-blocking source channel.

The expect object holds a String buffer for each input. The user calls one of the expect methods to wait until the
given matcher object matches the corresponding buffer contents. If the input buffer doesn't satisfy the matcher
criteria, then the method blocks for a configurable timeout of milliseconds until new data is available on the
input stream NIO pipe.

The result object indicates whether the match operation was successful or not. It holds the context of the match. It
implements the ``java.util.regexp.MatchResult`` interface which provides access to the result of regular
expression matching results. If the match was successful, then the corresponding input buffer is update, all
characters before the match including the matching string are removed. The next match is performed for the updated
buffer.

## Thread safety notes

The send methods are generally thread safe as long as the underlying output streams are. In other words it is safe 
to send data from one thread and expect the results in another.

The expect methods are not thread safe since they mutate the state of the expect buffers. The expect operaiton must
not be performed concurrently.

## Interacting with OS process

Here is an example of interacting with a spawn process:

```java
        Process process = Runtime.getRuntime().exec("/bin/sh");

        Expect expect = new ExpectBuilder()
                .withInputs(process.getInputStream())
                .withOutput(process.getOutputStream())
                .withTimeout(1, TimeUnit.SECONDS)
                .withExceptionOnFailure()
                .build();
        // try-with-resources is omitted for simplicity
        expect.sendLine("ls -lh");
        // capture the total
        String total = expect.expect(regexp("^total (.*)")).group(1);
        System.out.println("Size: " + total);
        // capture file list
        String list = expect.expect(regexp("\n$")).getBefore();
        // print the result
        System.out.println("List: " + list);
        expect.sendLine("exit");
        // expect the process to finish
        expect.expect(eof());
        // finally is omitted
        process.waitFor();
        expect.close();
```

## Interacting with SSH server

Here is an example on how to talk to a public SSH service on http://sdf.org using the JSch library.
Note: you will to add [the jsch library](http://www.jcraft.com/jsch/) to your project classpath.
```java
        JSch jSch = new JSch();
        Session session = jSch.getSession("new", "sdf.org");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        Channel channel = session.openChannel("shell");

        Expect expect = new ExpectBuilder()
                .withOutput(channel.getOutputStream())
                .withInputs(channel.getInputStream(), channel.getExtInputStream())
                .withEchoOutput(System.out)
                .withEchoInput(System.err)
        //        .withInputFilters(removeColors(), removeNonPrintable())
                .withExceptionOnFailure()
                .build();
        // try-with-resources is omitted for simplicity
        channel.connect();
        expect.expect(contains("[RETURN]"));
        expect.sendLine();
        String ipAddress = expect.expect(regexp("Trying (.*)\\.\\.\\.")).group(1);
        System.out.println("Captured IP: " + ipAddress);
        expect.expect(contains("login:"));
        expect.sendLine("new");
        expect.expect(contains("(Y/N)"));
        expect.send("N");
        expect.expect(regexp(": $"));
        // finally is omitted
        channel.disconnect();
        session.disconnect();
        expect.close();
```
Note that SSH servers normally echo the received commands. The echo can be disabled by sending the `stty -echo` command. [This](expectit-core/src/test/java/net/sf/expectit/SshLocalhostNoEchoExample.java) is an example of capturing the result of the `pwd` command when the command echo is switched off.

## Using different type of matchers

In the following example you can see how to combine different matchers (assuming static import of matcher factory
methods):
```java
        // match any of predicates
        expect.expect(anyOf(contains("string"), regexp("abc.*def")));
        // match all
        expect.expect(allOf(regexp("xyz"), regexp("abc.*def")));
        // varargs method arguments are equivalent to 'allOf'
        expect.expect(contains("string1"), contains("string2"));
        // expect to match three times in a row
        expect.expect(times(3, contains("string")));
        // expect any non-empty string match
        expect.expect(anyString());
        // expect to contain "a" and after that "b"
        expect.expect(sequence(contains("a"), contains("b")));
```
## Filtering the input

If you want to modify or remove some characters in the input before performing expect operations you can use filters.
A filter instance implements ``net.sf.expectit.filter.Filter`` interface and is applied right before the matching
occurs.

Filters are defined at the time an ``net.sf.expectit.Expect`` instance is being created and they can
be disabled and re-enabled while working with the Expect instance.

The library comes with the filters for removing ANSI escape terminal and non-printable characters.
There are also more general ``replaceInString`` and ``replaceInBuffer`` filters used to modify the input buffer using
regular expressions. Here is an example:

```java
     Expect expect = new ExpectBuilder()
            .withOutput(...)
            .withInputs(...)
            // define the filters
            .withInputFilters(
                // set the filter to remove ANSI char for colors in terminal
                removeColors(),
                // set the filter to remove non-printable characters
                removeNonPrintable(),
                // set the filter to replace a substring that matches 
                // the regular expression
                replaceInString("a(.)c", "x$1z"))
            .build();

```
Note that you may need to add static import of the filter factory methods in your code.

## More examples

* [Socket Example: parsing HTTP response](expectit-core/src/test/java/net/sf/expectit/SocketExample.java)
* [Complete SSH example using JSch](expectit-core/src/test/java/net/sf/expectit/SshExample.java)
* [Complete SSH example using SshJ](expectit-core/src/test/java/net/sf/expectit/SshJExample.java)
* [Interacting with the Apache Karaf remote shell](expectit-core/src/test/java/net/sf/expectit/KarafExample.java)
* [Expect for Ant example](expectit-ant/src/example/build-ssh-example.xml)
* [Capture the command result from SSH session](expectit-core/src/test/java/net/sf/expectit/SshLocalhostExample.java)
* [Disable SSH server echo](expectit-core/src/test/java/net/sf/expectit/SshLocalhostNoEchoExample.java)
 
## Questions

If you have any questions about the library please post your message to this [Google group](https://groups.google.com/forum/#!forum/java-expectit).

## License

[Apache License, Version 2.0](LICENSE.txt)


