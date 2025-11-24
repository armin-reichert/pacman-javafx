/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;

import java.time.LocalDateTime;

/**
 * Game events are sent by the model/controller layer and handled by the view layer and serve to decouple these layers.
 */
public class GameEvent {

    public enum Type {
        BONUS_ACTIVATED,
        BONUS_EATEN,
        BONUS_EXPIRED,
        CREDIT_ADDED,
        SPECIAL_SCORE_REACHED,
        GAME_CONTINUED,
        GAME_STARTED,
        GAME_STATE_CHANGED,
        GAME_VARIANT_CHANGED,
        GHOST_EATEN,
        GHOST_ENTERS_HOUSE,
        GHOST_STARTS_RETURNING_HOME,
        HUNTING_PHASE_STARTED,
        INTERMISSION_STARTED,
        LEVEL_CREATED,
        LEVEL_STARTED,
        PAC_DEAD,
        PAC_DYING,
        PAC_FOUND_FOOD,
        PAC_GETS_POWER,
        PAC_LOST_POWER,
        PAC_STARTS_LOSING_POWER,
        STOP_ALL_SOUNDS,

        UNSPECIFIED_CHANGE
    }

    protected final LocalDateTime creationTime;
    protected final Type type;
    protected final Game game;
    protected final Vector2i tile;

    public GameEvent(Game game, Type type, Vector2i tile) {
        this.creationTime = LocalDateTime.now();
        this.game = game;
        this.type = type;
        this.tile = tile;
    }

    public GameEvent(Game game, Type type) {
        this(game, type, null);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("GameEvent[");
        sb.append(type);
        sb.append(", created=").append(creationTime);
        sb.append(", game=").append(game);
        if (tile != null) {
            sb.append(", tile=").append(tile);
        }
        sb.append("]");
        return sb.toString();
    }

    public Type type() {
        return type;
    }

    public LocalDateTime creationTime() {
        return creationTime;
    }

    /**
     * @return the game model associated with this event
     */
    public Game game() {
        return game;
    }

    /**
     * @return the tile or {@code null} associated with this event
     */
    public Vector2i tile() {
        return tile;
    }
}