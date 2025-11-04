/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
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

    protected final LocalDateTime creationTime;
    protected final GameEventType type;
    protected final Game game;
    protected final Vector2i tile;

    public GameEvent(Game game, GameEventType type, Vector2i tile) {
        this.creationTime = LocalDateTime.now();
        this.game = game;
        this.type = type;
        this.tile = tile;
    }

    public GameEvent(Game game, GameEventType type) {
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

    public GameEventType type() {
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