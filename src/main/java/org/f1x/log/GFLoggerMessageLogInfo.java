/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.f1x.log;

import org.f1x.util.ByteArrayReference;
import org.gflogger.GFLog;
import org.gflogger.GFLogFactory;

import java.io.IOException;

/**
 * Stores FIX messages into GF Logger
 */
public class GFLoggerMessageLogInfo implements MessageLog {

    private final GFLog LOGGER;
    //TODO: Ask gflogger dev to add GFLog.append(byte[], offset, length) to avoid this
    private final ThreadLocal<ByteArrayReference> byteSequences = new ThreadLocal<>();

    private final LogFormatter formatter;

    public GFLoggerMessageLogInfo(String name) {
        this(name, null);
    }

    public GFLoggerMessageLogInfo(String name, LogFormatter formatter) {
        this.formatter = formatter;
        this.LOGGER = GFLogFactory.getLog(name);
    }

    @Override
    public void log(boolean isInbound, byte[] buffer, int offset, int length) {
        if (LOGGER.isInfoEnabled()) {
            ByteArrayReference byteSequence = byteSequences.get();
            if (byteSequence == null) {
                byteSequence = new ByteArrayReference(0);
                byteSequences.set(byteSequence);
            } else {
                byteSequence.reset();
            }
            if (formatter != null) {
                try {
                    formatter.log(isInbound, buffer, offset, length, byteSequence);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                byteSequence.set(buffer, offset, length);
            }
        LOGGER.info().append(byteSequence).commit();
            // LOGGER.info(byteSequence.toString());
        }
    }

    @Override
    public void close() throws IOException {
        // does nothing
    }
}
