/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

public class CoinControl {

    private int maxCoins = 99;
    private int numCoins = 0;

    public int maxCoins() {
        return maxCoins;
    }

    public void setMaxCoins(int maxCoins) {
        this.maxCoins = maxCoins;
    }

    /**
     * @return number of coins inserted.
     */
    public int credit() {
        return numCoins;
    }

    public boolean hasCredit() {
        return numCoins > 0;
    }

    public void setNumCoins(int numCoins) {
        if (numCoins >= 0 && numCoins <= maxCoins) {
            this.numCoins = numCoins;
        }
    }

    public boolean insertCoin() {
        if (numCoins < maxCoins) {
            ++numCoins;
            return true;
        }
        return false;
    }

    public void consumeCoin() {
        if (numCoins > 0) {
            --numCoins;
        }
    }
}