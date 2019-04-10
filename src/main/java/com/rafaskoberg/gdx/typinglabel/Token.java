
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
	SICK          ("SICK"),
	ENDSICK       ("ENDSICK"),
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

	/** Returns whether or not this is a speed token. */
	boolean isSpeed () {
		switch (this) {
		case SPEED:
		case SLOWER:
		case SLOW:
		case NORMAL:
		case FAST:
		case FASTER:
			return true;
		default:
			return false;
		}
	}

	/** Returns whether or not this is an effect token. */
	boolean isEffect () {
		switch (this) {
		case JUMP:
		case ENDJUMP:
		case SICK:
		case ENDSICK:
		case SHAKE:
		case ENDSHAKE:
		case WAVE:
		case ENDWAVE:
			return true;
		default:
			return false;
		}
	}

	/** Returns whether or not this is an effect start token. */
	boolean isEffectStart () {
		switch (this) {
		case JUMP:
		case SHAKE:
		case SICK:
		case WAVE:
			return true;
		default:
			return false;
		}
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
