/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.config;

import de.amr.pacmanfx.engine.runtime.GameVariantRuntime;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public interface GameVariantManager {

    void registerVariantConfig(String variantName);

    GameVariantRuntime variantRuntimeByName(String variantName);

    GameVariantRuntime currentRuntime();

    void selectVariant(String variantName);

    StringProperty selectedVariantNameProperty();

    String currentVariantName();

    boolean isVariantRegistered(String variantName);

    void addVariantListener(ChangeListener<String> listener);
}
