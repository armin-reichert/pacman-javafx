/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import javafx.beans.property.IntegerProperty;

public interface CoinMechanism {

    byte MAX_COINS = 99;

    IntegerProperty numCoinsProperty();

    int numCoins();

    boolean noCoin();

    void setNumCoins(int n);

    void insertCoin();

    void consumeCoin();
}