package net.sf.expectit.filter;

/*
 * #%L
 * net.sf.expectit
 * %%
 * Copyright (C) 2014 Alexey Gavrilov
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

/**
 * A filter interface applied to input before performing expect operations.
 *
 * @author Alexey Gavrilov
 */
public interface Filter {
    /**
     * Invoked when a string read from the input is about to be appended to the input buffer. This string is passed
     * as a parameter to this method, and the result is actually appended.
     * <p/>
     * Allows to modify the matching input by providing the return value which differs from the given string. The
     * method is invoked on the thread that performs an expect operation.
     *
     * @param string a chunk of input data read from the input stream. Can be {@code null} if the filter is run in a
     *               with in a filter chain, and preceding filter returns {@code null}
     * @param buffer the reference to the input buffer. Can be used to modify the entire buffer contents.
     * @return the string to be appended to the input buffers, or {@code null} to ignore all the consequent filters.
     */
    String beforeAppend(String string, StringBuilder buffer);

    /**
     * Invoked when the input string has just been appended to the input buffer.
     * <p/>
     * The method is invoked on the thread that performs an expect operation.
     *
     * @param buffer the reference to the input buffer. Can be used to modify the entire buffer contents.
     * @return a boolean flag indicating whether the filtering process should be stopped or not. {@code true} if all the
     *                  consequent filters must not be executed, or {@code false} otherwise.
     */
    boolean afterAppend(StringBuilder buffer);

    /**
     * Indicates if the filter is switched off.
     *
     * @return the filter off flag.
     */
    boolean isOff();

    /**
     * Switches the filter on and off for the consequent match operations.
     *
     * @param off if {@code true} then the filter will be switched off, {@code false} otherwise.
     */
    void setOff(boolean off);
}
