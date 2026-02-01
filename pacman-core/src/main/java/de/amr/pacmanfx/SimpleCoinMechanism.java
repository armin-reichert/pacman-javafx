/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx;

import de.amr.pacmanfx.model.CoinMechanism;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

public class SimpleCoinMechanism implements CoinMechanism {

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    @Override
    public IntegerProperty numCoinsProperty() { return numCoins; }

    public int numCoins() {
        return numCoinsProperty().get();
    }

    @Override
    public int maxCoins() {
        return 99;
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
        if (numCoins() +1 <= maxCoins()) {
            setNumCoins(numCoins() + 1);
        }
    }

    public void consumeCoin() {
        if (numCoins() > 0) {
            setNumCoins(numCoins() - 1);
        }
    }
}
