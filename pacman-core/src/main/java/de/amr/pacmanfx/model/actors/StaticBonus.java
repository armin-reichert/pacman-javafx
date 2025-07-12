/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameModel;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.theGameContext;

/**
 * Bonus that appears for some time at a fixed position before it gets eaten or vanishes.
 */
public class StaticBonus extends Actor implements Bonus {

    private final byte symbol;
    private final int points;
    private long countdown;
    private byte state;

    public StaticBonus(GameContext gameContext, byte symbol, int points) {
        super(gameContext);
        this.symbol = symbol;
        this.points = points;
        this.countdown = 0;
        this.state = STATE_INACTIVE;
    }

    @Override
    public StaticBonus actor() {
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
        hide();
        state = STATE_INACTIVE;
        Logger.trace("Bonus inactive: {}", this);
    }

    @Override
    public void setEdibleTicks(long ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Bonus edible time must be larger than zero");
        }
        countdown = ticks;
        show();
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
                    gameContext.theGameEventManager().publishEvent(game, GameEventType.BONUS_EXPIRED, tile());
                } else if (countdown != TickTimer.INDEFINITE) {
                    --countdown;
                }
            }
            default -> throw new IllegalStateException("Unknown bonus state: " + state);
        }
    }
}