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
package org.f1x.util.parse;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

public class Test_NumbersParser {

    //@Test takes too long
    public void testAllIntNumbers() {
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
            assertIntParser(i);
        }
    }

    @Test
    public void testSelectedNumbers() {
        assertIntParser(Integer.MIN_VALUE);
        assertIntParser(Integer.MIN_VALUE / 2);
        assertIntParser(0);
        assertIntParser(Integer.MAX_VALUE / 2);
        assertIntParser(Integer.MAX_VALUE);

        assertLongParser(Long.MIN_VALUE);
        assertLongParser(Long.MIN_VALUE / 2);
        assertLongParser(0);
        assertLongParser(Long.MAX_VALUE / 2);
        assertLongParser(Long.MAX_VALUE);

        assertDoubleParser(-0.1);
        assertDoubleParser(0);
        assertDoubleParser(0.1);
        assertDoubleParser(Math.PI);
        assertDoubleParser(3.14159);

    }

    @Test
    public void badInputs() {
        assertBadIntNumber("");
        assertBadIntNumber(" 123");
        assertBadIntNumber("123 ");
        assertBadIntNumber("1-");
        assertBadIntNumber("1-2");

        assertBadLongNumber("");
        assertBadLongNumber(" 123");
        assertBadLongNumber("123 ");
        assertBadLongNumber("1-");
        assertBadLongNumber("1-2");
    }

    @Test
    public void goodInputs() {
        assertIntNumber("00001", 1); // leading zeros are allowed
        assertIntNumber("-00001", -1); // leading zeros are allowed
        assertIntNumber("-0", 0);

        assertLongNumber("00001", 1); // leading zeros are allowed
        assertLongNumber("-00001", -1); // leading zeros are allowed
        assertLongNumber("-0", 0);
    }

    @Test
    public void intOKBoundaries() {
        final String minStr = "-2147483648";
        assertEquals(NumbersParser.parseInt1(minStr.getBytes(), 0, minStr.length()), NumbersParser.parseInt(minStr, 0, minStr.length()));
        assertEquals(NumbersParser.parseInt(minStr, 0, minStr.length()), Integer.MIN_VALUE);

        final String maxStr = "2147483647";
        assertEquals(NumbersParser.parseInt1(maxStr.getBytes(), 0, maxStr.length()), NumbersParser.parseInt(maxStr, 0, maxStr.length()));
        assertEquals(NumbersParser.parseInt(maxStr, 0, maxStr.length()), Integer.MAX_VALUE);
    }

    @Test
    public void intBadBoundaries() {
        final String minStrPlus1 = "-2147483649";
        assertBadIntNumber(minStrPlus1);

        final String maxStrPlus1 = "2147483648";
        assertBadIntNumber(maxStrPlus1);
    }

    @Test
    public void intBadBoundariesPlus1() {
        final String minStrPlus1 = "-2147483650";
        assertBadIntNumber(minStrPlus1);

        final String maxStrPlus1 = "2147483649";
        assertBadIntNumber(maxStrPlus1);
    }

    @Test
    public void longOKBoundaries() {
        final String minStr = "-9223372036854775808";
        assertEquals(NumbersParser.parseLong1(minStr.getBytes(), 0, minStr.length()), NumbersParser.parseLong(minStr, 0, minStr.length()));
        assertEquals(NumbersParser.parseLong(minStr, 0, minStr.length()), Long.MIN_VALUE);

        final String maxStr = "9223372036854775807";
        assertEquals(NumbersParser.parseLong1(maxStr.getBytes(), 0, maxStr.length()), NumbersParser.parseLong(maxStr, 0, maxStr.length()));
        assertEquals(NumbersParser.parseLong(maxStr, 0, maxStr.length()), Long.MAX_VALUE);
    }

    @Test
    public void longBadBoundaries() {
        final String minStrPlus1 = "-9223372036854775809";
        assertBadLongNumber(minStrPlus1);

        final String maxStrPlus1 = "9223372036854775808";
        assertBadLongNumber(maxStrPlus1);
    }

    @Test
    public void longBadBoundariesPlus1() {
        final String minStrPlus1 = "-9223372036854775810";
        assertBadLongNumber1(minStrPlus1);

        final String maxStrPlus1 = "9223372036854775809";
        assertBadLongNumber1(maxStrPlus1);
    }

    @Test
    public void tooLargeIntegers() {
        assertBadIntNumber("1234567890123456789012345678901234567890");
        assertBadLongNumber("1234567890123456789012345678901234567890");
    }

    @Test
    public void tooLargeIntegers1() {
        assertBadIntNumber1("1234567890123456789012345678901234567890");
        assertBadLongNumber1("1234567890123456789012345678901234567890");
    }

    private static void assertBadIntNumber(String value) {
        final byte[] valueBytes = value.getBytes();
        System.out.println("wraped: " + Arrays.toString(wrap(valueBytes)));
        try {
            int parsedValue = NumbersParser.parseInt(valueBytes, 1, valueBytes.length);
            parsedValue = NumbersParser.parseInt(wrap(valueBytes), 1, valueBytes.length);
            fail("Parser was expected to fail on \"" + value + "\" but instead it produced: " + parsedValue);
        } catch (Exception expected) {
        }
    }

    private static void assertBadLongNumber(String value) {
        final byte[] valueBytes = value.getBytes();
        System.out.println("wraped: " + Arrays.toString(wrap(valueBytes)));
        try {
            long parsedValue = NumbersParser.parseLong(valueBytes, 1, valueBytes.length);
            parsedValue = NumbersParser.parseLong(wrap(valueBytes), 1, valueBytes.length);
            fail("Parser was expected to fail on \"" + value + "\" but instead it produced: " + parsedValue);
        } catch (Exception expected) {
        }
    }

    private static void assertBadIntNumber1(String value) {
        final byte[] valueBytes = value.getBytes();
        System.out.println("wraped: " + Arrays.toString(wrap(valueBytes)));
        try {
            int parsedValue = NumbersParser.parseInt1(valueBytes, 0, valueBytes.length);
            parsedValue = NumbersParser.parseInt1(wrap(valueBytes), 0, valueBytes.length);
            fail("Parser was expected to fail on \"" + value + "\" but instead it produced: " + parsedValue);
        } catch (Exception expected) {
        }
    }

    private static void assertBadLongNumber1(String value) {
        final byte[] valueBytes = value.getBytes();
        System.out.println("wraped: " + Arrays.toString(wrap(valueBytes)));
        try {
            long parsedValue = NumbersParser.parseLong1(valueBytes, 0, valueBytes.length);
            parsedValue = NumbersParser.parseLong1(wrap(valueBytes), 0, valueBytes.length);
            fail("Parser was expected to fail on \"" + value + "\" but instead it produced: " + parsedValue);
        } catch (Exception expected) {
        }
    }

    private static void assertIntNumber(String value, int expectedValue) {
        byte[] valueBytes = value.getBytes();
        int parsedValue = NumbersParser.parseInt(wrap(valueBytes), 1, valueBytes.length);
        assertEquals(expectedValue, parsedValue);
    }

    private static void assertIntParser(int number) {
        String value = Integer.toString(number);
        byte[] valueBytes = value.getBytes();
        int parsedValue = NumbersParser.parseInt(wrap(valueBytes), 1, valueBytes.length);
        assertEquals(number, parsedValue);
    }

    private static void assertLongNumber(String value, long expectedValue) {
        byte[] valueBytes = value.getBytes();
        long parsedValue = NumbersParser.parseLong(wrap(valueBytes), 1, valueBytes.length);
        assertEquals(expectedValue, parsedValue);
    }

    private static void assertLongParser(long number) {
        String value = Long.toString(number);
        byte[] valueBytes = value.getBytes();
        long parsedValue = NumbersParser.parseLong(wrap(valueBytes), 1, valueBytes.length);
        assertEquals(number, parsedValue);
    }

    private static void assertDoubleParser(double number) {
        String value = Double.toString(number);
        byte[] valueBytes = value.getBytes();

        double parsedValue = NumbersParser.parseDouble(wrap(valueBytes), 1, valueBytes.length);
        assertEquals(number, parsedValue, .00001);
    }

    private static byte[] wrap(byte[] valueBytes) {
        //to make it more interesting
        byte[] result = new byte[valueBytes.length + 2];
        Arrays.fill(result, 0, result.length, (byte) '9');
        System.arraycopy(valueBytes, 0, result, 1, valueBytes.length);
        return result;
    }

}
