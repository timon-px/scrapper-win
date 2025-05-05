package com.desktop.core.uniqueizer;

import static com.desktop.core.common.constants.CharSwapperConstants.CYRILLIC_TO_LATIN;
import static com.desktop.core.common.constants.CharSwapperConstants.LATIN_TO_CYRILLIC;

public class CharSwapper {
    public static String ConvertChars(String input) {
        if (input == null) return null;

        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            char newChar = isLatin(ch) ?
                    LATIN_TO_CYRILLIC.getOrDefault(ch, ch)
                    : CYRILLIC_TO_LATIN.getOrDefault(ch, ch);

            result.append(newChar);
        }
        return result.toString();
    }

    // New method to check if a character is Latin based on our mapping
    private static boolean isLatin(char c) {
        // If the character is in LATIN_TO_CYRILLIC values, it's a Latin character from our mapping
        return LATIN_TO_CYRILLIC.containsKey(c);
    }
}
