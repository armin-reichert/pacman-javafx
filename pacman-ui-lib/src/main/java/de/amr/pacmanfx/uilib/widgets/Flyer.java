/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.Ufx;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.util.Arrays;

public class Flyer extends StackPane {

    public enum LayoutMode { FILL, FIT_HEIGHT }

    private final Image[] images;
    private final LayoutMode[] layoutModes;
    private int index;

    public Flyer(Image... images) {
        if (images.length == 0) {
            throw new IllegalArgumentException("No flyer images sepecified");
        }
        this.images = images;
        this.layoutModes = new LayoutMode[images.length];
        Arrays.fill(layoutModes, LayoutMode.FIT_HEIGHT);
        selectPage(0);
    }

    private void fill(Image image) {
        setBackground(Ufx.createImageBackground(image,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER, Ufx.FILL_PAGE_SIZE));
    }

    private void fitHeight(Image image) {
        setBackground(new Background(new BackgroundImage(image,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER, Ufx.FIT_HEIGHT_SIZE)));
    }

    public void selectPage(int index) {
        this.index = index;
        if (layoutModes[index] == LayoutMode.FILL) {
            fill(images[index]);
        } else {
            fitHeight(images[index]);
        }
    }

    public void setPageLayout(int index, LayoutMode layoutMode) {
        layoutModes[index] = layoutMode;
    }

    public void nextFlyerPage() {
        selectPage((index + 1) % images.length);
    }

    public void prevFlyerPage() {
        selectPage((index - 1 + images.length) % images.length);
    }
}
