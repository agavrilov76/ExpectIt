package net.sf.expectit.filter;

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

/**
 * A filter interface applied to the input before performing expect operations.
 *
 * @author Alexey Gavrilov
 */
public interface Filter {
    /**
     * Invoked when a string read from the input is about to be appended to the input buffer.
     * This string is passed
     * as a parameter to this method, and the return value is actually appended.
     * <p/>
     * Allows to modify the matching input by providing the return value which differs from the
     * given string. The
     * method is invoked on the thread that performs expect operations.
     *
     * @param string a chunk of input data read from the input stream. Can not be {@code null}.
     *               If the filter
     *               works in the {@link Filters#chain(Filter...)}, then the string is the result
     *               of preceding filter
     * @param buffer the reference to the input buffer. Can be used to modify the entire buffer
     *               contents.
     * @return the string to be appended to the input buffers, or {@code null} to ignore all the
     * consequent filters in
     * the filter chain.
     */
    String beforeAppend(String string, StringBuilder buffer);

    /**
     * Invoked when the input string has just been appended to the input buffer.
     * <p/>
     * The method is invoked on the thread that performs expect operations.
     *
     * @param buffer the reference to the input buffer. Can be used to modify the entire buffer
     *               contents.
     * @return a boolean flag indicating whether the filtering process should be stopped here.
     * {@code true} if all the
     * consequent filters must not be executed, or {@code false} otherwise.
     */
    boolean afterAppend(StringBuilder buffer);

    /**
     * Indicates if the filter is enabled or disabled.
     *
     * @return {@code true} if the filter is enabled, or {@code false} otherwise.
     */
    boolean isEnabled();

    /**
     * Enables or disables the filter for the input.
     * <p/>
     * Allows to switch filter on and off while working with the expect instance.
     *
     * @param enabled if {@code true} then the filter will be enabled, or {@code false} otherwise.
     */
    void setEnabled(boolean enabled);
}
