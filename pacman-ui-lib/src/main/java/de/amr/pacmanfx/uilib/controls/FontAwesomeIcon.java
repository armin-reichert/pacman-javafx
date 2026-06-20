/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.controls;

import de.amr.pacmanfx.uilib.controls.skin.FontAwesomeIconSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.*;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Displays a <a href="https://fontawesome.com/">Font Awesome icon</a>.
 */
public class FontAwesomeIcon extends Control {

    public static final String STYLESHEET = "font-awesome-icon.css";

    public static final String DEFAULT_STYLE_CLASS = "font-awesome-icon";

    public static final int DEFAULT_ICON_SIZE = 32;

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


    // To make the "size" property stylable via CSS, add metadata:
    private static final CssMetaData<FontAwesomeIcon, Number> SIZE_META =
        new CssMetaData<>("-fx-size", SizeConverter.getInstance(), DEFAULT_ICON_SIZE) {

            @Override
            public boolean isSettable(FontAwesomeIcon node) {
                return !node.sizeProperty().isBound();
            }

            @Override
            public StyleableDoubleProperty getStyleableProperty(FontAwesomeIcon node) {
                return node.sizeProperty();
            }
        };

    private static final List<CssMetaData<? extends Styleable, ?>> MERGED_META_DATA;

    static {
        final List<CssMetaData<? extends Styleable, ?>> metaData = new ArrayList<>(Control.getClassCssMetaData());
        metaData.add(FILL_META);
        metaData.add(SIZE_META);
        MERGED_META_DATA = Collections.unmodifiableList(metaData);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return MERGED_META_DATA;
    }

    // --- end of static area

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

    private final StyleableDoubleProperty size = new StyleableDoubleProperty(DEFAULT_ICON_SIZE) {

        @Override
        public Object getBean() {
            return FontAwesomeIcon.this;
        }

        @Override
        public String getName() {
            return "size";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return SIZE_META;
        }
    };

    public FontAwesomeIcon(FontAwesomeSymbol symbol) {
        this.symbol = requireNonNull(symbol);
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        maxWidthProperty().bind(size);
        maxHeightProperty().bind(size);
    }

    @Override
    public String toString() {
        return "FontAwesomeIcon{" + "symbol=" + symbol +
            ", size=" + size.get() +
            '}';
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

    public StyleableDoubleProperty sizeProperty() {
        return size;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FontAwesomeIconSkin(this);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    @Override
    public String getUserAgentStylesheet() {
        final URL url = getClass().getResource(STYLESHEET);
        return url != null ? url.toExternalForm() : null;
    }

    @Override
    protected double computePrefWidth(double height) {
        return sizeProperty().get();
    }

    @Override
    protected double computePrefHeight(double width) {
        return sizeProperty().get();
    }
}
