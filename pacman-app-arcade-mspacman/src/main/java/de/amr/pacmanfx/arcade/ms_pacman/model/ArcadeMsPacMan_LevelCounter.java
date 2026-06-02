/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.model.level.LevelCounter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;

public class ArcadeMsPacMan_LevelCounter implements LevelCounter {

    public static final int MAX_LEVEL_COUNTER_SYMBOLS = 7;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final List<Integer> symbolCodes = new ArrayList<>();

    @Override
    public void clear() {
        symbolCodes.clear();
    }

    @Override
    public void update(int levelNumber, int symbolCode) {
        if (levelNumber == 1) {
            symbolCodes.clear();
        }
        if (levelNumber < MAX_LEVEL_COUNTER_SYMBOLS && isEnabled()) {
            symbolCodes.add(symbolCode);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public List<Integer> symbolCodes() {
        return symbolCodes;
    }
}
