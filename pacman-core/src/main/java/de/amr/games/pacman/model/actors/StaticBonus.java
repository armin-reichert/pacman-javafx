/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;


/**
 * Bonus that appears for some time at a fixed position before it gets eaten or vanishes.
 *
 * @author Armin Reichert
 */
public class StaticBonus extends Entity implements Bonus {

    private final byte symbol;
    private final int points;
    private long countdown;
    private byte state;

    public StaticBonus(byte symbol, int points) {
        this.symbol = symbol;
        this.points = points;
        this.countdown = 0;
        this.state = STATE_INACTIVE;
    }

    @Override
    public StaticBonus entity() {
        return this;
    }

    @Override
    public String toString() {
        return "StaticBonus{" +
            "symbol=" + symbol +
            ", points=" + points +
            ", countdown=" + countdown +
            ", state=" + stateName() +
            '}';
    }

    @Override
    public byte state() {
        return state;
    }

    @Override
    public byte symbol() {
        return symbol;
    }

    @Override
    public int points() {
        return points;
    }

    @Override
    public void setInactive() {
        countdown = 0;
        visible = false;
        state = STATE_INACTIVE;
        Logger.trace("Bonus inactive: {}", this);
    }

    @Override
    public void setEdible(long ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Bonus edible time must be larger than zero");
        }
        countdown = ticks;
        visible = true;
        state = STATE_EDIBLE;
        Logger.trace("Bonus edible: {}", this);
    }

    @Override
    public void setEaten(long ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Bonus edible time must be larger than zero");
        }
        countdown = ticks;
        state = STATE_EATEN;
        Logger.trace("Bonus eaten: {}", this);
    }

    @Override
    public void update(GameModel game) {
        switch (state) {
            case STATE_INACTIVE -> {}
            case STATE_EDIBLE, STATE_EATEN -> {
                if (countdown == 0) {
                    setInactive();
                    game.publishGameEvent(GameEventType.BONUS_EXPIRED, tile());
                } else if (countdown != TickTimer.INDEFINITE) {
                    --countdown;
                }
            }
            default -> throw new IllegalStateException("Unknown bonus state: " + state);
        }
    }
}