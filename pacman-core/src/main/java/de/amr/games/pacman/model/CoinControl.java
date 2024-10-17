/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

public class CoinControl {

    private int numCoins = 0;

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
        if (numCoins >= 0 && numCoins <= GameModel.MAX_CREDIT) {
            this.numCoins = numCoins;
        }
    }

    public boolean insertCoin() {
        if (numCoins < GameModel.MAX_CREDIT) {
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