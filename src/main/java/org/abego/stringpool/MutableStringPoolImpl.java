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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class MutableStringPoolImpl implements MutableStringPool {
    private final List<String> allStrings = new ArrayList<>();
    private final Map<String, Integer> stringToId = new HashMap<>();

    private MutableStringPoolImpl() {
    }

    public static MutableStringPool newMutableStringPool() {
        return new MutableStringPoolImpl();
    }

    @Override
    public int add(@Nullable String string) {
        if (string == null) {
            return 0;
        }
        Integer i = stringToId.get(string);
        if (i != null) {
            return i;
        }
        i = allStrings.size()+1;
        allStrings.add(string);
        stringToId.put(string, i);
        return i;
    }

    @Override
    public String getString(int id) {
        @Nullable String result = getStringOrNull(id);
        if (result == null) {
            throw new IllegalArgumentException(
                    "Using id == 0 not allowed as it is mapped to `null`");
        }
        return result;
    }

    @Override
    public @Nullable String getStringOrNull(int id) {
        if (id < 0 || id > allStrings.size()) {
            throw new IllegalArgumentException("Invalid id");
        }
        return id == 0 ? null : allStrings.get(id-1);
    }

    @Override
    public Iterable<String> allStrings() {
        return allStrings;
    }

    @Override
    public Iterable<StringAndID> allStringAndIDs() {
        return stringToId.entrySet().stream().map(e -> new StringAndID() {
            @Override
            public String getString() {
                return e.getKey();
            }

            @Override
            public int getID() {
                return e.getValue();
            }
        }).collect(Collectors.toList());
    }
}
