/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.vm;

import de.amr.pacmanfx.ui.settings.ui.MiniViewSettings;
import javafx.beans.property.*;

public class MiniViewSettingsVM {

    public final IntegerProperty minHeightProperty;
    public final IntegerProperty maxHeightProperty;
    public final IntegerProperty heightProperty;
    public final BooleanProperty activeProperty;
    public final IntegerProperty opacityPercentageProperty;
    public final FloatProperty slideInSecondsProperty;
    public final FloatProperty slideOutSecondsProperty;

    public MiniViewSettingsVM() {
        this.minHeightProperty = new SimpleIntegerProperty();
        this.maxHeightProperty = new SimpleIntegerProperty();
        this.heightProperty = new SimpleIntegerProperty();
        this.activeProperty = new SimpleBooleanProperty();
        this.opacityPercentageProperty = new SimpleIntegerProperty();
        this.slideInSecondsProperty = new SimpleFloatProperty();
        this.slideOutSecondsProperty = new SimpleFloatProperty();
    }

    public void init(MiniViewSettings settings) {
        minHeightProperty.set(settings.minHeight());
        maxHeightProperty.set(settings.maxHeight());
        heightProperty.set(settings.height());
        activeProperty.set(settings.active());
        opacityPercentageProperty.set(settings.opacityPercentage());
        slideInSecondsProperty.set(settings.slideInSeconds());
        slideOutSecondsProperty.set(settings.slideOutSeconds());
    }
}
