# TypingLabel

<!-- Shields -->

A libGDX Label that appears as if it was being typed in real time.

It works as a drop-in replacement for normal [Scene2D Labels](https://github.com/libgdx/libgdx/wiki/Scene2d.ui#label), and you can use optional [tokens](#tokens) to customize the text's behavior.

<!-- GIF -->

## Setup
Download the source [as a zip](https://github.com/RafaSKB/typing-label/archive/master.zip) and copy the contents of `typing-label/src` into your project.

Maven and Gradle support is planned.


## Getting Started

- [Usage examples](https://github.com/rafaskb/typing-label/wiki/Examples)
- [Tokens](https://github.com/rafaskb/typing-label/wiki/Tokens)
- [Fine tuning](https://github.com/rafaskb/typing-label/wiki/Fine-Tuning)


----


#### Known Issues
Due to the fact color tokens are just Color Markup tags, all issues with them also happen here. Putting a character right after a color token is known to cause problems with layouts, which are more noticeable with the typing effect this library provides. A simple workaround is to always put a whitespace right after the color token. e.g. Use `Foo{Color=RED} Bar` instead of `Foo {Color=RED}Bar`. You can read more about it [here](https://github.com/libgdx/libgdx/issues/4192).

Color Markup's `[]` tag doesn't work properly. Use {CLEARCOLOR} instead.
