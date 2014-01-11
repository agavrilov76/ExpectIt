package net.sf.expectit;

import java.io.IOException;
import java.net.Socket;
import java.net.URLConnection;

import static net.sf.expectit.echo.EchoAdapters.adapt;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

/**
 * Socket example.
 */
public class SocketExample {
    public static void main(String[] args) throws IOException {
        // try-with-resources omitted
        Socket socket = new Socket("google.com", 80);
        Expect expect = new ExpectBuilder()
                .withInputs(socket.getInputStream())
                .withOutput(socket.getOutputStream())
       //         .withEchoOutput(adapt(System.err))
                .build();
        expect.sendLine("GET");
        String result = expect.expect(contains("\n")).getBefore();
        System.out.println("Result: " + result);
        expect.expect(contains("<H1>302 Moved</H1>"));
        String url = expect.expect(regexp("<A HREF=\"([^\"]*)")).group(1);
        System.out.println("Redirect url from html: " + url);
        // finally omitted
        expect.close();
        socket.close();
    }
}
