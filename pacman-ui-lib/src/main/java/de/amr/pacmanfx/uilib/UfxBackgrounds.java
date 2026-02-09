/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import static java.util.Objects.requireNonNull;

public final class UfxBackgrounds {

    /** Background size that scales an image to fill the entire page. */
    public static final BackgroundSize FILL_PAGE_SIZE =
        new BackgroundSize(1.0, 1.0, true, true, false, true);
    /** Background size that scales an image to fit the height. */
    public static final BackgroundSize FIT_HEIGHT_SIZE =
        new BackgroundSize(BackgroundSize.AUTO, 1.0, false, true, true, false);

    private UfxBackgrounds() {}

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
     * Creates a wallpaper-style background: centered, no-repeat, scaled to fill the page.
     *
     * @param image the image to display
     * @return a wallpaper-style {@link Background}
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
}
