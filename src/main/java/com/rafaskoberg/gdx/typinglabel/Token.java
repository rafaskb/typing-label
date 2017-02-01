
package com.rafaskoberg.gdx.typinglabel;

enum Token {
	// @off
	// Public
	WAIT          ("WAIT"),
	SPEED         ("SPEED"),
	SLOWER        ("SLOWER"),
	SLOW          ("SLOW"),
	NORMAL        ("NORMAL"),
	FAST          ("FAST"),
	FASTER        ("FASTER"),
	COLOR         ("COLOR"),
	CLEARCOLOR    ("CLEARCOLOR"),
	VAR           ("VAR"),
	EVENT         ("EVENT"),
	RESET         ("RESET"),
	
	// Private
	SKIP          ("SKIP");
	// @on

	final String name;

	private Token (String name) {
		this.name = name;
	}

	static Token fromName (String name) {
		if (name != null) {
			for (Token token : values()) {
				if (name.equalsIgnoreCase(token.name)) {
					return token;
				}
			}
		}
		return null;
	}
}
