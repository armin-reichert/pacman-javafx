/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public interface GameVariantManager {

    void registerGameVariant(String variantName);

    GameVariant currentGameVariant();

    void addVariantNameListener(ChangeListener<String> listener);

    void selectVariant(String variantName);

    StringProperty selectedVariantNameProperty();

    String currentVariantName();

    GameVariant gameVariantByName(String variantName);

    boolean isVariantRegistered(String variantName);
}
