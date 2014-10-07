package net.sf.expectit.java8;

/*
 * #%L
 * ExpectIt
 * %%
 * Copyright (C) 2014 Alexey Gavrilov and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static net.sf.expectit.matcher.SimpleResult.failure;
import static net.sf.expectit.matcher.SimpleResult.success;

import java.io.IOException;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;

/**
 * An example of using a lambda as a matcher.
 */
public class Java8Example {
    public static void main(String[] args) throws IOException {
        try (final Expect expect = new ExpectBuilder()
                .withInputs(System.in)
                .build()) {
            Result result = expect.expect((input, isEof) -> isEof
                    ? success(input, input, "")
                    : failure(input));
            System.out.println(result.getBefore());
        }
    }
}
