/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.tinylog.Logger;

import java.net.URL;

/**
 * Displays a FontAwesome icon.
 */
public class FontAwesomeIcon {

    public static final String FONT_PATH = "/de/amr/pacmanfx/uilib/fonts/fa7/Font Awesome 7 Free-Solid-900.otf";
    public static final int DEFAULT_SIZE = 16;

    //TODO incomplete
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

    private static final Font FONT = loadFont();

    private static Font loadFont() {
        final int size = DEFAULT_SIZE;
        final URL url = FontAwesomeIcon.class.getResource(FONT_PATH);
        Font font;
        if (url != null) {
            font = Font.loadFont(url.toExternalForm(), size);
            Logger.info("FontAwesome font loaded successfully: {}", font);
        } else {
            font = Font.font(size);
            Logger.error("Could not load Font Awesome fonts, using default font");
        }
        return font;
    }

    private final DoubleProperty size = new SimpleDoubleProperty(DEFAULT_SIZE);

    private final Text text = new Text();

    public FontAwesomeIcon(Symbol symbol, int size) {
        text.fontProperty().bind(sizeProperty().map(s -> Font.font(FONT.getFamily(), s.doubleValue())));
        text.setText(String.valueOf(symbol.unicode));
        sizeProperty().set(size);
    }

    public FontAwesomeIcon(Symbol symbol) {
        this(symbol, DEFAULT_SIZE);
    }

    public ObjectProperty<Paint> fillProperty() {
        return text.fillProperty();
    }

    public DoubleProperty opacityProperty() {
        return text.opacityProperty();
    }

    public BooleanProperty visibleProperty() {
        return text.visibleProperty();
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public Node node() {
        return text;
    }
}
