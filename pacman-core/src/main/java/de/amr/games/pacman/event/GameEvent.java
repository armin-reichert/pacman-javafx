/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public class GameEvent {

    public final GameEventType type;
    public final GameModel game;
    public final Vector2i tile;

    public GameEvent(GameEventType type, GameModel game, Vector2i tile) {
        Globals.assertNotNull(type);
        Globals.assertNotNull(game);
        this.type = type;
        this.game = game;
        this.tile = tile;
    }

    public GameEvent(GameEventType type, GameModel game) {
        this(type, game, null);
    }

    @Override
    public String toString() {
        return "GameEvent{" +
            "type=" + type +
            ", tile=" + tile +
            '}';
    }

    public Optional<Vector2i> tile() {
        return Optional.ofNullable(tile);
    }
}