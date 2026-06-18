/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
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
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.roundedBackground;
import static java.util.Objects.requireNonNull;

/**
 * Rounded, transparent button-like pane. Can certainly also be realized with a standard button but had been
 * created for the WebFX version at the time because WebFX didn't support rounded buttons.
 */
public class PrettyButton extends StackPane {

    private static final Runnable DEFAULT_ACTION = () -> Logger.info("No action assigned to button");

    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font(20));

    private final ObjectProperty<Runnable> action = new SimpleObjectProperty<>(DEFAULT_ACTION);

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

        maxWidthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + 20));
        maxHeightProperty().bind(font.map(f -> Math.min(2.5 * f.getSize(), 60)));

        setBackground(roundedBackground(bgColor, 20));
        setCursor(Cursor.HAND);

        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                e.consume();
                animateAndExecuteAction();
            }
        });

        setOnMouseEntered(_ -> setOpacity(0.85));
        setOnMouseExited(_ -> setOpacity(1.0));
        setOnMousePressed(_ -> setScaleX(0.97));
        setOnMouseReleased(_ -> setScaleX(1.0));

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                e.consume();
                animateAndExecuteAction();
            }
        });

        font.set(initialFont);
    }

    public ObjectProperty<Font> fontProperty() {
        return font;
    }

    public ObjectProperty<Runnable> actionProperty() {
        return action;
    }

    public void setOnAction(Runnable actionCode) {
        action.set(requireNonNull(actionCode));
    }

    private void animateAndExecuteAction() {
        playPressAnimation(action.get());
    }

    private void playPressAnimation(Runnable afterAnimation) {
        var scaleUp = new ScaleTransition(Duration.seconds(0.2), this);
        scaleUp.setToX(1.5);
        scaleUp.setToY(1.5);

        var scaleDown = new ScaleTransition(Duration.seconds(0.05), this);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        var seq = new SequentialTransition(scaleUp, scaleDown);
        seq.setOnFinished(_ -> afterAnimation.run());
        seq.play();
    }

}
