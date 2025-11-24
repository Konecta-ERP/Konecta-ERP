package com.konecta.identity_service.config;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordGenerator {
    private final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String DIGITS = "0123456789";
    private final String SPECIAL_CHARS = "@#$%^&+=!";
    private final String ALL_CHARS;

    private final int GENERATED_PASSWORD_LENGTH;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordGenerator(int length) {
        GENERATED_PASSWORD_LENGTH = length;
        ALL_CHARS = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARS;
    }

    public String generate() {
        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(getRandomChar(LOWERCASE));
        passwordChars.add(getRandomChar(UPPERCASE));
        passwordChars.add(getRandomChar(DIGITS));
        passwordChars.add(getRandomChar(SPECIAL_CHARS));

        int fillerLength = GENERATED_PASSWORD_LENGTH - passwordChars.size();
        for (int i = 0; i < fillerLength; i++) {
            passwordChars.add(getRandomChar(ALL_CHARS));
        }

        Collections.shuffle(passwordChars, secureRandom);

        return passwordChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private char getRandomChar(String charSet) {
        int randomIndex = secureRandom.nextInt(charSet.length());
        return charSet.charAt(randomIndex);
    }
}