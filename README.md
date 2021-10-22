![Typing Label Logo](logo.png)

# TypingLabel

[![Maven Central](https://img.shields.io/maven-central/v/com.rafaskoberg.gdx/typing-label.svg?colorB=43BD15)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22typing-label%22)
[![license](https://img.shields.io/github/license/rafaskb/typing-label.svg)](https://github.com/rafaskb/typing-label/blob/master/LICENSE)

A libGDX Label that appears as if it was being typed in real time.

It works as a drop-in replacement for normal [Scene2D Labels](https://github.com/libgdx/libgdx/wiki/Scene2d.ui#label), and you can use optional [tokens](https://github.com/rafaskb/typing-label/wiki/Tokens) to customize the text's behavior.

![Sample GIF](media/sample.gif)


## Installation

Open _build.gradle_ in project root and add this to the _ext_ section under _allprojects_:

```groovy
typingLabelVersion = '1.2.0'
regExodusVersion = '0.1.12' // Only if you're using HTML / GWT
```

#### Core module

Add this to your _build.gradle_ core dependencies:
```groovy
api "com.rafaskoberg.gdx:typing-label:$typingLabelVersion"
```

> _Note: Replace `api` with `compile` if you're using a Gradle version older than 3.4._

#### HTML dependencies
###### (Only if you're using HTML / GWT)

Add this to your _GdxDefinition.gwt.xml_ file:
```xml
<inherits name="com.rafaskoberg.gdx.typinglabel.typinglabel" />
```

Add this to your _build.gradle_ html dependencies:
```groovy
api "com.github.tommyettinger:regexodus:$regExodusVersion:sources"
api "com.rafaskoberg.gdx:typing-label:$typingLabelVersion:sources"
```

> _Note: Replace `api` with `compile` if you're using a Gradle version older than 3.4._


## Getting Started

Check the Wiki:
- [Usage examples](https://github.com/rafaskb/typing-label/wiki/Examples)
- [Tokens](https://github.com/rafaskb/typing-label/wiki/Tokens)
- [Fine tuning](https://github.com/rafaskb/typing-label/wiki/Fine-Tuning)
- [Custom Effects](https://github.com/rafaskb/typing-label/wiki/Tokens#custom-effects)

## textratypist and SDF / MSDF Support
_Multi-channel Signed Distance Field_ fonts allow you to prepare and load just one font file and render it in any scale you want, while mantaining the quality and cripsness of the original texture, as if you were working directly with vectors.

Since TypingLabel aims to be a replacement for regular scene2d.ui Labels though, that means it relies on BitmapFonts, which have a specific size and don't work well with scaling.

If you're using SDF fonts in your project and want TypingLabel features, then make sure to take a look at [Textramode](https://github.com/tommyettinger/textramode) and [Textratypist](https://github.com/tommyettinger/textramode) libraries by [Tommy Ettinger](https://github.com/tommyettinger). They support SDF and MSDF fonts, have much of the TypingLabel features, as well extended markup such as bold and oblique, and much more.
