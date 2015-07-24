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

import net.sf.expectit.Result;

/**
 * The interact result builder.
 * @param <R> the result type.
 */
public interface OngoingResult<R extends Result> {
    /**
     * Sets the action if the interact matcher matches. The action are performed in the order they
     * are declared.
     * @param action the action to perform.
     * @return the parent interact builder.
     */
    InteractBuilder then(Action<R> action);
}
