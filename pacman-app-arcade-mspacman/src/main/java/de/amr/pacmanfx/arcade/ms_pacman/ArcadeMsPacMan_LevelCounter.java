/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.LevelCounter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;

public class ArcadeMsPacMan_LevelCounter implements LevelCounter {

    public static final int MAX_LEVEL_COUNTER_SYMBOLS = 7;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final List<Byte> symbols = new ArrayList<>();

    public ArcadeMsPacMan_LevelCounter() {
    }

    @Override
    public void clear() {
        symbols.clear();
    }

    @Override
    public void update(int levelNumber, byte symbol) {
        if (levelNumber == 1) {
            symbols.clear();
        }
        if (levelNumber < MAX_LEVEL_COUNTER_SYMBOLS && enabled()) {
            symbols.add(symbol);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @Override
    public boolean enabled() {
        return this.enabled.get();
    }

    @Override
    public List<Byte> symbols() {
        return symbols;
    }
}