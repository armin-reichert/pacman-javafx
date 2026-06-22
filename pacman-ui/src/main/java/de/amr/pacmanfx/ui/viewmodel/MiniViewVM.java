/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;

public class MiniViewVM {

    public final IntegerProperty heightProperty;
    public final BooleanProperty activeProperty;
    public final IntegerProperty opacityPercentageProperty;

    public MiniViewVM(IntegerProperty heightProperty, BooleanProperty activeProperty, IntegerProperty opacityPercentageProperty) {
        this.heightProperty = heightProperty;
        this.activeProperty = activeProperty;
        this.opacityPercentageProperty = opacityPercentageProperty;
    }
}
