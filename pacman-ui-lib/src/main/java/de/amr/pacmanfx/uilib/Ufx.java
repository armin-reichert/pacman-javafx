/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.math.Vector2i;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import org.tinylog.Logger;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Utility class containing helper methods for JavaFX UI, image processing, colors,
 * materials, fonts, and layout operations used throughout the Pac-Man FX project.
 *
 * <p>This class is non-instantiable and provides only static utility methods.</p>
 */
public final class Ufx {

    private Ufx() {}

    /**
     * Computes the size of a screen section that occupies a given fraction of the available
     * screen height while maintaining a fixed aspect ratio.
     *
     * @param aspectRatio    the desired width/height ratio (e.g. 16.0/10.0)
     * @param heightFraction fraction of the available screen height to use (0–1)
     * @return a {@link Vector2i} containing the computed width and height
     */
    public static Vector2i computeScreenSectionSize(double aspectRatio, double heightFraction) {
        final double availableHeight = Screen.getPrimary().getVisualBounds().getHeight();
        final double height = Math.floor(heightFraction * availableHeight);
        final double width = Math.floor(aspectRatio * height);
        return new Vector2i((int)width, (int)height);
    }

    /**
     * Launches the given JavaFX application class. If an exception occurs during startup,
     * the full stack trace is written to {@code oh_shit.txt}.
     *
     * @param appClass the JavaFX {@link Application} subclass to launch
     * @param args     optional application arguments
     */
    public static void startApplication(Class<? extends Application> appClass, String... args) {
        try {
            Logger.info("Java runtime: {}", Runtime.version());
            Logger.info("User Language: {}", System.getProperty("user.language"));
            Logger.info("User Country: {}", System.getProperty("user.country"));
            Logger.info("Default Locale: {}", Locale.getDefault());
            Application.launch(appClass, args);
        } catch (Throwable x) {
            x.printStackTrace(System.err);
            try (var pw = new PrintWriter("oh_shit.txt")) {
                x.printStackTrace(pw);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Measures the execution time of the given runnable and logs the duration in milliseconds.
     *
     * @param description description included in the log output
     * @param code        the code block to execute and measure
     * @return the measured {@link Duration}, or {@link Optional#empty()} if an exception occurs
     */
    public static Optional<Duration> measureDuration(String description, Runnable code) {
        final Instant start = Instant.now();
        try {
            code.run();
            final Duration duration = Duration.between(start, Instant.now());
            Logger.info("Runtime: {} ms: '{}'", duration.toMillis(), description);
            return Optional.of(duration);
        } catch (Exception x) {
            Logger.error("Error running code in measurement", x);
            return Optional.empty();
        }
    }

    /**
     * Toggles the value of the given {@link BooleanProperty}.
     *
     * @param booleanProperty the property to toggle
     */
    public static void toggle(BooleanProperty booleanProperty) {
        booleanProperty.set(!booleanProperty.get());
    }

    /**
     * Returns a new font with the same family as the given font but with a different size.
     *
     * @param font the base font
     * @param size the new font size (must be non-negative)
     * @return a resized {@link Font}
     */
    public static Font deriveFont(Font font, double size) {
        requireNonNull(font);
        Validations.requireNonNegative(size);
        return Font.font(font.getFamily(), size);
    }

    /**
     * Scales the given font by a multiplicative factor.
     *
     * @param font   the base font
     * @param factor scale factor (e.g. 1.5 enlarges by 50%)
     * @return a new {@link Font} with scaled size
     */
    public static Font scaleFontBy(Font font, double factor) {
        return deriveFont(font, factor * font.getSize());
    }

    /**
     * Computes the layout width of the given string when rendered with the specified font.
     *
     * @param s    the text to measure
     * @param font the font used for measurement
     * @return the width in pixels
     */
    public static double textWidth(String s, Font font) {
        final Text dummy = new Text(s);
        dummy.setFont(font);
        return dummy.getLayoutBounds().getWidth();
    }

    /**
     * Creates a {@link PhongMaterial} using the given color for both diffuse and specular components.
     * The specular color is automatically brightened.
     *
     * @param color the base color
     * @return a configured {@link PhongMaterial}
     * @throws IllegalArgumentException if {@code paint} is not a {@link Color}
     */
    public static PhongMaterial coloredPhongMaterial(Color color) {
        requireNonNull(color);
        var material = new PhongMaterial(color);
        material.setSpecularColor(color.brighter());
        return material;
    }

    /**
     * Creates a {@link PhongMaterial} whose diffuse and specular colors are bound to the given
     * observable color property. The specular color is always the brighter version of the diffuse color.
     *
     * @param colorProperty observable color value
     * @return a bound {@link PhongMaterial}
     */
    public static PhongMaterial colorBoundPhongMaterial(ObservableValue<Color> colorProperty) {
        requireNonNull(colorProperty);
        var material = new PhongMaterial();
        material.diffuseColorProperty().bind(colorProperty);
        material.specularColorProperty().bind(colorProperty.map(Color::brighter));
        return material;
    }
}
