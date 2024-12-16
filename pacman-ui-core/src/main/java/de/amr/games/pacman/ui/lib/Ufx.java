/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.lib;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.nes.NES_Palette;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * Useful JavaFX helper methods.
 *
 * @author Armin Reichert
 */
public interface Ufx {

    BackgroundSize FILL_PAGE = new BackgroundSize(1, 1, true, true, false, true);
    BackgroundSize FIT_HEIGHT = new BackgroundSize(AUTO, 1, false, true, true, false);

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

    static ImageView createIcon(Image iconImage, int size, ObservableBooleanValue visibleProperty) {
        var icon = new ImageView(iconImage);
        icon.setFitWidth(size);
        icon.setPreserveRatio(true);
        icon.visibleProperty().bind(visibleProperty);
        return icon;
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

    static Font scaledFont(Font font, double scaling) {
        return Font.font(font.getFamily(), scaling * font.getSize());
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

    record ColorChange(Color from, Color to) {}

    static Image exchangeColors(Map<String, ColorChange> changes, Image source) {
        WritableImage target = new WritableImage((int) source.getWidth(), (int) source.getHeight());
        PixelReader sourceReader = source.getPixelReader();
        PixelWriter w = target.getPixelWriter();

        Set<Color> colorsInImage = new HashSet<>();
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                Color color = sourceReader.getColor(x, y);
                w.setColor(x, y, color);
                colorsInImage.add(color);
            }
        }
        Logger.info("Colors in image before: {}", colorsInImage);

        boolean fillColorFound = false, strokeColorFound = false, pelletColorFound = false;
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                final Color color = sourceReader.getColor(x, y);
                if (color.equals(changes.get("fill").from)) {
                    fillColorFound = true;
                    w.setColor(x, y, changes.get("fill").to);
                }
                else if (color.equals(changes.get("stroke").from)) {
                    strokeColorFound = true;
                    w.setColor(x, y, changes.get("stroke").to);
                }
                else if (color.equals(changes.get("pellet").from)) {
                    pelletColorFound = true;
                    w.setColor(x, y, changes.get("pellet").to);
                }
            }
        }
        if (!fillColorFound) {
            Logger.warn("Fill color {} not found in image, WTF?", changes.get("fill").from);
        }
        if (!strokeColorFound) {
            Logger.warn("Stroke color {} not found in image, WTF?", changes.get("stroke").from);
        }
        if (!pelletColorFound) {
            Logger.warn("Pellet color {} not found in image, WTF?", changes.get("pellet").from);
        }

        colorsInImage.clear();
        PixelReader targetReader = target.getPixelReader();
        for (int x = 0; x < target.getWidth(); ++x) {
            for (int y = 0; y < target.getHeight(); ++y) {
                Color color = targetReader.getColor(x, y);
                w.setColor(x, y, color);
                colorsInImage.add(color);
            }
        }
        Logger.info("Colors in image after: {}", colorsInImage);

        return target;
    }

    static Image maskImage(Image source, BiPredicate<Integer, Integer> isMasked, Color maskColor) {
        WritableImage target = new WritableImage((int) source.getWidth(), (int) source.getHeight());
        PixelReader sourceReader = source.getPixelReader();
        PixelWriter w = target.getPixelWriter();
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                if (isMasked.test(x, y)) {
                    w.setColor(x, y, maskColor);
                } else {
                    w.setColor(x, y, sourceReader.getColor(x, y));
                }
            }
        }
        return target;
    }

    static Image subImage(Image source, RectArea area) {
        WritableImage target = new WritableImage(area.width(), area.height());
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = target.getPixelWriter();
        for (int y = 0; y < area.height(); ++y) {
            for (int x = 0; x < area.width(); ++x) {
                Color color = reader.getColor(area.x()  + x, area.y() + y);
                writer.setColor(x, y, color);
            }
        }
        return target;
    }

    static boolean checkForNonNES_PaletteColors(Image image) {
        boolean found = false;
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                Color color = reader.getColor(x, y);
                if (color.equals(Color.TRANSPARENT)) continue;
                if (!NES_PALETTE_COLORS.contains(color)) {
                    Logger.warn("Found non-NES palette color {} at x={} y={}", color, x, y);
                    found = true;
                }
            }
        }
        return found;
    }

    Set<Color> NES_PALETTE_COLORS = Stream.of(NES_Palette.COLORS).map(Color::valueOf).collect(Collectors.toSet());

    static Image exchange_NESColorScheme(Image source, NES_ColorScheme from, NES_ColorScheme to) {
        Map<String, ColorChange> changes = Map.of(
            "fill", new ColorChange(Color.valueOf(from.fillColor()), Color.valueOf(to.fillColor())),
            "stroke", new ColorChange(Color.valueOf(from.strokeColor()), Color.valueOf(to.strokeColor())),
            "pellet", new ColorChange(Color.valueOf(from.pelletColor()), Color.valueOf(to.pelletColor()))
        );
        return exchangeColors(changes, source);
    }
}