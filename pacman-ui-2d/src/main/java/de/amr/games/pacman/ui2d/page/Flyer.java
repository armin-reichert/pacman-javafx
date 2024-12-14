/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui.lib.Ufx;
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
        selectFlyerPage(0);
    }

    private void fill(Image image) {
        setBackground(Ufx.imageBackground(image,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER, Ufx.FILL_PAGE));
    }

    private void fitHeight(Image image) {
        setBackground(new Background(new BackgroundImage(image,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER, Ufx.FIT_HEIGHT)));
    }

    public void selectFlyerPage(int index) {
        this.index = index;
        if (layoutModes[index] == LayoutMode.FILL) {
            fill(images[index]);
        } else {
            fitHeight(images[index]);
        }
    }

    public void setLayoutMode(int index, LayoutMode layoutMode) {
        layoutModes[index] = layoutMode;
    }

    public void nextFlyerPage() {
        selectFlyerPage((index + 1) % images.length);
    }

    public void prevFlyerPage() {
        selectFlyerPage((index - 1 + images.length) % images.length);
    }
}
