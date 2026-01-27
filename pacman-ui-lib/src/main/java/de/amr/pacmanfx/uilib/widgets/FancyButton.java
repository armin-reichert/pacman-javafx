/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.Ufx;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Rounded, transparent button-like pane. Can probably be realized with a standard button but this has been
 * created for the WebFX version at the time.
 */
public class FancyButton extends BorderPane {

    private Runnable action;

    public FancyButton(String buttonText, Font font, Color bgColor, Color fillColor) {
        var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));

        var text = new Text();
        text.setFill(fillColor);
        text.setFont(font);
        text.setText(buttonText);
        text.setEffect(shadow);

        setCenter(text);
        setCursor(Cursor.HAND);
        setMaxSize(200, 60);
        setPadding(new Insets(5, 5, 5, 5));
        setBackground(Ufx.roundedBackground(bgColor, 20));
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (action != null) action.run();
            e.consume();
        });
    }

    public void setAction(Runnable action) {
        this.action = action;
    }
}
