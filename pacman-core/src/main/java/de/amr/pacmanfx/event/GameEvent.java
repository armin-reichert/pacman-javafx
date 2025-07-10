/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameModel;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class GameEvent {

    private static final int DEFAULT_PAYLOAD_SIZE = 5;

    public static final String PAYLOAD_KEY_CREATED = "_created";
    public static final String PAYLOAD_KEY_GAME    = "_game";
    public static final String PAYLOAD_KEY_TILE    = "_tile";

    private final GameEventType type;
    private final Map<String, Object> payload = new HashMap<>(DEFAULT_PAYLOAD_SIZE);

    public GameEvent(GameModel game, GameEventType type, Vector2i tile) {
        this.type = requireNonNull(type);
        setPayload(PAYLOAD_KEY_CREATED, LocalDateTime.now());
        setPayload(PAYLOAD_KEY_GAME, requireNonNull(game));
        if (tile != null) {
            setPayload(PAYLOAD_KEY_TILE, tile);
        }
    }

    public GameEvent(GameModel game, GameEventType type) {
        this(game, type, null);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("GameEvent[");
        sb.append(type);
        payload.keySet().stream().sorted()
            .filter(key -> !PAYLOAD_KEY_CREATED.equals(key))
            .filter(key -> !PAYLOAD_KEY_GAME.equals(key))
            .filter(key -> !PAYLOAD_KEY_TILE.equals(key))
            .forEach(key -> sb.append(", ").append(key).append("=").append(payload.get(key)));
        if (tile() != null) {
            sb.append(", tile=").append(tile());
        }
        sb.append(", created=").append(creationTime());
        sb.append(", game=").append(game());
        sb.append("]");
        return sb.toString();
    }

    public GameEventType type() {
        return type;
    }

    public LocalDateTime creationTime() { return payload(PAYLOAD_KEY_CREATED); }

    /**
     * @return the game model associated with this event
     */
    public GameModel game() { return payload(PAYLOAD_KEY_GAME); }

    /**
     * @return the tile or {@code null} associated with this event
     */
    public Vector2i tile() { return payload(PAYLOAD_KEY_TILE); }

    public void setPayload(String key, Object value) { payload.put(key, value); }

    @SuppressWarnings("unchecked")
    public <T> T payload(String key) { return (T) payload.get(key); }
}