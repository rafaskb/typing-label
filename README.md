# TypingLabel

<!-- Shields -->

A libGDX Label that appears as if it was being typed in real time.

It works as a drop-in replacement for normal [Scene2D Labels](https://github.com/libgdx/libgdx/wiki/Scene2d.ui#label), and you can use optional [tokens](#tokens) to customize the text's behavior.

<!-- GIF -->


## Usage

Simple example with variables:
```java
// Create some text with tokens
String text = "{COLOR=RED}Hello,{WAIT}{SLOWER}{COLOR=ORANGE} world!"
    + "{RESET} My name is {VAR=name} and I like {VAR=i_like}.";

// Create a TypingLabel instance with your custom text
TypingLabel label = new TypingLabel(text, skin);

// Optionally assign variables to replace {VAR} tokens
label.setVariable("name", "Bob");
label.setVariable("i_like", "squirrels");

// Add the actor to any widget like you normally would
stage.add(label);
```

It's also possible to fire events, according to their position in the text:
```java
// Create text with events
String text = "{SPEED=0.25}This is an {EVENT=example}example of "
    + "how to {EVENT=fire}fire {EVENT=events}events!";

TypingLabel label = new TypingLabel(text, skin);

// Create a TypingListener to listen to events as they're fired
label.setTypingListener(new TypingListener() {
    public void event (String event)  {
        System.out.println("Received text event: " + event);
    }

    public void end () {
        System.out.println("This is called when the text reaches the end.");
    }
});
```

Also you have full control of how the text behaves:
```java
// Pause and resume the typing progression
label.pause();
label.resume();

// Skip to the end already!
label.skipToTheEnd();

// Restart the typing progression, either with the same text or a new one
label.restart();
label.restart("Some new text.");
```

## Tokens
Currently the following tokens are accepted:

| Token | Description |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `{WAIT}` | Pauses the text for 250 milliseconds. |
| `{WAIT=seconds}` | Pauses the text for the specified amount of seconds. |
| `{SPEED=factor}` | Changes the speed of the text to the given factor, relative to the default speed. e.g. `2` makes the text go twice as fast, while `0.1` makes it super slow. |
| `{SLOWER}` | Equivalent to `{SPEED=0.5}`. |
| `{SLOW}` | Equivalent to `{SPEED=0.66}`. |
| `{NORMAL}` | Restores the text's default speed. Equivalent to `{SPEED=1}` or `{SPEED}`. |
| `{FAST}` | Equivalent to `{SPEED=2}`. |
| `{FASTER}` | Equivalent to `{SPEED=4}`. |
| `{COLOR=name}` | Sets the color of the text to the given name. [See accepted values](https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/Colors.java). |
| `{COLOR=#hex}` | Sets the color of the text to the given hex code. Accepted formats are #RRGGBBAA, where AA is optional and defaults to 0xFF. |
| `{CLEARCOLOR}` | Sets the color of the text to the Clear Color value, which [can be configured](#configuration) either globally or per label instance. Defaults to white. |
| `{RESET}` | Resets both speed and color. Equivalent to `{SPEED=1}{CLEARCOLOR}`. |
| `{VAR=name}` | Replaces the token with the value assigned to the variable name via `label.setVariable(key, value)`. |
| `{EVENT=name}` | Fires an event with the same name that can be caught with a `TypingListener`. |


## Configuration
The static class `TypingConfig` contains some global variables that can be modified at your will to fine tune the library's behavior, such as:
- Default text speed.
- Default value for the `{WAIT}` token.
- Default color to be used on the `{CLEARCOLOR}` token.
- Whether or not to enable color markup.
- How many characters can be "typed" per frame.
- Speed multipliers for specific characters, such as fast whitespaces but slow question marks.

## Setup
Download the source [as a zip](https://github.com/RafaSKB/typing-label/archive/master.zip) and copy the contents of `typing-label/src` into your project.

Maven and Gradle support is planned.

-------------------------------------

#### Known Issues
Due to the fact color tokens are just Color Markup tags, all issues with it also happen here. Putting a character right after a color token is known to cause problems with layouts, which are more noticeable with the typing effect this library provides. A simple workaround is to always put a whitespace right after the color token. e.g. Use `Foo{Color=RED} Bar` instead of `Foo {Color=RED}Bar`. You can read more about it [here](https://github.com/libgdx/libgdx/issues/4192).

Color Markup's `[]` tag doesn't work properly. Use `{CLEARCOLOR}` instead.
