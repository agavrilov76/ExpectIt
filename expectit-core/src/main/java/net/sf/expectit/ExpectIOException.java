package net.sf.expectit;

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
 * An exception which occurs if an expect operation has failed.
 */
public class ExpectIOException extends IOException {
    private final String inputBuffer;
    /**
     * Creates a new exception instance with the given error message.
     * @param message the message.
     * @param inputBuffer the input string buffer.
     */
    public ExpectIOException(final String message, final String inputBuffer) {
        super(message);
        this.inputBuffer = inputBuffer;
    }

    /**
     * Retrieves the input data buffer when the exception occurs.
     * @return the input string buffer.
     */
    public String getInputBuffer() {
        return inputBuffer;
    }
}
