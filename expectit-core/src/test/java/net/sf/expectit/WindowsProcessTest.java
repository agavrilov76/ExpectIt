package net.sf.expectit;

import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.regexp;

public class WindowsProcessTest {
    public static void main(String[] args) throws Exception {
        Process process = Runtime.getRuntime().exec("cmd.exe");
        Expect expect = new ExpectBuilder()
                .withInputs(process.getInputStream(), process.getErrorStream())
                .withOutput(process.getOutputStream())
                .withTimeout(1, TimeUnit.SECONDS)
                .withExceptionOnFailure()
                .build();
        expect.expect(regexp("C:\\\\Users\\\\.*>"));
        expect.sendLine("echo HELLO");
        expect.expect(regexp("C:\\\\Users\\\\.*>"));
        expect.sendLine("dir");
        String list = expect.expect(regexp("C:\\\\Users\\\\.*>")).getBefore();
        System.out.println("List: " + list);
        expect.close();
        expect.sendLine("exit");
        process.waitFor();
    }
}
