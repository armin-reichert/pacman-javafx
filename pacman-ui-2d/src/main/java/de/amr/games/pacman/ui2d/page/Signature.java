/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class Signature extends TextFlow {

    public final ObjectProperty<Font> fontPy = new SimpleObjectProperty<>(this, "font", Font.font("Serif"));
    private final Transition animation;
    private final FadeTransition fadeIn;
    private final FadeTransition fadeOut;

    public Signature(String... words) {
        fadeIn = new FadeTransition(Duration.seconds(2), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.seconds(2));

        fadeOut = new FadeTransition(Duration.seconds(3), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        animation = new SequentialTransition(fadeIn, fadeOut);
        setOpacity(0);

        if (words.length > 0) {
            setWords(words);
        }
    }

    public void setWords(String... words) {
        if (words.length > 0) {
            var texts = new Text[words.length];
            for (int i = 0; i < words.length; ++i) {
                texts[i] = new Text(words[i]);
                texts[i].setFill(Color.grayRgb(200));
                texts[i].fontProperty().bind(fontPy);
            }
            getChildren().setAll(texts);
        }
    }

    public void show(double fadeInSec, double fadeOutSec) {
        fadeIn.setDuration(Duration.seconds(fadeInSec));
        fadeOut.setDuration(Duration.seconds(fadeOutSec));
        animation.playFromStart();
    }

    public void hide() {
        animation.stop();
        setOpacity(0);
    }
}