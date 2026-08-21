/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

public class CoinMechanism {

    private int numCoins;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    private final int maxCoins;

    public CoinMechanism(int maxCoins) {
        if (maxCoins < 0) {
            throw new IllegalArgumentException("maxCoins < 0");
        }
        this.maxCoins = maxCoins;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        enabledProperty().set(enabled);
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void setNumCoins(int n) {
        if (n >= 0 && n <= maxCoins) {
            numCoins = n;
        } else {
            Logger.error("Cannot set number of coins to {}", n);
        }
    }

    public int numCoins() {
        return numCoins;
    }

    public int maxCoins() {
        return maxCoins;
    }

    public boolean isFull() {
        return numCoins == maxCoins;
    }

    public boolean isEmpty() {
        return numCoins == 0;
    }

    public void insertCoin() {
        if (numCoins + 1 <= maxCoins) {
            ++numCoins;
        }
    }

    public void consumeCoin() {
        if (numCoins > 0) {
            --numCoins;
        }
    }
}
