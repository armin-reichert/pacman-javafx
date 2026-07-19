/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public interface GameVariantManager {

    StringProperty variantNameProperty();

    GameVariant currentVariant();

    void addVariantNameListener(ChangeListener<String> listener);

    void selectVariant(String gameVariantName);

    String currentVariantName();

    GameVariant gameVariantByName(String gameVariantName);

    boolean isVariantRegistered(String variantName);
}
