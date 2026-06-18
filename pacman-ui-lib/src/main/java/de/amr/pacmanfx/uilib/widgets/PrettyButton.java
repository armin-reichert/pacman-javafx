/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.UfxBackgrounds;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.tinylog.Logger;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.roundedBackground;
import static java.util.Objects.requireNonNull;

/**
 * Rounded, transparent button-like pane. Can certainly also be realized with a standard button but had been
 * created for the WebFX version at the time because WebFX didn't support rounded buttons.
 */
public class PrettyButton extends StackPane {

    private Runnable action = () -> {
        Logger.info("No action assigned to button");
    };

    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font(20));

    public PrettyButton(String buttonText, Font initialFont, Color bgColor, Color fillColor) {
        final var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));

        final var text = new Text();
        text.setFill(fillColor);
        text.fontProperty().bind(font);
        text.setText(buttonText);
        text.setEffect(shadow);

        getChildren().add(text);

        maxHeightProperty().bind(font.map(f -> Math.min(2.5 * f.getSize(), 60)));

        maxWidthProperty().bind(Bindings.createDoubleBinding(
            () -> 1.25 * text.getText().length() * fontProperty().get().getSize(),
            text.textProperty(), fontProperty()
        ));

        setBackground(roundedBackground(bgColor, 20));
        setCursor(Cursor.HAND);

        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && action != null) {
                action.run();
                e.consume();
            }
        });

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                action.run();
                e.consume();
            }
        });

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setBackground(roundedBackground(Color.GREEN, 20));
            } else {
                setBackground(roundedBackground(bgColor, 20));
            }
        });

        font.set(initialFont);
    }

    public ObjectProperty<Font> fontProperty() {
        return font;
    }

    public void setOnAction(Runnable actionCode) {
        this.action = requireNonNull(actionCode);
    }
}
