/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.model;

import de.amr.pacmanfx.ui.config.ui.MiniViewSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MiniViewModel {

    public final IntegerProperty heightProperty;
    public final BooleanProperty activeProperty;
    public final IntegerProperty opacityPercentageProperty;

    public MiniViewModel() {
        this.heightProperty = new SimpleIntegerProperty();
        this.activeProperty = new SimpleBooleanProperty();
        this.opacityPercentageProperty = new SimpleIntegerProperty();
    }

    public void init(MiniViewSettings settings) {
        heightProperty.set(settings.height());
        activeProperty.set(settings.active());
        opacityPercentageProperty.set(settings.opacityPercentage());
    }
}
