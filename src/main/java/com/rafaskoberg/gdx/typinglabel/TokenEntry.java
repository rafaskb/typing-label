
package com.rafaskoberg.gdx.typinglabel;

import com.rafaskoberg.gdx.typinglabel.effects.Effect;

/** Container representing a token, parsed parameters and its position in text. */
class TokenEntry implements Comparable<TokenEntry> {
	Token token;
	int index;
	float floatValue;
	String stringValue;
	Effect effect;
	
	TokenEntry (Token token, int index, float floatValue, String stringValue) {
		this.token = token;
		this.index = index;
		this.floatValue = floatValue;
		this.stringValue = stringValue;
	}

	TokenEntry (Token token, int index, float floatValue) {
		this.token = token;
		this.index = index;
		this.floatValue = floatValue;
	}

	TokenEntry (Token token, int index, String stringValue) {
		this.token = token;
		this.index = index;
		this.stringValue = stringValue;
	}

	@Override
	public int compareTo (TokenEntry o) {
		return Integer.compare(index, o.index);
	}

}
