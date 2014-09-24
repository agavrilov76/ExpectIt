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

import static net.sf.expectit.matcher.Matchers.contains;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.lang.management.ManagementFactory;
import java.nio.channels.Selector;
import java.util.concurrent.TimeUnit;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.junit.Assume;
import org.junit.Test;

/**
 * Verifies that there is no resource leaks after using an instance of Expect.
 */
public class ResourceLeakTest {

    /**
     * Gets the number of the open file descriptors via JMX.s
     */
    private long getOpenFileDescriptorCount() throws Exception {
        ObjectName os = new ObjectName("java.lang:type=OperatingSystem");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            return (Long) server.getAttribute(os, "OpenFileDescriptorCount");
        } catch (AttributeNotFoundException ok) {
            Assume.assumeNoException("OS doesn't support ope file descriptors parameter", ok);
            throw ok;
        }
    }

    @Test
    public void testFileDescriptorLeak() throws Exception {
        // initialize the NIO subsystem before using the Expect.
        Selector.open().close();

        long before = getOpenFileDescriptorCount();
        for (int i = 0; i < 100; i++) {
            String string = "abc" + i + "def";
            ByteArrayInputStream input = new ByteArrayInputStream(string.getBytes());
            Expect expect = new ExpectBuilder()
                    .withInputs(input)
                    .withTimeout(Utils.SMALL_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build();
            expect.expect(contains(i + "d"));
            // close input and the expect instance
            input.close();
            expect.close();
        }
        long after = getOpenFileDescriptorCount();
        // the numbers of the open file descriptors must be the same after
        // closing the expect instance.
        assertEquals(before, after);
    }
}
