package net.sf.expectit.java8;

import java.io.IOException;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.SimpleResult;

/**
 * An example of using a lambda as a matcher.
 */
public class Java8Example {
    public static void main(String[] args) throws IOException {
        try (final Expect expect = new ExpectBuilder()
                .withInputs(System.in)
                .build()) {
            Result result = expect.expect((input, isEof) -> SimpleResult.valueOf(isEof, input, ""));
            System.out.println(result.getBefore());
        }
    }
}
