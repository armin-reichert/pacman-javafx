/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets.startbutton;

import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.roundedBackground;
import static java.util.Objects.requireNonNull;

/**
 * Rounded, transparent button-like pane. Can certainly also be realized with a standard button but had been
 * created for the WebFX version at the time because WebFX didn't support rounded buttons.
 */
public class GameStartButton extends Control {

    private static final Runnable DEFAULT_ACTION = () -> Logger.info("No action assigned");

    private final ObjectProperty<Runnable> onAction = new SimpleObjectProperty<>(DEFAULT_ACTION);
    private final ObjectProperty<String> text = new SimpleObjectProperty<>("");
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font(20));

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
        return GameStartButton.class
            .getResource("/de/amr/pacmanfx/uilib/widgets/startbutton/game-start-button.css")
            .toExternalForm();
    }


    @Override
    protected Skin<?> createDefaultSkin() {
        return new GameStartButtonSkin(this);
    }

    public ObjectProperty<Runnable> onActionProperty() { return onAction; }
    public void setOnAction(Runnable r) { onAction.set(r); }

    public ObjectProperty<String> textProperty() { return text; }
    public String getText() { return text.get(); }

    public ObjectProperty<Font> fontProperty() { return font; }
    public Font getFont() { return font.get(); }
}
