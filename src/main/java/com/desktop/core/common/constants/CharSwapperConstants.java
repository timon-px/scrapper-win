package com.desktop.core.common.constants;

import java.util.HashMap;
import java.util.Map;

public class CharSwapperConstants {
    // Cyrillic to Latin mapping
    public static final Map<Character, Character> CYRILLIC_TO_LATIN = new HashMap<>();
    // Latin to Cyrillic mapping (reverse)
    public static final Map<Character, Character> LATIN_TO_CYRILLIC = new HashMap<>();

    private static final Map<Character, Character> SMALL_CHARS_MAP = Map.of(
            'а', 'a',
            'е', 'e',
            'о', 'o',
            'с', 'c',
            'х', 'x',
            'і', 'i'
    );
    private static final Map<Character, Character> LARGE_CHARS_MAP = Map.of(
            'А', 'A',
            'В', 'B',
            'Е', 'E',
            'М', 'M',
            'Н', 'H',
            'О', 'O',
            'Р', 'P',
            'С', 'C',
            'Т', 'T',
            'Х', 'X'
    );

    static {
        // Initialize Cyrillic to Latin mapping
        CYRILLIC_TO_LATIN.putAll(SMALL_CHARS_MAP);
        CYRILLIC_TO_LATIN.putAll(LARGE_CHARS_MAP);

        // Initialize reverse mapping (Latin to Cyrillic)
        for (Map.Entry<Character, Character> entry : CYRILLIC_TO_LATIN.entrySet()) {
            LATIN_TO_CYRILLIC.put(entry.getValue(), entry.getKey());
        }
    }
}
