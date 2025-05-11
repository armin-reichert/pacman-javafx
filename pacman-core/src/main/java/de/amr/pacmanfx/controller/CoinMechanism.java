/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import org.tinylog.Logger;

public class CoinMechanism {

    public static byte MAX_COINS = 99;

    private byte numCoins;

    public int numCoins() { return numCoins; }

    public boolean isEmpty() { return numCoins == 0; }

    public void setNumCoins(int numCoins) {
        if (numCoins >= 0 && numCoins <= MAX_COINS) {
            this.numCoins = (byte) numCoins;
        } else {
            Logger.error("Cannot set number of coins to illegal value {}", numCoins);
        }
    }

    public void insertCoin() {
        numCoins++;
    }

    public void consumeCoin() {
        if (numCoins > 0) {
            --numCoins;
        }
    }
}