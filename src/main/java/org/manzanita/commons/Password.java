package org.manzanita.commons;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.RandomStringGenerator.Builder;

public class Password {

    private static final RandomStringGenerator GENERATOR = new Builder()
            .withinRange(33, 126)  // ASCII range for printable characters
            .filteredBy(CharacterPredicates.ASCII_ALPHA_NUMERALS)
            .get();

    public static String generate() {
        return GENERATOR.generate(12);
    }

}
