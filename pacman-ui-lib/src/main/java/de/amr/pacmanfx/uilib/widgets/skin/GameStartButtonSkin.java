/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets.skin;

import de.amr.pacmanfx.uilib.widgets.GameStartButton;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class GameStartButtonSkin extends SkinBase<GameStartButton> {

    public GameStartButtonSkin(GameStartButton control) {
        super(control);

        final Text text = new Text();
        text.textProperty().bind(control.textProperty());
        text.fontProperty().bind(control.fontProperty());
        text.setFill(Color.WHITE);

        final Pane root = new StackPane(text);

        root.getStyleClass().add("game-start-button-container");

        // Without this the button fill the complete area
        root.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        root.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

        root.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                e.consume();
                control.onActionProperty().get().run();
            }
        });

        root.setOnMouseEntered(_ -> root.setOpacity(0.85));
        root.setOnMouseExited(_ -> root.setOpacity(1.0));
        root.setOnMousePressed(_ -> root.setScaleX(0.97));
        root.setOnMouseReleased(_ -> root.setScaleX(1.0));

        final Animation pulse = createPulseAnimation(root);

        control.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                root.setScaleX(1.0);
                root.setScaleY(1.0);
                pulse.playFromStart();
            } else {
                pulse.stop();
                root.setScaleX(1.0);
                root.setScaleY(1.0);
            }
        });

        getChildren().add(root);
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
