/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.widgets.skin.GameStartButtonSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.net.URL;

/**
 * Rounded, semi-transparent button.
 */
public class GameStartButton extends Control {

    public static final String STYLESHEET = "game-start-button.css";

    private static final Runnable DEFAULT_ACTION = () -> Logger.info("No action assigned");

    private final ObjectProperty<Runnable> onAction = new SimpleObjectProperty<>(DEFAULT_ACTION);
    private final ObjectProperty<String> text = new SimpleObjectProperty<>("");
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font(20));

    public GameStartButton() {
        this("");
    }

    public GameStartButton(String text) {
        this.text.set(text);
        setFocusTraversable(true);
        getStyleClass().add("game-start-button");

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                e.consume();
                onAction.get().run();
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        final URL url = GameStartButton.class.getResource(STYLESHEET);
        return url != null ? url.toExternalForm() : null;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new GameStartButtonSkin(this);
    }

    public ObjectProperty<Runnable> onActionProperty() { return onAction; }
    public void setOnAction(Runnable r) { onAction.set(r); }

    public ObjectProperty<String> textProperty() { return text; }
    public String getText() { return text.get(); }
    public void setText(String s) { text.set(s); }

    public ObjectProperty<Font> fontProperty() { return font; }
    public Font getFont() { return font.get(); }
    public void setFont(Font f) { font.set(f); }
}
