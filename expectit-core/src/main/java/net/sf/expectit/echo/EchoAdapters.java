package net.sf.expectit.echo;

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

import java.io.IOException;

/**
 * Echo adapter factory.
 */
public final class EchoAdapters {

    private EchoAdapters() {
    }

    /**
     * Creates an instance of {@link EchoOutput} which appends all the input and output data to the given appender.
     *
     * @param appendable the delegate
     * @return the instance
     */
    public static EchoOutput adapt(final Appendable appendable) {
        return new EchoOutput() {
            @Override
            public void onReceive(int input, String string) throws IOException {
                appendable.append(string);
            }

            @Override
            public void onSend(String string) throws IOException {
                appendable.append(string);
            }
        };
    }

    public static Appendable adaptInput(final int input, final EchoOutput echoOutput) {
        return new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException {
                echoOutput.onReceive(input, csq.toString());
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public Appendable append(char c) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static Appendable adaptOutput(final EchoOutput echoOutput) {
        return new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException {
                echoOutput.onSend(csq.toString());
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public Appendable append(char c) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }
}
