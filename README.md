![Typing Label Logo](logo.png)

# TypingLabel

[![Maven Central](https://img.shields.io/maven-central/v/com.rafaskoberg.gdx/typing-label.svg?colorB=43BD15)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22typing-label%22)
[![license](https://img.shields.io/github/license/rafaskb/typing-label.svg)](https://github.com/rafaskb/typing-label/blob/master/LICENSE)

A libGDX Label that appears as if it was being typed in real time.

It works as a drop-in replacement for normal [Scene2D Labels](https://libgdx.com/wiki/graphics/2d/scene2d/scene2d-ui#label), and you can use optional [tokens](https://github.com/rafaskb/typing-label/wiki/Tokens) to customize the text's behavior.

![Sample GIF](media/sample.gif)


## Installation

Open _build.gradle_ in project root and add this to the _ext_ section under _allprojects_:

```groovy
typingLabelVersion = '1.4.0'
regExodusVersion = '0.1.20' // Only if you're using HTML / GWT
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
- [Fine-tuning](https://github.com/rafaskb/typing-label/wiki/Fine-Tuning)
- [Custom Effects](https://github.com/rafaskb/typing-label/wiki/Tokens#custom-effects)

## textratypist and SDF / MSDF Support
_Signed Distance Field_ and _Multi-channel Signed Distance Field_ fonts allow you to prepare and load just one font file
and render it in any scale you want, while maintaining the quality and crispness of the original texture, as if you were
working directly with vectors.

Since TypingLabel aims to be a replacement for regular scene2d.ui Labels though, that means it relies on BitmapFonts,
which have a specific size and don't work well with scaling. There is a DistanceFieldFont class in libGDX that extends
BitmapFont, but it flushes its Batch a lot more than a BitmapFont does, and isn't exactly easy to use.

If you're using SDF or MSDF fonts in your project and want TypingLabel features, then make sure to take a look at the
[Textratypist](https://github.com/tommyettinger/textratypist) library by
[Tommy Ettinger](https://github.com/tommyettinger). It supports SDF and MSDF fonts, has most of the TypingLabel
features, offers extended markup such as bold and oblique, and much more.

(Tommy Ettinger has hijacked the README.md at this point.)

Other features in TextraTypist include inline images such as emoji, click-able links that go to a URL, click-able words
that trigger an event via TypingListener, rotating glyphs in-place (with various effects that use this),
stretching/squashing individual glyphs (also used by various effects), named colors that can mix and alter their parts
(like `darker dull blue green`), no usage of reflection except what Skin uses (useful for Graal Native Images), various
ways to adjust fonts to force monospace, change line-height, increase or decrease glyph width, experimental support for
justifying text... It goes on for a while.

However, TextraTypist isn't as drop-in compatible as Typing-Label with scene2d.ui support! The `TypingLabel` in this
library extends `Label` from scene2d.ui, while the one in TextraTypist does not, and in fact there's a whole duplicate
set of scene2d.ui widgets that use TextraTypist's `Font` class instead of `BitmapFont`. These aren't always necessary to
use, but they are needed if you need most of the mentioned TextraTypist features, like how a TextraSelectBox can have
emoji icons in its text rows.

A few other features are present in Typing-Label but are absent from TextraTypist. TextraTypist uses both square braces
and curly brackets for different types of markup, so it doesn't have an easy way to swap out an effect like `{RAINBOW}`
and make it use a different syntax, like `<<RAINBOW>>`. Typing-Label can do this! TextraTypist can, at most, use the
alternate syntax `[-RAINBOW]` to be compatible with I18N properties files.

When gdx-liftoff added effects to warning messages to draw attention to them, it added Typing-Label as a dependency, and
it applied smoothly in-place. When gdx-liftoff updated to libGDX 1.14.0, Typing-Label wasn't compatible immediately, so
the dependency switched to TextraTypist -- because TextraTypist ~~stole~~ built upon mostly the same code as
Typing-Label, the needed changes were small. Typing-Label should be compatible with libGDX 1.14.0 as of now (at least
via JitPack, and probably via a Maven Central release), so this isn't going to be a problem in the future.
