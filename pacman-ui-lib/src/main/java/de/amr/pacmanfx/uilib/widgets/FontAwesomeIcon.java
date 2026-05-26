/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.tinylog.Logger;

import java.net.URL;

import static java.util.Objects.requireNonNull;

/**
 * A text control displaying a FontAwesome icon.
 */
public class FontAwesomeIcon extends Text {

    //TODO generate full enum from FontAwesome catalog
    public enum Symbol {
        CHEVRON_CIRCLE_LEFT('\uf137'),
        CHEVRON_CIRCLE_RIGHT('\uf138'),
        CUBES('\uf1b3'),
        DEAF('\uf2a4'),
        FLAG('\uf024'),
        PAUSE('\uf04c'),
        TAXI('\uf1ba'),
        USER_SECRET('\uf21b');

        Symbol(char unicode) {
            this.unicode = unicode;
        }

        private final char unicode;
    }

    private static final Font FONT;

    static {
        final URL url = FontAwesomeIcon.class.getResource("/de/amr/pacmanfx/uilib/fonts/fa7/Font Awesome 7 Free-Solid-900.otf");
        if (url != null) {
            FONT = Font.loadFont(url.toExternalForm(), 20);
        } else {
            Logger.error("Could not load Font Awesome fonts");
            FONT = Font.font(20);
        }
    }

    private final DoubleProperty fontSize = new SimpleDoubleProperty(16);

    public DoubleProperty fontSizeProperty() {
        return fontSize;
    }

    public static FontAwesomeIcon of(Symbol symbol, double fontSize, Color color) {
        requireNonNull(symbol);
        requireNonNull(color);
        final FontAwesomeIcon icon = new FontAwesomeIcon();
        icon.setFill(color);
        icon.fontSizeProperty().set(fontSize);
        icon.fontProperty().bind(icon.fontSizeProperty().map(s -> Font.font(FONT.getFamily(), s.doubleValue())));
        icon.setText(String.valueOf(symbol.unicode));
        return icon;
    }
}
