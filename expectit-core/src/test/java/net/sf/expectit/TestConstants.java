package net.sf.expectit;

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
 * Constants used in the tests.
 */
public final class TestConstants {
    private TestConstants() {
    }

    /**
     * A small timeout used for the tests that block until an expect operation finishes.
     */
    public static final long SMALL_TIMEOUT = Long.getLong(TestConstants.class.getName(), 200);

    /**
     * A long timeout which should cause tests to fail.
     */
    public static final long LONG_TIMEOUT = Long.getLong(TestConstants.class.getName(), 1000);

}
