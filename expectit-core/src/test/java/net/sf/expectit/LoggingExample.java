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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

/**
 * An example of logging debug information.
 */
public class LoggingExample {
    public static void main(String[] args) throws IOException {
        enableLogging();
        SocketExample.main(args);
    }

    static ConsoleHandler enableLogging() {
        final Logger logger = Logger.getLogger("net.sf.expectit");
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        logger.addHandler(handler);
        logger.setLevel(Level.FINE);
        return handler;
    }

    static void disableLogging(ConsoleHandler handler) {
        final Logger logger = Logger.getLogger("net.sf.expectit");
        handler.setLevel(Level.INFO);
        logger.setLevel(Level.INFO);
    }
}
