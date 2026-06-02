/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.level.LevelCounter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TengenMsPacMan_LevelCounter implements LevelCounter {

    public static final byte LEVEL_COUNTER_MAX_SIZE = 7;

    private final BooleanProperty levelCounterEnabled = new SimpleBooleanProperty(true);
    private final List<Integer> symbolCodes = new ArrayList<>();

    public BooleanProperty levelCounterEnabledProperty() {
        return levelCounterEnabled;
    }

    @Override
    public List<Integer> symbolCodes() {
        return Collections.unmodifiableList(symbolCodes);
    }

    @Override
    public void clear() {
        symbolCodes.clear();
    }

    @Override
    public void update(int levelNumber, int symbolCode) {
        if (levelNumber == 1) {
            clear();
        }
        if (isEnabled()) {
            symbolCodes.add(symbolCode);
            if (symbolCodes.size() > LEVEL_COUNTER_MAX_SIZE) {
                symbolCodes.removeFirst();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        levelCounterEnabledProperty().set(enabled);
    }

    @Override
    public boolean isEnabled() {
        return levelCounterEnabledProperty().get();
    }
}
