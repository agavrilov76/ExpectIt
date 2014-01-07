package net.sf.expectit;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.eof;
import static net.sf.expectit.matcher.Matchers.regexp;

/**
 * A process example.
 */
public class ProcessExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("/bin/sh");

        Expect expect = new ExpectBuilder()
                .withInputs(process.getInputStream())
                .withOutput(process.getOutputStream())
                .withTimeout(1, TimeUnit.SECONDS)
                .withEchoOutput(new PrintWriter(System.out))
                .withErrorOnTimeout(true)
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
    }

}
