/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class GameEvent {

    public final GameEventType type;
    public final GameModel game;
    private Map<String, Object> payloadMap;

    public void setPayload(String key, Object value) {
        payloadMap().put(key, requireNonNull(value));
    }

    @SuppressWarnings("unchecked")
    public <T> T payload(String key) {
        return (T) payloadMap().get(key);
    }

    private Map<String, Object> payloadMap() {
        if (payloadMap == null) {
            payloadMap = new HashMap<>(3);
        }
        return payloadMap;
    }

    public GameEvent(GameModel game, GameEventType type, Vector2i tile) {
        this.type = requireNonNull(type);
        this.game = requireNonNull(game);
        setPayload("tile", tile);
    }

    public GameEvent(GameModel game, GameEventType type) {
        this.type = requireNonNull(type);
        this.game = requireNonNull(game);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("GameEvent[");
        sb.append(type);
        if (payloadMap != null) {
            sb.append(" [");
            payloadMap.forEach((key, value) -> sb.append(key).append("=").append(value).append(","));
            sb.append("]");
        }
        sb.append(" game=").append(game);
        sb.append("]");
        return sb.toString();
    }

    public Optional<Vector2i> tile() {
        return Optional.ofNullable(payload("tile"));
    }
}