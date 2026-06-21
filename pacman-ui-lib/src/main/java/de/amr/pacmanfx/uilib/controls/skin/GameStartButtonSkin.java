/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.controls.skin;

import de.amr.pacmanfx.uilib.controls.GameStartButton;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class GameStartButtonSkin extends SkinBase<GameStartButton> {

    public GameStartButtonSkin(GameStartButton button) {
        super(button);

        final Text text = new Text();
        text.textProperty().bind(button.textProperty());
        text.setFill(Color.WHITE);

        button.getStyleClass().add("game-start-button-container");

        // Without this the button fill the complete area
        button.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        button.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

        button.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                e.consume();
                button.fire();
            }
        });

        button.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                e.consume();
                button.fire();
            }
        });

        button.setOnMouseEntered(_ -> button.setOpacity(0.85));
        button.setOnMouseExited (_ -> button.setOpacity(1.0));

        button.setOnMousePressed (_ -> button.setScaleX(0.97));
        button.setOnMouseReleased(_ -> button.setScaleX(1.0));

        final Animation pulse = createPulseAnimation(button);

        button.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                button.setScaleX(1.0);
                button.setScaleY(1.0);
                pulse.playFromStart();
            } else {
                pulse.stop();
                button.setScaleX(1.0);
                button.setScaleY(1.0);
            }
        });

        final StackPane layout = new StackPane(text);
        getChildren().setAll(layout);
    }

    private Animation createPulseAnimation(Node node) {
        var up = new ScaleTransition(Duration.seconds(0.3), node);
        up.setToX(1.25);
        up.setToY(1.25);
        up.setInterpolator(Interpolator.EASE_OUT);

        var down = new ScaleTransition(Duration.seconds(0.3), node);
        down.setToX(1.0);
        down.setToY(1.0);
        down.setInterpolator(Interpolator.EASE_IN);

        var pause = new PauseTransition(Duration.seconds(2));

        var twoPulses = new SequentialTransition(up, down);
        twoPulses.setCycleCount(2);

        var seq = new SequentialTransition(twoPulses, pause);
        seq.setCycleCount(Animation.INDEFINITE);
        return seq;
    }
}
