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
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * Useful JavaFX helper methods.
 *
 * @author Armin Reichert
 */
public interface Ufx {

    BackgroundSize FILL_PAGE = new BackgroundSize(1, 1, true, true, false, true);
    BackgroundSize FIT_HEIGHT = new BackgroundSize(AUTO, 1, false, true, true, false);

    Font  CONTEXT_MENU_TITLE_FONT = Font.font("Dialog", FontWeight.BLACK, 14);
    Color CONTEXT_MENU_TITLE_BACKGROUND = Color.CORNFLOWERBLUE; // "Kornblumenblau, sind die Augen der Frauen beim Weine..."

    static MenuItem menuTitleItem(String title) {
        var text = new Text(title);
        text.setFont(CONTEXT_MENU_TITLE_FONT);
        text.setFill(CONTEXT_MENU_TITLE_BACKGROUND);
        return new CustomMenuItem(text);
    }

    /**
     * Starts the JavaFX application specified by the given class. In case an exception is thrown,
     * the stacktrace is written to a log file ("oh_shit.txt").
     *
     * @param appClass application class
     * @param args application arguments
     */
    static void startApplication(Class<? extends Application> appClass, String... args) {
        try {
            Logger.info("Java version: {}", Runtime.version());
            Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
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

    static String formatColorHex(Color color) {
        return String.format("#%02x%02x%02x", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    static  Color opaqueColor(Color color, double opacity) {
        requireNonNull(color);
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    static  Background coloredBackground(Color color) {
        requireNonNull(color);
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    static  Background coloredRoundedBackground(Color color, int radius) {
        requireNonNull(color);
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

    static PhongMaterial coloredPhongMaterial(ObjectProperty<Color> colorProperty) {
        requireNonNull(colorProperty);
        var material = new PhongMaterial();
        material.diffuseColorProperty().bind(colorProperty);
        material.specularColorProperty().bind(colorProperty.map(Color::brighter));
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
    static Transition pauseSec(double seconds, Runnable action) {
        requireNonNull(action);
        var pause = new PauseTransition(Duration.seconds(seconds));
        pause.setOnFinished(e -> action.run());
        return pause;
    }

    record ColorChange(Color from, Color to) {}

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

    static Image recolorImage(Image image, NES_ColorScheme sourceColorScheme, NES_ColorScheme targetColorScheme) {
        Map<String, ColorChange> colorChanges = Map.of(
            "fill",   new ColorChange(Color.web(sourceColorScheme.fillColorRGB()),   Color.web(targetColorScheme.fillColorRGB())),
            "stroke", new ColorChange(Color.web(sourceColorScheme.strokeColorRGB()), Color.web(targetColorScheme.strokeColorRGB())),
            "pellet", new ColorChange(Color.web(sourceColorScheme.pelletColorRGB()), Color.web(targetColorScheme.pelletColorRGB()))
        );
        return exchangeColors(colorChanges, image);
    }
}