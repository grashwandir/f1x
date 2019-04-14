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
package org.f1x.util;

import java.nio.charset.StandardCharsets;

public class AsciiUtils {

    public static final byte NULL = (byte) 0; // NULL
    public static final byte SOH = (byte) 1; // FIX field separator
    public static final byte NL = (byte) 10; // New line
    public static final byte PIPE = (byte) 124; // PIPE

    public static byte[] getBytes(final String asciiText) {
        return asciiText.getBytes(StandardCharsets.US_ASCII);
    }

    public static byte[] getBytes(final CharSequence asciiText) {
        final int length = asciiText.length();
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) asciiText.charAt(i);
        }
        return bytes;
    }

    public static boolean equals(byte[] array1, byte[] array2, int offset2, int length) {
        for (int i = 0; i < length; i++) {
            if (array1[i] != array2[i + offset2]) {
                return false;
            }
        }
        return true;
    }

}
