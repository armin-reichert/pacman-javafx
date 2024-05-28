/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Useful JavaFX helper methods.
 *
 * @author Armin Reichert
 */
public class Ufx {

    private Ufx() {
    }

    public static void toggle(BooleanProperty booleanProperty) {
        booleanProperty.set(!booleanProperty.get());
    }


    public static  Background coloredBackground(Color color) {
        checkNotNull(color);
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    public static  Background coloredRoundedBackground(Color color, int radius) {
        checkNotNull(color);
        return new Background(new BackgroundFill(color, new CornerRadii(radius), Insets.EMPTY));
    }

    public static  Border roundedBorder(Color color, double cornerRadius, double width) {
        checkNotNull(color);
        return new Border(
            new BorderStroke(color, BorderStrokeStyle.SOLID, new CornerRadii(cornerRadius), new BorderWidths(width)));
    }

    public static  Border border(Color color, double width) {
        checkNotNull(color);
        return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, null, new BorderWidths(width)));
    }

    public static  PhongMaterial coloredMaterial(Color color) {
        checkNotNull(color);
        var material = new PhongMaterial(color);
        material.setSpecularColor(color.brighter());
        return material;
    }

    public static  Color opaqueColor(Color color, double opacity) {
        checkNotNull(color);
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    public static PhongMaterial createColorBoundMaterial(ObjectProperty<Color> diffuseColorProperty) {
        var material = new PhongMaterial();
        material.diffuseColorProperty().bind(diffuseColorProperty);
        material.specularColorProperty()
            .bind(Bindings.createObjectBinding(() -> diffuseColorProperty.get().brighter(), diffuseColorProperty));
        return material;
    }

    /**
     * Pauses for the given number of seconds.
     *
     * @param seconds number of seconds
     * @return pause transition
     */
    public static PauseTransition pauseSec(double seconds) {
        return new PauseTransition(Duration.seconds(seconds));
    }

    /**
     * Prepends a pause of the given duration (in seconds) before the given action can be run. Note that you have to call
     * {@link Animation#play()} to execute the action!
     * <p>
     * NOTE: Do NOT start an animation in the code!
     *
     * @param delaySeconds number of seconds
     * @param action       code to run
     * @return pause transition
     */
    public static Transition doAfterSec(double delaySeconds, Runnable action) {
        checkNotNull(action);
        var pause = new PauseTransition(Duration.seconds(delaySeconds));
        pause.setOnFinished(e -> action.run());
        return pause;
    }

    public static Transition now(Runnable action) {
        checkNotNull(action);
        var wrapper = new Transition() {
            @Override
            protected void interpolate(double frac) {
            }
        };
        wrapper.setOnFinished(e -> action.run());
        return wrapper;
    }
}