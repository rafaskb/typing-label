package com.rafaskoberg.gdx.typinglabel;

/**
 * Enum that lists all supported delimiters for tokens.
 */
public enum TokenDelimiter {
    /** <code>{TOKEN}</code> */
    CURLY_BRACKETS('{', '}'),

    /** <code>[TOKEN]</code> */
    BRACKETS('[', ']'),

    /** <code>(TOKEN)</code> */
    PARENTHESES('(', ')');

    public final char open;
    public final char close;

    TokenDelimiter(char open, char close) {
        this.open = open;
        this.close = close;
    }
}
