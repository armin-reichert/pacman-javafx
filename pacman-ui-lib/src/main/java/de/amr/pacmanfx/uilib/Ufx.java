/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.Validations;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;
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
            Logger.info("Task '{}' took {} milliseconds", description, duration.toMillis());
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
    public static void toggleBooleanProperty(BooleanProperty booleanProperty) {
        booleanProperty.set(!booleanProperty.get());
    }

    public static <T> void toggleProperty(ObjectProperty<T> property, T either, T other) {
        requireNonNull(property);
        requireNonNull(either);
        requireNonNull(other);
        final T value = property.getValue();
        if (either.equals(value)) {
            property.set(other);
        } else if (other.equals(value)) {
            property.set(either);
        }
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
     * The specular color is automatically set.
     *
     * @param color the base color
     * @return a configured {@link PhongMaterial}
     * @throws IllegalArgumentException if {@code paint} is not a {@link Color}
     */
    public static PhongMaterial coloredPhongMaterial(Color color) {
        requireNonNull(color);
        final var material = new PhongMaterial(color);
        material.setSpecularColor(deriveSpecular(color, 0.8));
        material.setSpecularPower(80);
        return material;
    }

    public static void setDrawMode(Group group, DrawMode drawMode) {
        for (Node node : group.getChildren()) {
            if (node instanceof Group subGroup) {
                setDrawMode(subGroup, drawMode);
            }
            else if (node instanceof Shape3D shape3D) {
                if (!shape3D.drawModeProperty().isBound()) {
                    shape3D.setDrawMode(drawMode);
                }
            }
        }
    }

    public static void bindDrawMode(Group group, ObjectProperty<DrawMode> drawModeProperty) {
        for (Node node : group.getChildren()) {
            if (node instanceof Group subGroup) {
                bindDrawMode(subGroup, drawModeProperty);
            }
            else if (node instanceof Shape3D shape3D) {
                shape3D.drawModeProperty().bind(drawModeProperty);
            }
        }
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
        final var material = new PhongMaterial();
        material.diffuseColorProperty().bind(colorProperty);
        material.specularColorProperty().bind(colorProperty.map(color -> deriveSpecular(color, 0.8)));
        material.setSpecularPower(80);
        return material;
    }

    public static Color deriveSpecular(Color diffuse, double boost) {
        // Clamp boost to [0, 1]
        boost = Math.clamp(boost, 0.0, 1.0);

        // Extract channels
        double r = diffuse.getRed();
        double g = diffuse.getGreen();
        double b = diffuse.getBlue();

        // Normalize so brightest channel becomes 1.0
        double max = Math.max(r, Math.max(g, b));
        if (max == 0.0) {
            return Color.BLACK; // avoid division by zero
        }

        double rn = r / max;
        double gn = g / max;
        double bn = b / max;

        // Apply boost factor
        return new Color(
            rn * boost,
            gn * boost,
            bn * boost,
            1.0
        );
    }

    /**
     * Executes the given action immediately when played in a SequentialTransition.
     * @param action code to run
     * @return empty transition
     */
    public static Transition doNow(Runnable action) {
        requireNonNull(action);
        var transition = new Transition() {
            @Override
            protected void interpolate(double t) {}
        };
        transition.setOnFinished(_ -> action.run());
        return transition;
    }

    /**
     * Pauses for the given number of seconds.
     *
     * @param seconds number of seconds
     * @return pause transition
     */
    public static PauseTransition pauseSec(double seconds) {
        return new PauseTransition(javafx.util.Duration.seconds(seconds));
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
    public static PauseTransition pauseSecThen(double seconds, Runnable action) {
        requireNonNull(action);
        var pause = new PauseTransition(javafx.util.Duration.seconds(seconds));
        pause.setOnFinished(_ -> action.run());
        return pause;
    }

    public static TriangleMesh createScaledTriangleMesh(TriangleMesh mesh, double scale) {
        requireNonNull(mesh);

        final TriangleMesh result = new TriangleMesh();
        result.getTexCoords().addAll(mesh.getTexCoords());
        result.getFaces().addAll(mesh.getFaces());
        result.getFaceSmoothingGroups().addAll(mesh.getFaceSmoothingGroups());
        final float[] points = mesh.getPoints().toArray(null);
        for (int i = 0; i < points.length; i++) {
            points[i] *= (float) scale;
        }
        result.getPoints().addAll(points);

        return result;
    }

    /**
     * Tests whether a sphere intersects an axis-aligned bounding box (AABB).
     *
     * <p>The algorithm:
     * <ol>
     *   <li>Clamp the sphere center to the AABB.</li>
     *   <li>Compute the squared distance between the sphere center and the clamped point.</li>
     *   <li>Intersection occurs if the squared distance is less than or equal to {@code radius²}.</li>
     * </ol>
     *
     * @param sphereCenter the sphere center in the same coordinate system as the AABB
     * @param radius       the sphere radius
     * @param boxMin       minimum AABB corner
     * @param boxMax       maximum AABB corner
     * @return {@code true} if the sphere intersects the AABB
     */
    public static boolean intersectsSphereAABB(Point3D sphereCenter, double radius, Point3D boxMin, Point3D boxMax) {

        final double x = Math.clamp(sphereCenter.getX(), boxMin.getX(), boxMax.getX());
        final double y = Math.clamp(sphereCenter.getY(), boxMin.getY(), boxMax.getY());
        final double z = Math.clamp(sphereCenter.getZ(), boxMin.getZ(), boxMax.getZ());

        final double dx = sphereCenter.getX() - x;
        final double dy = sphereCenter.getY() - y;
        final double dz = sphereCenter.getZ() - z;

        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    /** Background size that scales an image to fill the entire page. */
    public static final BackgroundSize FILL_PAGE_SIZE =
        new BackgroundSize(1.0, 1.0, true, true, false, true);

    /** Background size that scales an image to fit the height. */
    public static final BackgroundSize FIT_HEIGHT_SIZE =
        new BackgroundSize(BackgroundSize.AUTO, 1.0, false, true, true, false);

    /**
     * Creates a solid background fill using the given paint.
     *
     * @param paint the fill paint
     * @return a {@link Background} using the paint
     */
    public static Background paintBackground(Paint paint) {
        requireNonNull(paint);
        return new Background(new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY));
    }

    /**
     * Creates a rounded background with the given paint and corner radius.
     *
     * @param paint  the fill paint
     * @param radius the corner radius in pixels
     * @return a {@link Background} with rounded corners
     */
    public static Background roundedBackground(Paint paint, int radius) {
        requireNonNull(paint);
        return new Background(new BackgroundFill(paint, new CornerRadii(radius), Insets.EMPTY));
    }

    /**
     * Creates a background displaying the given image with default {@link BackgroundImage} settings.
     *
     * @param image the image to display
     * @return a new {@link Background} containing the image
     */
    public static Background createImageBackground(Image image) {
        requireNonNull(image);
        return new Background(new BackgroundImage(image, null, null, null, null));
    }

    /**
     * Creates a background displaying the given image with custom repeat, position, and size settings.
     *
     * @param image    the image to display
     * @param repeatX  horizontal repeat mode
     * @param repeatY  vertical repeat mode
     * @param position background image position
     * @param size     background image size
     * @return a configured {@link Background}
     */
    public static Background createImageBackground(
        Image image,
        BackgroundRepeat repeatX,
        BackgroundRepeat repeatY,
        BackgroundPosition position,
        BackgroundSize size) {

        requireNonNull(image);
        return new Background(new BackgroundImage(image, repeatX, repeatY, position, size));
    }

    /**
     * Creates a wallpaper-posture background: centered, no-repeat, scaled to fill the page.
     *
     * @param image the image to display
     * @return a wallpaper-posture {@link Background}
     */
    public static Background createWallpaper(Image image) {
        return createImageBackground(
            image,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            FILL_PAGE_SIZE
        );
    }

    /**
     * Creates a border with rounded corners.
     *
     * @param color        the border color
     * @param cornerRadius the corner radius in pixels
     * @param width        the border width in pixels
     * @return a {@link Border} with rounded corners
     */
    public static Border roundedBorder(Color color, double cornerRadius, double width) {
        requireNonNull(color);
        return new Border(
            new BorderStroke(color, BorderStrokeStyle.SOLID, new CornerRadii(cornerRadius), new BorderWidths(width)));
    }

    /**
     * Creates a simple rectangular border.
     *
     * @param color the border color
     * @param width the border width in pixels
     * @return a {@link Border}
     */
    public static Border border(Color color, double width) {
        requireNonNull(color);
        return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, null, new BorderWidths(width)));
    }

    /**
     * Converts a {@link Color} to a hexadecimal RGBA string compatible with {@link Color#web(String)}.
     *
     * @param color the color to convert
     * @return a string in the form {@code #RRGGBBAA}
     */
    public static String formatColorHex(Color color) {
        return "#%02x%02x%02x%02x".formatted(
            (int) Math.round(color.getRed()     * 255),
            (int) Math.round(color.getGreen()   * 255),
            (int) Math.round(color.getBlue()    * 255),
            (int) Math.round(color.getOpacity() * 255)
        );
    }

    /**
     * Returns a copy of the given color with the specified opacity.
     *
     * @param color   the base color
     * @param opacity the new opacity (0–1)
     * @return a new {@link Color} with the same RGB values and updated opacity
     */
    public static Color colorWithOpacity(Color color, double opacity) {
        requireNonNull(color);
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    /**
     * Represents a color transformation from one color to another.
     *
     * @param from the original color
     * @param to   the replacement color
     */
    public record ColorChange(Color from, Color to) {}
}
