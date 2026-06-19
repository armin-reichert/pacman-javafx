/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.widgets.skin.FontAwesomeIconSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import static java.util.Objects.requireNonNull;

/**
 * Displays a FontAwesome icon.
 */
public class FontAwesomeIcon extends Control {

    public static final String DEFAULT_STYLE_CLASS = "font-awesome-icon";

    public static final int DEFAULT_SIZE = 16;

    private final FontAwesomeSymbol symbol;

    private final ObjectProperty<Paint> fill = new SimpleObjectProperty<>(Color.WHITE);

    public FontAwesomeIcon(FontAwesomeSymbol symbol) {
        this(symbol, DEFAULT_SIZE);
    }

    public FontAwesomeIcon(FontAwesomeSymbol symbol, double prefSize) {
        this.symbol = requireNonNull(symbol);
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        setPrefSize(prefSize, prefSize);
        setMouseTransparent(false);
    }

    public ObjectProperty<Paint> fillProperty() {
        return fill;
    }

    public void setFill(Paint paint) {
        fillProperty().set(paint);
    }

    public Paint fill() {
        return fill.get();
    }

    public FontAwesomeSymbol symbol() {
        return symbol;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FontAwesomeIconSkin(this);
    }
}
