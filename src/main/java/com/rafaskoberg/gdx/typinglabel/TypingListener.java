
package com.rafaskoberg.gdx.typinglabel;

/** Simple listener for label events. */
public interface TypingListener {

	/** Called each time an {@code EVENT} token is processed.
	 * @param event Name of the event specified in the token. e.g. <tt>{EVENT=player_name}</tt> will have <tt>player_name</tt> as
	 *           argument. */
	public void event (String event);

	/** Called when the char progression reaches the end. */
	public void end ();

}
