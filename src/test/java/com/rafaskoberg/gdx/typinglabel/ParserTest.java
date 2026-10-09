package com.rafaskoberg.gdx.typinglabel;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.rafaskoberg.gdx.typinglabel.effects.WindEffect;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** Tests token parsing. Runs without a GL context by using an empty font, since only the label's text matters here. */
public class ParserTest {
    private static final float DELTA = 0.0001f;

    private TokenDelimiter originalDelimiter;
    private LabelStyle     style;

    @Before
    public void setUp() {
        originalDelimiter = TypingConfig.TOKEN_DELIMITER;
        TypingConfig.TOKEN_DELIMITER = TokenDelimiter.CURLY_BRACKETS;

        BitmapFont font = new BitmapFont(new BitmapFontData(), Array.with(new TextureRegion()), false);
        style = new LabelStyle(font, null);
    }

    @After
    public void tearDown() {
        TypingConfig.TOKEN_DELIMITER = originalDelimiter;
    }

    /** Creates a label with the given text and variables (as key/value pairs), parses its tokens and returns it. */
    private TypingLabel parse(String text, String... variables) {
        TypingLabel label = new TypingLabel(text, style);
        for(int i = 0; i < variables.length; i += 2) {
            label.setVariable(variables[i], variables[i + 1]);
        }
        label.parseTokens();
        return label;
    }

    private String parseText(String text, String... variables) {
        return parse(text, variables).getText().toString();
    }

    private Array<TokenEntry> entriesOf(TypingLabel label, TokenCategory category) {
        Array<TokenEntry> entries = new Array<>();
        for(TokenEntry entry : label.tokenEntries) {
            if(entry.category == category) entries.add(entry);
        }
        return entries;
    }

    @Test
    public void ifTokenWithAccentedOptions() {
        String text = "{IF=PRONOUNS;1=Funcionária do mês;2=Funcionário do mês;Funcionárie do mês}";
        assertEquals("Funcionária do mês", parseText(text, "PRONOUNS", "1"));
        assertEquals("Funcionário do mês", parseText(text, "PRONOUNS", "2"));
        assertEquals("Funcionárie do mês", parseText(text, "PRONOUNS", "3"));
    }

    @Test
    public void ifTokenWithoutAccentsStillWorks() {
        String text = "{IF=PRONOUNS;1=Colaboradora;2=Colaborador;Colaboradore}";
        assertEquals("Colaboradora", parseText(text, "PRONOUNS", "1"));
        assertEquals("Colaborador", parseText(text, "PRONOUNS", "2"));
        assertEquals("Colaboradore", parseText(text, "PRONOUNS", "3"));
    }

    @Test
    public void ifTokenWithPunctuationInOptions() {
        String text = "{IF=MOOD;happy=Yay! (really) and/or \"quoted\";sad=Oh no... 50% off, right?;Meh!}";
        assertEquals("Yay! (really) and/or \"quoted\"", parseText(text, "MOOD", "happy"));
        assertEquals("Oh no... 50% off, right?", parseText(text, "MOOD", "sad"));
        assertEquals("Meh!", parseText(text, "MOOD", "other"));
    }

    @Test
    public void nonLatinScriptsInParameters() {
        // Japanese
        String japanese = "{IF=PRONOUNS;1=今月の従業員;2=こんにちは、世界！;デフォルト}";
        assertEquals("今月の従業員", parseText(japanese, "PRONOUNS", "1"));
        assertEquals("こんにちは、世界！", parseText(japanese, "PRONOUNS", "2"));
        assertEquals("デフォルト", parseText(japanese, "PRONOUNS", "3"));

        // Cyrillic, both as IF options and as a variable name
        assertEquals("Сотрудница месяца", parseText("{IF=PRONOUNS;1=Сотрудница месяца;Сотрудник месяца}", "PRONOUNS", "1"));
        assertEquals("Привет, Rafa!", parseText("Привет, {VAR=имя}!", "имя", "Rafa"));

        // Event tokens keep their parameter untouched
        Array<TokenEntry> events = entriesOf(parse("Hello{EVENT=событие №1} world"), TokenCategory.EVENT);
        assertEquals(1, events.size);
        assertEquals("событие №1", events.first().stringValue);
    }

    @Test
    public void colorTokenStillWorks() {
        TypingLabel label = parse("{COLOR=RED}Hello{CLEARCOLOR} world");
        assertEquals("[RED]Hello[#ffffffff] world", label.getText().toString());
        assertEquals(2, entriesOf(label, TokenCategory.SKIP).size);

        assertEquals("[#FF0000]Hello", parseText("{COLOR=#FF0000}Hello"));
        assertEquals("[#FF0000]Hello", parseText("{COLOR=FF0000}Hello"));
    }

    @Test
    public void varTokenStillWorks() {
        assertEquals("Hello, Rafa!", parseText("Hello, {VAR=PLAYER}!", "PLAYER", "Rafa"));
        assertEquals("Hello, Rafa!", parseText("Hello, {var=player}!", "PLAYER", "Rafa"));
        assertEquals("Hello, MISSING!", parseText("Hello, {VAR=missing}!"));
    }

