/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

public interface CoinMechanism {

    // Null object
    CoinMechanism MISSING = new CoinMechanism() {

        private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

        @Override
        public IntegerProperty numCoinsProperty() {
            return numCoins ;
        }
    };

    byte MAX_COINS = 99;

    IntegerProperty numCoinsProperty();

    default int numCoins() {
        return numCoinsProperty().get();
    }

    default boolean isEmpty() {
        return numCoins() == 0;
    }

    default void setNumCoins(int n) {
        if (n >= 0 && n <= CoinMechanism.MAX_COINS) {
            numCoinsProperty().set(n);
        } else {
            Logger.error("Cannot set number of coins to {}", n);
        }
    }

    default void insertCoin() {
        if (numCoins() +1 <= MAX_COINS) {
            setNumCoins(numCoins() + 1);
        }
    }

    default void consumeCoin() {
        if (numCoins() > 0) {
            setNumCoins(numCoins() - 1);
        }
    }
}