package net.sf.expectit.interact;

/*
 * #%L
 * ExpectIt
 * %%
 * Copyright (C) 2014 - 2015 Alexey Gavrilov and contributors
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
import net.sf.expectit.Result;

/**
 * The interact loop callback.
 *
 * @param <R> the result type.
 */
public interface Action<R extends Result> {
    /**
     * The method is called when the interact condition matches.
     *
     * @param result the matching result.
     * @throws IOException if I/O error occurs.
     */
    void apply(R result) throws IOException;
}
