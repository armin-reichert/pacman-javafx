/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public interface GameVariantManager {

    void registerVariantConfig(String variantName);

    GameVariantConfig variantConfigByName(String variantName);

    GameVariantConfig currentVariantConfig();

    void selectVariant(String variantName);

    StringProperty selectedVariantNameProperty();

    String currentVariantName();

    boolean isVariantRegistered(String variantName);

    void addVariantListener(ChangeListener<String> listener);
}
