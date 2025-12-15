/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.model.BaseHUD;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.HUD;
import javafx.beans.property.IntegerProperty;

import java.util.Objects;

public class Arcade_HUD extends BaseHUD {

    private final CoinMechanism coinMechanism;

    public Arcade_HUD(CoinMechanism coinMechanism) {
        this.coinMechanism = Objects.requireNonNull(coinMechanism);
    }

    public IntegerProperty numCoinsProperty() {
        return coinMechanism.numCoinsProperty();
    }

    public int numCoins() {
        return coinMechanism.numCoins();
    }

    private boolean creditVisible;

    public boolean isCreditVisible() { return creditVisible; }

    public HUD credit(boolean visible) {
        creditVisible = visible;
        return this;
    }

    @Override
    public HUD all(boolean visible) {
        return credit(visible).score(visible).levelCounter(visible).livesCounter(visible);
    }
}
