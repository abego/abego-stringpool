/*
 * MIT License
 *
 * Copyright (c) 2022 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringpool;


import org.eclipse.jdt.annotation.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A StringPool implementation using a compact String representation
 */
final class StringPoolDefault implements StringPool {
    /**
     * The {@link Charset} used to encode the string text.
     */
    static final Charset CHARSET_FOR_STRING_TEXT = StandardCharsets.UTF_8;

    //TODO: review the format description as we now also support joined strings
    /**
     * The bytes holding all strings of this {@link StringPool}.
     *
     * <p>The ID of a string is an offset into this byte array. {@code 0},
     * i.e. the ID for a {@code null} String, refers to the first byte of the
     * array, that is just a placeholder. All other Strings follow, with in the
     * following format:
     *
     * <pre>
     *     1-5 bytes The number of bytes of the UTF-8 encoded string ("n"), stored in VLQ encoding (<a href="https://en.wikipedia.org/wiki/Variable-length_quantity">VLQ in Wikipedia</a>)
     *     n bytes   Bytes representing the String's text, in UTF-8 encoding (CHARSET_FOR_STRING_TEXT).
     * </pre>
     */
    private final byte[] bytes;

    private StringPoolDefault(byte[] bytes) {
        this.bytes = bytes;
    }

    public static StringPoolDefault newStringPoolDefault(byte[] bytes) {
        return new StringPoolDefault(bytes);
    }

    @Override
    @Nullable
    public String getStringOrNull(int id) {
        if (id == 0) {
            return null;
        }
        int[] offsetVar = new int[]{id};
        return readStringAtOffset(offsetVar);
    }

    public Iterable<String> allStrings() {
        return () -> {
            int[] offsetVar = new int[]{1};
            return new Iterator<String>() {

                @Override
                public boolean hasNext() {
                    return offsetVar[0] < bytes.length;
                }

                @Override
                public String next() {
                    if (!hasNext()) throw new NoSuchElementException();

                    return readStringAtOffset(offsetVar);
                }
            };
        };
    }


    public Iterable<StringAndID> allStringAndIDs() {
        return () -> {
            int[] offsetVar = new int[]{1};
            return new Iterator<StringAndID>() {

                @Override
                public boolean hasNext() {
                    return offsetVar[0] < bytes.length;
                }

                @Override
                public StringAndID next() {
                    if (!hasNext()) throw new NoSuchElementException();

                    int id = offsetVar[0];
                    String s = readStringAtOffset(offsetVar);
                    return new StringAndID() {
                        @Override
                        public String getString() {
                            return s;
                        }

                        @Override
                        public int getID() {
                            return id;
                        }
                    };
                }
            };
        };
    }

    @Override
    public boolean isByteAccessSupported() {
        return true;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    private String readStringAtOffset(int[] offsetVar) {
        VLQUtil.ByteSupplier readByte = () -> bytes[offsetVar[0]++];

        int n = VLQUtil.decodeSignedIntFromVLQ(readByte);
        if (n >= 0) {
            String result = new String(bytes, offsetVar[0], n, CHARSET_FOR_STRING_TEXT);
            offsetVar[0] += n;
            return result;
        } else {
            int nParts = -n;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < nParts; i++) {
                int partID = VLQUtil.decodeUnsignedIntFromVLQ(readByte);
                int[] partIDVar = new int[]{partID};
                builder.append(readStringAtOffset(partIDVar));
            }
            return builder.toString();
        }
    }
}
