/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine;

import de.amr.pacmanfx.core.GameVariantRuntime;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public interface GameVariantManager {

    void registerVariant(EngineCoreContext engine, String variantName);

    GameVariantRuntime variantRuntime(String variantName);

    GameVariantRuntime currentRuntime();

    void selectVariant(EngineCoreContext engine, String variantName);

    StringProperty currentVariantIDProperty();

    String currentVariantID();

    boolean variantExists(String variantName);

    void addVariantChangeListener(ChangeListener<String> listener);
}
