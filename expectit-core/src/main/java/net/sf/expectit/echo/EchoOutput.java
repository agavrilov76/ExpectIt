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
 * A callback interface for capturing input and output from expect operations.
 */
@Deprecated
public interface EchoOutput {
    /**
     * A callback method called when the data is received from the given input.
     *
     * @param input  the input number starting from 0
     * @param string the input data converted from bytes using the charset specified in the
     *               {@link net.sf.expectit.ExpectBuilder#withCharset(java.nio.charset.Charset)}
     *               method
     * @throws IOException if an I/O error occurs
     */
    void onReceive(int input, String string) throws IOException;

    /**
     * A callback method called when the data has just been sent to the output.
     *
     * @param string the received data converted from bytes using the charset specified in the
     *               {@link net.sf.expectit.ExpectBuilder#withCharset(java.nio.charset.Charset)}
     *               method
     * @throws IOException if an I/O error occurs
     */
    void onSend(String string) throws IOException;
}
