# TypingLabel

[![Jitpack](https://jitpack.io/v/rafaskb/typing-label.svg)](https://jitpack.io/#rafaskb/typing-label)
[![license](https://img.shields.io/github/license/rafaskb/typing-label.svg)](https://github.com/rafaskb/typing-label/blob/master/LICENSE)

A libGDX Label that appears as if it was being typed in real time.

It works as a drop-in replacement for normal [Scene2D Labels](https://github.com/libgdx/libgdx/wiki/Scene2d.ui#label), and you can use optional [tokens](https://github.com/rafaskb/typing-label/wiki/Tokens) to customize the text's behavior.

![Sample GIF](media/sample.gif)

## Installation

#### Using Gradle

Step 1. Add the JitPack repository to your root build.gradle file at the end of repositories:
```groovy
	allprojects {
	    repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2. Add the dependency to your build.gradle file, in the dependencies block of the core project:

```groovy
	dependencies {
		compile 'com.github.rafaskb:typing-label:1.0.5'
	}
```

#### Using Maven

Step 1. Add the JitPack repository to your build file
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

Step 2. Add the dependency
```xml
	<dependency>
	    <groupId>com.rafaskoberg.gdx</groupId>
	    <artifactId>typing-label</artifactId>
	    <version>1.0.5</version>
	</dependency>
```

#### Manually

Alternatively you can download the source [as a zip](https://github.com/RafaSKB/typing-label/archive/master.zip) and copy the contents of `typing-label/src` into your project.


## Getting Started

Check the Wiki:
- [Usage examples](https://github.com/rafaskb/typing-label/wiki/Examples)
- [Tokens](https://github.com/rafaskb/typing-label/wiki/Tokens)
- [Fine tuning](https://github.com/rafaskb/typing-label/wiki/Fine-Tuning)

----

#### Known Issues
Due to the fact color tokens are just Color Markup tags, all issues with them also happen here. Putting a character right after a color token is known to cause problems with layouts, which are more noticeable with the typing effect this library provides. A simple workaround is to always put a whitespace right after the color token. e.g. Use `Foo{Color=RED} Bar` instead of `Foo {Color=RED}Bar`. You can read more about it [here](https://github.com/libgdx/libgdx/issues/4192).

Color Markup's `[]` tag doesn't work properly. Use {CLEARCOLOR} instead.
