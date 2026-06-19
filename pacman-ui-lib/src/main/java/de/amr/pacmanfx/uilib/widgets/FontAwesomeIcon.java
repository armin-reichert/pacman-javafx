/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.widgets.skin.FontAwesomeIconSkin;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.PaintConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Displays a FontAwesome icon.
 */
public class FontAwesomeIcon extends Control {

    public static final String DEFAULT_STYLE_CLASS = "font-awesome-icon";

    public static final int DEFAULT_SIZE = 16;

    // To make the "fill" property stylable via CSS, add metadata:
    private static final CssMetaData<FontAwesomeIcon, Paint> FILL_META =
        new CssMetaData<>("-fx-fill", PaintConverter.getInstance(), Color.WHITE) {
            @Override
            public boolean isSettable(FontAwesomeIcon node) {
                return !node.fill.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(FontAwesomeIcon node) {
                return node.fillProperty();
            }
        };

    private static final List<CssMetaData<? extends Styleable, ?>> MERGED_META_DATA;

    static {
        final List<CssMetaData<? extends Styleable, ?>> metaData = new ArrayList<>(Control.getClassCssMetaData());
        metaData.add(FILL_META);
        MERGED_META_DATA = Collections.unmodifiableList(metaData);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return MERGED_META_DATA;
    }

    private final FontAwesomeSymbol symbol;

    private final StyleableObjectProperty<Paint> fill = new StyleableObjectProperty<>(Color.WHITE) {

            @Override
            public Object getBean() {
                return FontAwesomeIcon.this;
            }

            @Override
            public String getName() {
                return "fill";
            }

            @Override
            public CssMetaData<FontAwesomeIcon, Paint> getCssMetaData() {
                return FILL_META;
            }
        };

    public FontAwesomeIcon(FontAwesomeSymbol symbol) {
        this(symbol, DEFAULT_SIZE);
    }

    public FontAwesomeIcon(FontAwesomeSymbol symbol, double prefSize) {
        this.symbol = requireNonNull(symbol);
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        setPrefSize(prefSize, prefSize);
    }

    public StyleableObjectProperty<Paint> fillProperty() {
        return fill;
    }

    public void setFill(Paint paint) {
        fillProperty().set(paint);
    }

    public Paint getFill() {
        return fill.get();
    }

    public FontAwesomeSymbol symbol() {
        return symbol;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FontAwesomeIconSkin(this);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
}
