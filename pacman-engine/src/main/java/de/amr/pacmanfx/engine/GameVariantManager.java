/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine;

import de.amr.pacmanfx.core.GameVariantRuntime;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public interface GameVariantManager {

    void registerGameVariant(EngineContext engine, String variantName);

    GameVariantRuntime runtimeByName(String variantName);

    GameVariantRuntime currentRuntime();

    void selectVariant(EngineContext engine, String variantName);

    StringProperty selectedVariantNameProperty();

    String currentVariantName();

    boolean isVariantRegistered(String variantName);

    void addVariantListener(ChangeListener<String> listener);
}
