/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.controls;

import de.amr.pacmanfx.uilib.controls.skin.GameStartButtonSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.text.Font;

import java.net.URL;

/**
 * Rounded, semi-transparent button.
 */
public class GameStartButton extends Control {

    private static final String STYLESHEET = "game-start-button.css";

    private static final String DEFAULT_STYLE_CLASS = "game-start-button";

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");

    private final ObjectProperty<String> text = new SimpleObjectProperty<>("");

    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font(20));

    public GameStartButton() {
        this("");
    }

    public GameStartButton(String buttonText) {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        text.set(buttonText);

        setFocusTraversable(true);

    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new GameStartButtonSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        final URL url = GameStartButton.class.getResource(STYLESHEET);
        return url != null ? url.toExternalForm() : null;
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final void setOnAction(EventHandler<ActionEvent> handler) {
        onAction.set(handler);
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    public void fire() {
        ActionEvent event = new ActionEvent();
        Event.fireEvent(this, event);

        EventHandler<ActionEvent> handler = getOnAction();
        if (handler != null) {
            handler.handle(event);
        }
    }

    public ObjectProperty<String> textProperty() { return text; }

    public String getText() { return text.get(); }

    public void setText(String s) { text.set(s); }

    public ObjectProperty<Font> fontProperty() { return font; }

    public Font getFont() { return font.get(); }

    public void setFont(Font f) { font.set(f); }
}
