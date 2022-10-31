package com.github.mfnsvrtm;

enum LineEnding {
    WIN("\r\n"),
    MAC("\r"),
    NIX("\n");

    private final String value;

    LineEnding(String value) {
        this.value = value;
    }

    String stringValue() {
        return value;
    }
}
