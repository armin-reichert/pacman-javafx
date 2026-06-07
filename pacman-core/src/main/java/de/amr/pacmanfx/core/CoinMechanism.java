/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

public class CoinMechanism {

    public static final CoinMechanism OUT_OF_SERVICE = new CoinMechanism(0) {

        @Override
        public void setNumCoins(int n) {}

        @Override
        public void insertCoin() {}

        @Override
        public void consumeCoin() {}
    };

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    private final int maxCoins;

    public CoinMechanism() {
        this(99);
    }

    public CoinMechanism(int maxCoins) {
        if (maxCoins < 0) {
            throw new IllegalArgumentException("maxCoins < 0");
        }
        this.maxCoins = maxCoins;
    }

    public IntegerProperty numCoinsProperty() {
        return numCoins;
    }

    public int numCoins() {
        return numCoinsProperty().get();
    }

    public int maxCoins() {
        return maxCoins;
    }

    public boolean isFull() {
        return numCoins() == maxCoins;
    }

    public boolean isEmpty() {
        return numCoins() == 0;
    }

    public void setNumCoins(int n) {
        if (n >= 0 && n <= maxCoins) {
            numCoinsProperty().set(n);
        } else {
            Logger.error("Cannot set number of coins to {}", n);
        }
    }

    public void insertCoin() {
        if (numCoins() + 1 <= maxCoins) {
            setNumCoins(numCoins() + 1);
        }
    }

    public void consumeCoin() {
        if (numCoins() > 0) {
            setNumCoins(numCoins() - 1);
        }
    }
}
