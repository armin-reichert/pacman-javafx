/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public interface CoinMechanism {

    CoinMechanism MISSING = new CoinMechanism() {

        private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

        public IntegerProperty numCoinsProperty() {
            return numCoins ;
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
        }

        @Override
        public void insertCoin() {
        }

        @Override
        public void consumeCoin() {
        }
    };

    IntegerProperty numCoinsProperty();

    int numCoins();

    int maxCoins();

    boolean isEmpty();

    void setNumCoins(int n);

    void insertCoin();

    void consumeCoin();
}