/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.model;

import de.amr.pacmanfx.ui.settings.ui.MiniViewSettings;
import javafx.beans.property.*;

public class MiniViewModel {

    public final IntegerProperty heightProperty;
    public final BooleanProperty activeProperty;
    public final IntegerProperty opacityPercentageProperty;
    public final FloatProperty slideInSecondsProperty;
    public final FloatProperty slideOutSecondsProperty;

    public MiniViewModel() {
        this.heightProperty = new SimpleIntegerProperty();
        this.activeProperty = new SimpleBooleanProperty();
        this.opacityPercentageProperty = new SimpleIntegerProperty();
        this.slideInSecondsProperty = new SimpleFloatProperty();
        this.slideOutSecondsProperty = new SimpleFloatProperty();
    }

    public void init(MiniViewSettings settings) {
        heightProperty.set(settings.height());
        activeProperty.set(settings.active());
        opacityPercentageProperty.set(settings.opacityPercentage());
        slideInSecondsProperty.set(settings.slideInSeconds());
        slideOutSecondsProperty.set(settings.slideOutSeconds());
    }
}
