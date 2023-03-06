package org.abego.stringpool;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MutableStringPoolImplTest {

    @Test
    void addTwice() {
        MutableStringPool pool = StringPools.newMutableStringPool();

        int i = pool.add("foo");
        int j = pool.add("foo");

        assertEquals(i, j);
        assertNotEquals(0, j);
    }
    @Test
    void addNull() {
        MutableStringPool pool = StringPools.newMutableStringPool();

        int i = pool.add(null);
        
        assertEquals(0,i);
    }

    @Test
    void getStringZero() {
        MutableStringPool pool = StringPools.newMutableStringPool();

        assertThrowsWithMessage(IllegalArgumentException.class, 
                "Using id == 0 not allowed as it is mapped to `null`",
                ()->pool.getString(0));
    }

    @Test
    void getString() {
        MutableStringPool pool = StringPools.newMutableStringPool();
        int i = pool.add("foo");

        String s = pool.getString(i);

        assertEquals("foo", s);
        assertNotEquals(0, i);
    }

    @Test
    void getStringOrNull() {
        MutableStringPool pool = StringPools.newMutableStringPool();
        int i = pool.add("foo");

        @Nullable String s = pool.getStringOrNull(i);

        assertEquals("foo", s);
        assertNotEquals(0, i);
        assertNull(pool.getStringOrNull(0));
        assertThrowsWithMessage(IllegalArgumentException.class, "Invalid id", 
                () -> pool.getString(-1));
        assertThrowsWithMessage(IllegalArgumentException.class, "Invalid id",
                () -> pool.getString(2));
    }

    @Test
    void allStrings() {
        MutableStringPool pool = StringPools.newMutableStringPool();
        pool.add("foo");
        pool.add("foo");
        pool.add("bar");

        Iterable<String> iterable = pool.allStrings();

        assertEquals("foo,bar", String.join(",", iterable));
    }
    @Test
    void allStringAndIDs() {
        MutableStringPool pool = StringPools.newMutableStringPool();
        pool.add("foo");
        pool.add("foo");
        pool.add("bar");

        List<String> items = new ArrayList<>();
        for (StringPool.StringAndID item:pool.allStringAndIDs()) {
            items.add(item.getString()+" "+item.getID());
        }

        Collections.sort(items);
        assertEquals("bar 2,foo 1",String.join(",",items));
    }

    private static <T extends Throwable> void assertThrowsWithMessage(
            Class<T> expectedType, String expectedMessage, Executable executable) {
        T e = assertThrows(expectedType,executable);
        assertEquals(expectedMessage,e.getMessage());
    }

}
