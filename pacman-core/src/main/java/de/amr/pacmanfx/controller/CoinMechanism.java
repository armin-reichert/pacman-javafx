/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

public class CoinMechanism {

    public static byte MAX_COINS = 99;

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    public IntegerProperty numCoinsProperty() { return numCoins; }

    public int numCoins() { return numCoins.get(); }

    public boolean isEmpty() { return numCoins() == 0; }

    public void setNumCoins(int n) {
        if (n >= 0 && n <= MAX_COINS) {
            numCoins.set(n);
        } else {
            Logger.error("Cannot set number of coins to {}", n);
        }
    }

    public void insertCoin() {
        setNumCoins(numCoins() + 1);
    }

    public void consumeCoin() {
        if (numCoins() > 0) {
            setNumCoins(numCoins() - 1);
        }
    }
}