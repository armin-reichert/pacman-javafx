/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.page;

import de.amr.games.pacman.ui.fx.GameSceneContext;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class Signature extends TextFlow {

    private final Transition signatureAnimation;
    private final Text remakeText = new Text("Remake (2021-2024) by ");
    private final Text authorText = new Text("Armin Reichert");

    Signature(GameSceneContext context) {
        remakeText.setFill(Color.grayRgb(200));
        authorText.setFill(Color.grayRgb(200));
        getChildren().addAll(remakeText, authorText);

        var fadeIn = new FadeTransition(Duration.seconds(2), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.seconds(2));

        var fadeOut = new FadeTransition(Duration.seconds(3), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        signatureAnimation = new SequentialTransition(fadeIn, fadeOut);
        setOpacity(0);
    }

    public Text authorText() {
        return authorText;
    }

    public Text remakeText() {
        return remakeText;
    }

    void show() {
        signatureAnimation.playFromStart();
    }

    void hide() {
        signatureAnimation.stop();
        setOpacity(0);
    }
}