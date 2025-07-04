/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.model.LevelCounter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArcadePacMan_LevelCounter implements LevelCounter {

    public static final byte LEVEL_COUNTER_MAX_SIZE = 7;

    private final BooleanProperty enabledPy = new SimpleBooleanProperty(true);
    private final List<Byte> symbols = new ArrayList<>();

    public BooleanProperty enabledProperty() {
        return enabledPy;
    }

    @Override
    public List<Byte> symbols() {
        return Collections.unmodifiableList(symbols);
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
