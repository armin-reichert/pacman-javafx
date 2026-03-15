/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.LevelCounter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TengenMsPacMan_LevelCounter implements LevelCounter {

    public static final byte LEVEL_COUNTER_MAX_SIZE = 7;

    private final BooleanProperty levelCounterEnabled = new SimpleBooleanProperty(true);
    private final List<Byte> levelCounterSymbols = new ArrayList<>();

    public BooleanProperty levelCounterEnabledProperty() {
        return levelCounterEnabled;
    }

    @Override
    public List<Byte> symbols() {
        return Collections.unmodifiableList(levelCounterSymbols);
    }

    @Override
    public void clear() {
        levelCounterSymbols.clear();
    }

    @Override
    public void update(int levelNumber, byte symbol) {
        if (levelNumber == 1) {
            clear();
        }
        if (isEnabled()) {
            levelCounterSymbols.add(symbol);
            if (levelCounterSymbols.size() > LEVEL_COUNTER_MAX_SIZE) {
                levelCounterSymbols.removeFirst();
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
