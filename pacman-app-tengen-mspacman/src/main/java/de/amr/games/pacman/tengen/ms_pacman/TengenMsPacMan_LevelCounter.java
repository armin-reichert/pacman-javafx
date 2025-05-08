/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.LevelCounter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
            for (byte number = 1; number <= Math.min(startLevelNumber, LEVEL_COUNTER_MAX_SIZE); ++number) {
                symbols.add((byte) (number - 1));
            }
        } else {
            symbols.clear();
        }
    }

    @Override
    public Stream<Byte> symbols() {
        return symbols.stream();
    }

    @Override
    public void clear() {
        symbols.clear();
    }

    @Override
    public void update(int levelNumber, byte symbol) {
        if (levelNumber == 1) {
            clear();
        }
        if (isEnabled()) {
            symbols.add(symbol);
            if (symbols.size() > LEVEL_COUNTER_MAX_SIZE) {
                symbols.removeFirst();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        enabledProperty().set(enabled);
    }

    @Override
    public boolean isEnabled() {
        return enabledProperty().get();
    }
}
