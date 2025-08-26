/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.LevelCounter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TengenMsPacMan_LevelCounter implements LevelCounter {

    public static final byte LEVEL_COUNTER_MAX_SIZE = 7;

    private final BooleanProperty enabledPy = new SimpleBooleanProperty(true);
    private final List<Byte> symbols = new ArrayList<>();

    public BooleanProperty enabledProperty() {
        return enabledPy;
    }

    void setStartLevel(int startLevelNumber) {
        if (startLevelNumber > 1) {
            symbols.clear();
            for (byte number = 1; number < Math.min(startLevelNumber, LEVEL_COUNTER_MAX_SIZE); ++number) {
                symbols.add((byte) (number - 1));
            }
        } else {
            symbols.clear();
        }
    }

    @Override
    public List<Byte> levelCounterSymbols() {
        return Collections.unmodifiableList(symbols);
    }

    @Override
    public void clearLevelCounter() {
        symbols.clear();
    }

    @Override
    public void updateLevelCounter(int levelNumber, byte symbol) {
        if (levelNumber == 1) {
            clearLevelCounter();
        }
        if (isLevelCounterEnabled()) {
            symbols.add(symbol);
            if (symbols.size() > LEVEL_COUNTER_MAX_SIZE) {
                symbols.removeFirst();
            }
        }
    }

    @Override
    public void setLevelCounterEnabled(boolean enabled) {
        enabledProperty().set(enabled);
    }

    @Override
    public boolean isLevelCounterEnabled() {
        return enabledProperty().get();
    }

    // Additional features

    private int displayedLevelNumber;

    /**
     * @param number level number or 0 if no level number shall be displayed
     */
    public void setDisplayedLevelNumber(int number) {
        displayedLevelNumber = number;
    }

    public int displayedLevelNumber() {
        return displayedLevelNumber;
    }
}
