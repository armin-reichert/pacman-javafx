/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.viewmodel;

import de.amr.pacmanfx.ui.settings.ui.MiniViewSettings;
import javafx.beans.property.*;

public class MiniViewSettingsViewModel {

    private final IntegerProperty minHeight;
    private final IntegerProperty maxHeight;
    private final IntegerProperty height;
    private final BooleanProperty active;
    private final IntegerProperty opacityPercentage;
    private final FloatProperty slideInSeconds;
    private final FloatProperty slideOutSeconds;

    public MiniViewSettingsViewModel() {
        minHeight = new SimpleIntegerProperty();
        maxHeight = new SimpleIntegerProperty();
        height = new SimpleIntegerProperty();
        active = new SimpleBooleanProperty();
        opacityPercentage = new SimpleIntegerProperty();
        slideInSeconds = new SimpleFloatProperty();
        slideOutSeconds = new SimpleFloatProperty();
    }

    public void init(MiniViewSettings settings) {
        minHeight.set(settings.minHeight());
        maxHeight.set(settings.maxHeight());
        height.set(settings.height());
        active.set(settings.active());
        opacityPercentage.set(settings.opacityPercentage());
        slideInSeconds.set(settings.slideInSeconds());
        slideOutSeconds.set(settings.slideOutSeconds());
    }

    public int getMinHeightProperty() {
        return minHeight.get();
    }

    public IntegerProperty minHeightProperty() {
        return minHeight;
    }

    public int getMaxHeightProperty() {
        return maxHeight.get();
    }

    public IntegerProperty maxHeightProperty() {
        return maxHeight;
    }

    public int getHeightProperty() {
        return height.get();
    }

    public IntegerProperty heightProperty() {
        return height;
    }

    public boolean isActiveProperty() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public int getOpacityPercentageProperty() {
        return opacityPercentage.get();
    }

    public IntegerProperty opacityPercentageProperty() {
        return opacityPercentage;
    }

    public float getSlideInSecondsProperty() {
        return slideInSeconds.get();
    }

    public FloatProperty slideInSecondsProperty() {
        return slideInSeconds;
    }

    public float getSlideOutSecondsProperty() {
        return slideOutSeconds.get();
    }

    public FloatProperty slideOutSecondsProperty() {
        return slideOutSeconds;
    }
}
