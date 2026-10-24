/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.gamestate.FrameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSession {

    public static boolean DEFAULT_BOOLEAN_VALUE = false;
    public static int     DEFAULT_INT_VALUE = 0;
    public static long    DEFAULT_LONG_VALUE = 0;

    private FrameState frameState;

    private GameLevel level;

    private final int initialNumLives;

    private final HUD hud;

    private final GameCheats cheats;

    private final Map<Named, Object> values = new HashMap<>();

    public GameSession(String variantName, GameCheats cheats, int initialLifeCount) {
        requireNonNull(variantName);
        requireNonNull(cheats);

        this.cheats = requireNonNull(cheats);
        this.initialNumLives = initialLifeCount;

        hud = new HUD(variantName);

        setValue(SessionKey.NUM_LIVES, initialLifeCount);
        frameState = new FrameState(0);

        cheats.cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                hud.highScore().data().setEnabled(false);
            }
        });
    }

    public int initialNumLives() {
        return initialNumLives;
    }

    public void setLevel(GameLevel level) {
        this.level = level;
    }

    public Optional<GameLevel> optLevel() {
        return Optional.ofNullable(level);
    }

    /**
     * @return the current game level. If there is no current game level, throws an exception!
     */
    public GameLevel level() {
        if (level != null) {
            return level;
        }
        throw new IllegalStateException("No game level exists at this time");
    }

    public HUD hud() {
        return hud;
    }

    public GameCheats cheats() {
        return cheats;
    }

    public <T> T value(Named key, Class<T> type) {
        requireNonNull(key);
        final Object value = values.get(key);
        if (value != null) {
            return type.cast(value);
        }
        return null;
    }

    public boolean booleanValue(Named key) {
        final Boolean value = value(key, Boolean.class);
        return value != null ? value : DEFAULT_BOOLEAN_VALUE;
    }

    public int intValue(Named key) {
        final Integer value = value(key, Integer.class);
        return value != null ? value : DEFAULT_INT_VALUE;
    }

    public long longValue(Named key) {
        final Long value = value(key, Long.class);
        return value != null ? value : DEFAULT_LONG_VALUE;
    }

    public void setValue(Named key, Object value) {
        requireNonNull(key);
        values.put(key, value);
    }

    public void clearValue(Named key) {
        requireNonNull(key);
        values.remove(key);
    }

    public void setFrameState(FrameState frameState) {
        this.frameState = requireNonNull(frameState);
    }

    public FrameState frameState() {
        return frameState;
    }
}
