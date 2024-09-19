/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.lib.Direction;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.PrintWriter;
import java.util.Locale;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Useful JavaFX helper methods.
 *
 * @author Armin Reichert
 */
public interface Ufx {

    BackgroundSize FILL_PAGE = new BackgroundSize(1, 1, true, true, false, true);

    /**
     * Launches the application specified by the given class. In case an exception is thrown,
     * the stacktrace is written to a log file ("oh_shit.txt").
     *
     * @param appClass application class
     * @param args application arguments
     */
    static void launchApplication(Class<? extends Application> appClass, String... args) {
        try {
            Logger.info("Java version:     {}", Runtime.version());
            Logger.info("Locale (default): {}", Locale.getDefault());
            Application.launch(appClass, args);
        } catch (Throwable x) {
            try (var pw = new PrintWriter("oh_shit.txt")) {
                x.printStackTrace(pw);
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    static void toggle(BooleanProperty booleanProperty) {
        booleanProperty.set(!booleanProperty.get());
    }


    static  Color opaqueColor(Color color, double opacity) {
        checkNotNull(color);
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    static  Background coloredBackground(Color color) {
        checkNotNull(color);
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    static  Background coloredRoundedBackground(Color color, int radius) {
        checkNotNull(color);
        return new Background(new BackgroundFill(color, new CornerRadii(radius), Insets.EMPTY));
    }

    /**
     * @param image some source
     * @return source background with default properties, see {@link BackgroundImage}
     */
    static Background imageBackground(Image image) {
        return new Background(new BackgroundImage(image, null, null, null, null));
    }

    static Background wallpaperBackground(Image image) {
        return imageBackground(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, FILL_PAGE);
    }

    /**
     * @param image some source
     * @return source background with specified attributes
     */
    static Background imageBackground(Image image, BackgroundRepeat repeatX, BackgroundRepeat repeatY,
                                       BackgroundPosition position, BackgroundSize size) {
        return new Background(new BackgroundImage(image, repeatX, repeatY, position, size));
    }

    static Border roundedBorder(Color color, double cornerRadius, double width) {
        checkNotNull(color);
        return new Border(
            new BorderStroke(color, BorderStrokeStyle.SOLID, new CornerRadii(cornerRadius), new BorderWidths(width)));
    }

    static Border border(Color color, double width) {
        checkNotNull(color);
        return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, null, new BorderWidths(width)));
    }

    static PhongMaterial coloredMaterial(Color color) {
        checkNotNull(color);
        var material = new PhongMaterial(color);
        material.setSpecularColor(color.brighter());
        return material;
    }

    static PhongMaterial coloredMaterial(ObjectProperty<Color> colorProperty) {
        checkNotNull(colorProperty);
        var material = new PhongMaterial();
        material.diffuseColorProperty().bind(colorProperty);
        material.specularColorProperty().bind(colorProperty.map(Color::brighter));
        return material;
    }

    /**
     * Pauses for the given number of seconds.
     *
     * @param seconds number of seconds
     * @return pause transition
     */
    static PauseTransition pauseSec(double seconds) {
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
    static Transition doAfterSec(double delaySeconds, Runnable action) {
        checkNotNull(action);
        var pause = new PauseTransition(Duration.seconds(delaySeconds));
        pause.setOnFinished(e -> action.run());
        return pause;
    }

    static Animation doAfterSec(double delaySeconds, Animation animation) {
        checkNotNull(animation);
        animation.setDelay(Duration.seconds(delaySeconds).add(animation.getDelay()));
        return animation;
    }

    static Transition now(Runnable action) {
        checkNotNull(action);
        var wrapper = new Transition() {
            @Override
            protected void interpolate(double frac) {}
        };
        wrapper.setOnFinished(e -> action.run());
        return wrapper;
    }

    static int angle(Direction dir) {
        return switch (dir) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        };
    }
}