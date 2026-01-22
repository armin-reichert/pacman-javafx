/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.world.Obstacle;
import de.amr.pacmanfx.model.world.WorldMap;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import org.tinylog.Logger;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Useful JavaFX helper methods.
 */
public interface Ufx {

    record ColorChange(Color from, Color to) {}

    BackgroundSize FILL_PAGE_SIZE  = new BackgroundSize(1.0, 1.0, true, true, false, true);
    BackgroundSize FIT_HEIGHT_SIZE = new BackgroundSize(BackgroundSize.AUTO, 1.0, false, true, true, false);

    static Dimension2D computeSceneSize(double aspectRatio, double heightFraction) {
        final double availableHeight = Screen.getPrimary().getVisualBounds().getHeight();
        final double height = Math.floor(heightFraction * availableHeight);
        final double width = Math.floor(aspectRatio * height);
        return new Dimension2D(width, height);
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

    static Duration measureDuration(String description, Runnable code) {
        final Instant start = Instant.now();
        try {
            code.run();
            Duration duration = Duration.between(start, Instant.now());
            Logger.info("{} millis: '{}'", duration.toMillis(), description);
            return duration;
        } catch (Exception x) {
            Logger.error("Error running code in measurement");
            return Duration.ZERO;
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

    static Background paintBackground(Paint paint) {
        requireNonNull(paint);
        return new Background(new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY));
    }

    static Background roundedBackground(Paint paint, int radius) {
        requireNonNull(paint);
        return new Background(new BackgroundFill(paint, new CornerRadii(radius), Insets.EMPTY));
    }

    /**
     * @param image image shown as background
     * @return background object with default properties, see {@link BackgroundImage}
     */
    static Background createImageBackground(Image image) {
        requireNonNull(image);
        return new Background(new BackgroundImage(image, null, null, null, null));
    }

    /**
     * @param image some source
     * @return source background with specified attributes
     */
    static Background createImageBackground(Image image, BackgroundRepeat repeatX, BackgroundRepeat repeatY,
                                            BackgroundPosition position, BackgroundSize size) {
        requireNonNull(image);
        return new Background(new BackgroundImage(image, repeatX, repeatY, position, size));
    }

    /**
     * @param image image shown as background
     * @return background object with properties suitable for a wallpaper
     */
    static Background createWallpaper(Image image) {
        return createImageBackground(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, FILL_PAGE_SIZE);
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

    static Font deriveFont(Font font, double size) {
        requireNonNull(font);
        Validations.requireNonNegative(size);
        return Font.font(font.getFamily(), size);
    }

    static Font scaleFontBy(Font font, double factor) {
        return deriveFont(font, factor * font.getSize());
    }

    static double textWidth(String s, Font font) {
        final Text dummy = new Text(s);
        dummy.setFont(font);
        return dummy.getLayoutBounds().getWidth();
    }

    /**
     * @param paint color
     * @return material with Phong shading (diffuse=color, specular=color.brighter)
     */
    static PhongMaterial defaultPhongMaterial(Paint paint) {
        requireNonNull(paint);
        if (paint instanceof Color color) {
            var material = new PhongMaterial(color);
            material.setSpecularColor(color.brighter());
            return material;
        }
        throw new IllegalArgumentException("Phong material needs color, no general paint");
    }

    /**
     * @param colorProperty color property
     * @return material with Phong shading (diffuse=color, specular=color.brighter)
     */
    static PhongMaterial defaultPhongMaterial(ObservableValue<Color> colorProperty) {
        requireNonNull(colorProperty);
        var material = new PhongMaterial();
        material.diffuseColorProperty().bind(colorProperty);
        material.specularColorProperty().bind(colorProperty.map(Color::brighter));
        return material;
    }

    static Image recolorImage(Image source, Map<Color, Color> changes) {
        WritableImage target = new WritableImage((int) source.getWidth(), (int) source.getHeight());
        PixelReader sourceReader = source.getPixelReader();
        PixelWriter targetWriter = target.getPixelWriter();
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                final Color color = sourceReader.getColor(x, y);
                final Color newColor = changes.get(color);
                if (newColor != null) {
                    targetWriter.setColor(x, y, newColor);
                }
            }
        }
        return target;
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

    static Image replaceImageColors(
        Image sourceImage,
        Color fillFrom, Color strokeFrom, Color pelletFrom,
        Color fillTo, Color strokeTo, Color pelletTo) {

        WritableImage targetImage = new WritableImage((int) sourceImage.getWidth(), (int) sourceImage.getHeight());
        PixelReader sourceReader = sourceImage.getPixelReader();
        PixelWriter targetWriter = targetImage.getPixelWriter();
        boolean fillColorFound = false, strokeColorFound = false, pelletColorFound = false;
        for (int x = 0; x < sourceImage.getWidth(); ++x) {
            for (int y = 0; y < sourceImage.getHeight(); ++y) {
                final Color color = sourceReader.getColor(x, y);
                if (color.equals(fillFrom)) {
                    fillColorFound = true;
                    targetWriter.setColor(x, y, fillTo);
                }
                else if (color.equals(strokeFrom)) {
                    strokeColorFound = true;
                    targetWriter.setColor(x, y, strokeTo);
                }
                else if (color.equals(pelletFrom)) {
                    pelletColorFound = true;
                    targetWriter.setColor(x, y, pelletTo);
                }
            }
        }
        if (!fillColorFound) {
            Logger.warn("Fill color {} not found in image, WTF?", fillFrom);
        }
        if (!strokeColorFound) {
            Logger.warn("Stroke color {} not found in image, WTF?", strokeFrom);
        }
        if (!pelletColorFound) {
            Logger.warn("Pellet color {} not found in image, WTF?", pelletFrom);
        }
        return targetImage;
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

    /**
     * Tests whether a sphere collides with an axis-aligned box.
     *
     * @param cx     sphere center x
     * @param cy     sphere center y
     * @param cz     sphere center z
     * @param radius sphere radius
     * @param xmin   box minimum x
     * @param ymin   box minimum y
     * @param zmin   box minimum z
     * @param xmax   box maximum x
     * @param ymax   box maximum y
     * @param zmax   box maximum z
     * @return true if sphere intersects box
     */
    static boolean intersectsSphereBox(
        double cx, double cy, double cz, double radius,
        double xmin, double ymin, double zmin,
        double xmax, double ymax, double zmax) {

        // Find closest point on box to sphere center
        double px = Math.clamp(cx, xmin, xmax);
        double py = Math.clamp(cy, ymin, ymax);
        double pz = Math.clamp(cz, zmin, zmax);

        // Compute squared distance
        double dx = px - cx;
        double dy = py - cy;
        double dz = pz - cz;
        double dist2 = dx*dx + dy*dy + dz*dz;

        return dist2 <= radius * radius;
    }

    //TODO check if this covers all cases
    static boolean isBorderObstacle(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == worldMap.terrainLayer().emptyRowsOverMaze() * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }
}