    @Test
    public void speedAndWaitTokensStillWork() {
        TypingLabel label = parse("Hello {SPEED=0.5}slow {WAIT=1.5}world{FAST}!");
        assertEquals("Hello slow world!", label.getText().toString());

        Array<TokenEntry> speeds = entriesOf(label, TokenCategory.SPEED);
        assertEquals(2, speeds.size);
        speeds.sort();
        assertEquals("SPEED", speeds.get(0).token);
        assertEquals(TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.5f, speeds.get(0).floatValue, DELTA);
        assertEquals("FAST", speeds.get(1).token);
        assertEquals(TypingConfig.DEFAULT_SPEED_PER_CHAR / 2f, speeds.get(1).floatValue, DELTA);

        Array<TokenEntry> waits = entriesOf(label, TokenCategory.WAIT);
        assertEquals(1, waits.size);
        assertEquals(1.5f, waits.first().floatValue, DELTA);
    }

    @Test
    public void effectTokenWithSeveralParametersStillWorks() {
        TypingLabel label = parse("{WIND=2;4;0.5;0.5}Windy{ENDWIND} {GRADIENT=ORANGE;DB6600;-0.5;5}text{ENDGRADIENT}");
        assertEquals("Windy text", label.getText().toString());

        Array<TokenEntry> starts = entriesOf(label, TokenCategory.EFFECT_START);
        assertEquals(2, starts.size);
        starts.sort();
        assertEquals("WIND", starts.get(0).token);
        assertTrue(starts.get(0).effect instanceof WindEffect);
        assertEquals(0, starts.get(0).index);
        assertEquals("GRADIENT", starts.get(1).token);
        assertNotNull(starts.get(1).effect);
        assertEquals(2, entriesOf(label, TokenCategory.EFFECT_END).size);
    }

    @Test
    public void unknownTokensAreLeftUntouched() {
        assertEquals("{NOPE=ação} and {plain text}", parseText("{NOPE=ação} and {plain text}"));
    }

    @Test
    public void parameterDoesNotRunPastClosingDelimiter() {
        assertEquals("1 text 2", parseText("{VAR=A} text {VAR=B}", "A", "1", "B", "2"));
        assertEquals("á! text é?", parseText("{IF=A;1=á!;x} text {IF=B;2=é?;y}", "A", "1", "B", "2"));

        TypingLabel label = parse("{EVENT=primeiro, né?} text {EVENT=second}");
        assertEquals(" text ", label.getText().toString());
        Array<TokenEntry> events = entriesOf(label, TokenCategory.EVENT);
        assertEquals(2, events.size);
        events.sort();
        assertEquals("primeiro, né?", events.get(0).stringValue);
        assertEquals("second", events.get(1).stringValue);
    }

    @Test
    public void unclosedTokenIsNotParsed() {
        assertEquals("{VAR=A text 2", parseText("{VAR=A text {VAR=B}", "A", "1", "B", "2"));
    }

    @Test
    public void bracketsDelimiter() {
        TypingConfig.TOKEN_DELIMITER = TokenDelimiter.BRACKETS;
        assertEquals("1 text 2", parseText("[VAR=A] text [VAR=B]", "A", "1", "B", "2"));
        assertEquals("Funcionária do mês! {x}", parseText("[IF=PRONOUNS;1=Funcionária do mês!;Funcionárie] {x}", "PRONOUNS", "1"));

        TypingLabel label = parse("[WIND=2;4;0.5;0.5]Windy[ENDWIND] {VAR=A}", "A", "1");
        assertEquals("Windy {VAR=A}", label.getText().toString());
        assertEquals(1, entriesOf(label, TokenCategory.EFFECT_START).size);
    }

    @Test
    public void parenthesesDelimiter() {
        TypingConfig.TOKEN_DELIMITER = TokenDelimiter.PARENTHESES;
        assertEquals("1 text 2", parseText("(VAR=A) text (VAR=B)", "A", "1", "B", "2"));
        assertEquals("Olá, {você}!", parseText("(IF=PRONOUNS;1=Olá, {você}!;Oi)", "PRONOUNS", "1"));

        TypingLabel label = parse("Hello (SPEED=0.5)world (EVENT=é isso)");
        assertEquals("Hello world ", label.getText().toString());
        assertEquals(1, entriesOf(label, TokenCategory.SPEED).size);
        assertEquals("é isso", entriesOf(label, TokenCategory.EVENT).first().stringValue);
    }

    @Test
    public void delimiterCanBeSwitchedBack() {
        TypingConfig.TOKEN_DELIMITER = TokenDelimiter.BRACKETS;
        assertEquals("1", parseText("[VAR=A]", "A", "1"));

        TypingConfig.TOKEN_DELIMITER = TokenDelimiter.CURLY_BRACKETS;
        assertEquals("1 [VAR=A]", parseText("{VAR=A} [VAR=A]", "A", "1"));
    }
}
