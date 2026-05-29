/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

public class CoinMechanism {

    public static final CoinMechanism MISSING = new CoinMechanism() {

        private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

        @Override
        public IntegerProperty numCoinsProperty() {
            return numCoins;
        }

        @Override
        public int numCoins() {
            return 0;
        }

        @Override
        public int maxCoins() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void setNumCoins(int n) {
            // no-op
        }

        @Override
        public void insertCoin() {
            // no-op
        }

        @Override
        public void consumeCoin() {
            // no-op
        }
    };

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    public IntegerProperty numCoinsProperty() {
        return numCoins;
    }

    public int numCoins() {
        return numCoinsProperty().get();
    }

    public int maxCoins() {
        return 99;
    }

    public boolean isFull() {
        return numCoins() == maxCoins();
    }
    public boolean isEmpty() {
        return numCoins() == 0;
    }

    public void setNumCoins(int n) {
        if (n >= 0 && n <= maxCoins()) {
            numCoinsProperty().set(n);
        } else {
            Logger.error("Cannot set number of coins to {}", n);
        }
    }

    public void insertCoin() {
        if (numCoins() + 1 <= maxCoins()) {
            setNumCoins(numCoins() + 1);
        }
    }

    public void consumeCoin() {
        if (numCoins() > 0) {
            setNumCoins(numCoins() - 1);
        }
    }
}
