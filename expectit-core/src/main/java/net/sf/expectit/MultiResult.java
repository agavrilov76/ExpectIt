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

import java.util.List;

/**
 * A representation of the result of multiple match operations.
 * <p>The result of the one operation is selected by implementation to represent all the other
 * operations
 * and fulfill the contract of the {@code Result} super interface.
 * <p/>
 * <p>For example, the result can represent a combination of
 * match operations, while the match with the greatest end position is selected to implement the
 * super interface.
 */
public interface MultiResult extends Result {
    /**
     * Returns an unmodifiable list of the result instances of all the performed match operations
     * . The list includes
     * the result of the one operation chosen to represent the others.
     * <p/>
     * The order of the elements should correspond to the order in which the match operations
     * have been performed.
     * Never returns <code>null</code>.
     *
     * @return an unmodifiable list of all the match results
     */
    List<Result> getResults();
}
