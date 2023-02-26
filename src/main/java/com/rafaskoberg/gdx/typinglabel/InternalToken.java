
package com.rafaskoberg.gdx.typinglabel;

enum InternalToken {
    // @formatter:off
	// Public
	WAIT          ("WAIT",          TokenCategory.WAIT     ),
	SPEED         ("SPEED",         TokenCategory.SPEED    ),
	SLOWER        ("SLOWER",        TokenCategory.SPEED    ),
	SLOW          ("SLOW",          TokenCategory.SPEED    ),
	NORMAL        ("NORMAL",        TokenCategory.SPEED    ),
	FAST          ("FAST",          TokenCategory.SPEED    ),
	FASTER        ("FASTER",        TokenCategory.SPEED    ),
	COLOR         ("COLOR",         TokenCategory.COLOR    ),
	CLEARCOLOR    ("CLEARCOLOR",    TokenCategory.COLOR    ),
	ENDCOLOR      ("ENDCOLOR",      TokenCategory.COLOR    ),
	VAR           ("VAR",           TokenCategory.VARIABLE ),
	IF            ("IF",            TokenCategory.IF       ),
	EVENT         ("EVENT",         TokenCategory.EVENT    ),
	RESET         ("RESET",         TokenCategory.RESET    ),
	SKIP          ("SKIP",          TokenCategory.SKIP     );
	// @formatter:on

    final String        name;
    final TokenCategory category;

    private InternalToken(String name, TokenCategory category) {
        this.name = name;
        this.category = category;
    }

    @Override
    public String toString() {
        return name;
    }

    static InternalToken fromName(String name) {
        if(name != null) {
            for(InternalToken token : values()) {
                if(name.equalsIgnoreCase(token.name)) {
                    return token;
                }
            }
        }
        return null;
    }
}
