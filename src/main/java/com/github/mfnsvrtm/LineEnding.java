package com.github.mfnsvrtm;

enum LineEnding {
    WIN,
    MAC,
    NIX;

    String stringValue() {
        switch (this) {
            case WIN:
                return "\r\n";
            case MAC:
                return "\r";
            case NIX:
                return "\n";
        }
        throw new RuntimeException("Unexpected LineEnding enum variant.");
    }
}
