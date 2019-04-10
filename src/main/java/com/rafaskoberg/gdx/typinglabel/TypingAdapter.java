
package com.rafaskoberg.gdx.typinglabel;

/** Simple listener for label events. You can derive from this and only override what you are interested in. */
public class TypingAdapter implements TypingListener {

    @Override
    public void event(String event) {
    }

    @Override
    public void end() {
    }

    @Override
    public String replaceVariable(String variable) {
        return null;
    }

    @Override
    public void onChar(Character ch) {
    }

}
