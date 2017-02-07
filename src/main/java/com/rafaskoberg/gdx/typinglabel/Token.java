
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
	
	// Effects
	SHAKE         ("SHAKE"),
	ENDSHAKE      ("ENDSHAKE"),
	WAVE          ("WAVE"),
	ENDWAVE       ("ENDWAVE"),
	JUMP          ("JUMP"),
	ENDJUMP       ("ENDJUMP"),
	
	// Private
	SKIP          ("SKIP");
	// @on

	final String name;

	private Token (String name) {
		this.name = name;
	}

	@Override
	public String toString () {
		return name;
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
