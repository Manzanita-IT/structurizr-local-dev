package org.manzanita.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordTest {

    @Test
    void generatesAlphanumeric12CharPasswords() {
        String p = Password.generate();
        assertEquals(12, p.length());
        assertTrue(p.matches("[A-Za-z0-9]{12}"));
    }
}
