/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
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

import static java.util.Objects.requireNonNull;

/**
 * Useful JavaFX helper methods.
 */
public interface Ufx {

    record ColorChange(Color from, Color to) {}

    BackgroundSize FILL_PAGE_SIZE  = new BackgroundSize(1.0, 1.0, true, true, false, true);
    BackgroundSize FIT_HEIGHT_SIZE = new BackgroundSize(BackgroundSize.AUTO, 1.0, false, true, true, false);

    /**
     * Starts the JavaFX application specified by the given class. In case an exception is thrown,
     * the stacktrace is written to a log file ("oh_shit.txt").
     *
     * @param appClass application class
     * @param args application arguments
     */
    static void startApplication(Class<? extends Application> appClass, String... args) {
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

    static void toggle(BooleanProperty booleanProperty) {
        booleanProperty.set(!booleanProperty.get());
    }

    /**
     * @param color a color
     * @return string representation of RGBA color that can be parsed with {@link Color#web(String)}.
     */
    static String formatColorHex(Color color) {
        return "#%02x%02x%02x%02x".formatted(
            (int) Math.round(color.getRed()     * 255),
            (int) Math.round(color.getGreen()   * 255),
            (int) Math.round(color.getBlue()    * 255),
            (int) Math.round(color.getOpacity() * 255)
        );
    }

    static Color colorWithOpacity(Color color, double opacity) {
        requireNonNull(color);
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    static Background colorBackground(Color color) {
        requireNonNull(color);
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    static Background roundedBackground(Color color, int radius) {
        requireNonNull(color);
        return new Background(new BackgroundFill(color, new CornerRadii(radius), Insets.EMPTY));
    }

    /**
     * @param image image shown as background
     * @return background object with default properties, see {@link BackgroundImage}
     */
    static Background createBackground(Image image) {
        requireNonNull(image);
        return new Background(new BackgroundImage(image, null, null, null, null));
    }

    /**
     * @param image some source
     * @return source background with specified attributes
     */
    static Background createBackground(Image image, BackgroundRepeat repeatX, BackgroundRepeat repeatY,
                                       BackgroundPosition position, BackgroundSize size) {
        requireNonNull(image);
        return new Background(new BackgroundImage(image, repeatX, repeatY, position, size));
    }

    /**
     * @param image image shown as background
     * @return background object with properties suitable for a wallpaper
     */
    static Background createWallpaper(Image image) {
        return createBackground(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, FILL_PAGE_SIZE);
    }

    static Border roundedBorder(Color color, double cornerRadius, double width) {
        requireNonNull(color);
        return new Border(
            new BorderStroke(color, BorderStrokeStyle.SOLID, new CornerRadii(cornerRadius), new BorderWidths(width)));
    }

    static Border border(Color color, double width) {
        requireNonNull(color);
        return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, null, new BorderWidths(width)));
    }

    static Font scaledFont(Font font, double scaling) {
        return Font.font(font.getFamily(), scaling * font.getSize());
    }

    static PhongMaterial coloredPhongMaterial(Color color) {
        requireNonNull(color);
        var material = new PhongMaterial(color);
        material.setSpecularColor(color.brighter());
        return material;
    }

    static Transition doNow(Runnable action) {
        requireNonNull(action);
        var transition = new Transition() {
            @Override
            protected void interpolate(double t) {}
        };
        transition.setOnFinished(e -> action.run());
        return transition;
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
     * Prepends a pause of the given duration (in seconds) before the given action is executed. Note that you have to call
     * {@link Animation#play()} to execute the action!
     * <p>
     * NOTE: Do NOT start an animation in the action!
     *
     * @param seconds number of seconds to wait before the action is executed
     * @param action       code to run
     * @return pause transition
     */
    static PauseTransition pauseSec(double seconds, Runnable action) {
        requireNonNull(action);
        var pause = new PauseTransition(Duration.seconds(seconds));
        pause.setOnFinished(e -> action.run());
        return pause;
    }

    static Image imageToGreyscale(Image source) {
        if (source == null) {
            return null;
        }
        int width = (int) source.getWidth(), height = (int) source.getHeight();
        WritableImage target = new WritableImage(width, height);
        PixelReader r = source.getPixelReader();
        PixelWriter w = target.getPixelWriter();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                Color color = r.getColor(x, y);
                double g = (color.getRed() + color.getBlue() + color.getGreen()) / 3.0;
                w.setColor(x, y, Color.gray(g));
            }
        }
        return target;
    }

    /**
     * @param sourceImage source image
     * @param r rectangular region
     * @return image copy of region
     */
    static Image subImage(Image sourceImage, RectShort r) {
        return subImage(sourceImage, r.x(), r.y(), r.width(), r.height());
    }

    /**
     * @param sourceImage source image
     * @param x      region x-coordinate
     * @param y      region y-coordinate
     * @param width  region width
     * @param height region height
     * @return image copy of region
     */
    static Image subImage(Image sourceImage, int x, int y, int width, int height) {
        var section = new WritableImage(width, height);
        section.getPixelWriter().setPixels(0, 0, width, height, sourceImage.getPixelReader(), x, y);
        return section;
    }

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

    static Image recolorPixels(Image image, BiPredicate<Integer, Integer> insideMaskedArea, Color maskColor) {
        WritableImage result = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter w = result.getPixelWriter();
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                if (insideMaskedArea.test(x, y)) {
                    w.setColor(x, y, maskColor);
                } else {
                    w.setColor(x, y, image.getPixelReader().getColor(x, y));
                }
            }
        }
        return result;
    }

    static boolean checkForNonNES_PaletteColors(Image image) {
        Set<Color> NES_colors = Stream.of(NES_Palette.COLORS).map(Color::valueOf).collect(Collectors.toSet());
        boolean found = false;
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                Color color = reader.getColor(x, y);
                if (color.equals(Color.TRANSPARENT)) continue;
                if (!NES_colors.contains(color)) {
                    Logger.warn("Found non-NES palette color {} at x={} y={}", color, x, y);
                    found = true;
                }
            }
        }
        return found;
    }

    static Image exchangeNES_ColorScheme(Image image, NES_ColorScheme sourceScheme, NES_ColorScheme targetScheme) {
        Map<String, ColorChange> colorChanges = Map.of(
            "fill",   new ColorChange(Color.web(sourceScheme.fillColorRGB()),   Color.web(targetScheme.fillColorRGB())),
            "stroke", new ColorChange(Color.web(sourceScheme.strokeColorRGB()), Color.web(targetScheme.strokeColorRGB())),
            "pellet", new ColorChange(Color.web(sourceScheme.pelletColorRGB()), Color.web(targetScheme.pelletColorRGB()))
        );
        return exchangeColors(colorChanges, image);
    }
}