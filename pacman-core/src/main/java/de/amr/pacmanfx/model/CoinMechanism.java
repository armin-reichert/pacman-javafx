/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents a coin-handling mechanism used by the game to track and manipulate
 * the number of inserted coins. Implementations may represent real coin slots,
 * virtual credit counters, or placeholder mechanisms.
 * <p>
 * The mechanism exposes both an integer property for JavaFX binding and
 * convenience methods for querying and modifying the coin count.
 * </p>
 */
public interface CoinMechanism {

    /**
     * A placeholder {@code CoinMechanism} implementation used when no real
     * mechanism is available. This instance always reports zero coins, cannot
     * accept or consume coins, and its internal property never changes.
     * <p>
     * Useful as a safe default to avoid {@code null} checks.
     * </p>
     */
    CoinMechanism MISSING = new CoinMechanism() {

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

    /**
     * Returns the JavaFX property representing the current number of coins.
     * <p>
     * This allows UI components to observe and react to changes in the coin count.
     * </p>
     *
     * @return the coin count property
     */
    IntegerProperty numCoinsProperty();

    /**
     * Returns the current number of coins stored in the mechanism.
     *
     * @return the number of coins
     */
    int numCoins();

    /**
     * Returns the maximum number of coins the mechanism can hold.
     * Implementations may return {@code Integer.MAX_VALUE} if unbounded.
     *
     * @return the maximum capacity
     */
    int maxCoins();

    /**
     * Indicates whether the mechanism currently contains zero coins.
     *
     * @return {@code true} if no coins are stored
     */
    boolean isEmpty();

    /**
     * Sets the number of coins stored in the mechanism.
     * Implementations may clamp the value to their maximum capacity.
     *
     * @param n the new coin count
     */
    void setNumCoins(int n);

    /**
     * Inserts a single coin into the mechanism.
     * Implementations may ignore the request if the mechanism is full.
     */
    void insertCoin();

    /**
     * Consumes (removes) a single coin from the mechanism.
     * Implementations may ignore the request if the mechanism is empty.
     */
    void consumeCoin();
}
