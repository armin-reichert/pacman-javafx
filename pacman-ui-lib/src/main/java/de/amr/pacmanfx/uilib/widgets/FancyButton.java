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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Rounded, transparent button-like pane. Can probably be realized with a standard button but this has been
 * created for the WebFX version at the time.
 */
public class FancyButton extends StackPane {

    private Runnable action;

    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font(20));

    public FancyButton(String buttonText, Font initialFont, Color bgColor, Color fillColor) {
        var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));

        var text = new Text();
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

        setBackground(UfxBackgrounds.roundedBackground(bgColor, 20));
        setCursor(Cursor.HAND);

        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && action != null) {
                action.run();
                e.consume();
            }
        });

        font.set(initialFont);
    }

    public ObjectProperty<Font> fontProperty() {
        return font;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }
}
