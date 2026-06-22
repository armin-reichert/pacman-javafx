/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;

public record MiniViewSettingsProperties(
    IntegerProperty heightProperty,
    BooleanProperty activeProperty,
    IntegerProperty opacityPercentageProperty)
{}
