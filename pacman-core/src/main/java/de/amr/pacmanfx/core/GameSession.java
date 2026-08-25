/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;
import de.amr.pacmanfx.core.gamestate.FrameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.HUDState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSession {

    public interface GameSessionValueKey {}

    private FrameState frameState;

    private GameLevel level;

    private final QuerySet<GameEntity> hudEntities = new QuerySet<>();

    private boolean attractMode;

    private boolean gameRunning;

    private int numLives;

    private final HUDState hud;

    private final GameCheats cheats;

    private final Map<GameSessionValueKey, Object> values = new HashMap<>();

    private int gameOverStateTicks;

    private long levelStartTimeMillis;

    private boolean cutScenesEnabled;

    public GameSession(String variantName, GameCheats cheats, int numLives) {
        requireNonNull(variantName);
        requireNonNull(cheats);

        this.numLives = Validations.requireNonNegativeInt(numLives);
        this.cheats = cheats;
        this.hud = new HUDState();

        newFrameState(0);

        hudEntities.add(new LevelCounter());
        hudEntities.add(new LivesCounter());
        hudEntities.add(new MessageView());
        hudEntities.add(new Score(Score.Type.GAME_SCORE));
        hudEntities.add(ScoreSystem.createHighScore(ScoreSystem.highScoreFile(variantName)));

        cheats.cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                highScore().data().setEnabled(false);
            }
        });
    }

    public int numLives() {
        return numLives;
    }

    public void setNumLives(int numLives) {
        this.numLives = numLives;
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

    public QuerySet<GameEntity> hudEntities() {
        return hudEntities;
    }

    public HUDState hud() {
        return hud;
    }

    public GameCheats cheats() {
        return cheats;
    }

    public void setAttractMode(boolean attractMode) {
        this.attractMode = attractMode;
    }

    public boolean isAttractMode() {
        return attractMode;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
    }

    public boolean cutScenesEnabled() {
        return cutScenesEnabled;
    }

    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled = enabled;
    }

    // Easier access to score and high-score entities

    public Score score() {
        return hudEntities
            .selectWhere(Score.class, score -> score.type() == Score.Type.GAME_SCORE)
            .findFirst()
            .orElseThrow();
    }

    public Score highScore() {
        return hudEntities
            .selectWhere(Score.class, score -> score.type() == Score.Type.HIGH_SCORE)
            .findFirst().orElseThrow();
    }

    public <T> T value(GameSessionValueKey key, Class<T> type) {
        requireNonNull(key);
        final Object value = values.get(key);
        if (value != null) {
            return type.cast(value);
        }
        return null;
    }

    public void setValue(GameSessionValueKey key, Object value) {
        requireNonNull(key);
        values.put(key, value);
    }

    public void clearValue(GameSessionValueKey key) {
        requireNonNull(key);
        values.remove(key);
    }

    public FrameState thisFrame() {
        return frameState;
    }

    public void newFrameState(long tick) {
        frameState = new FrameState(tick, new GamePlayStep());
    }

    public int gameOverStateTicks() {
        return gameOverStateTicks;
    }

    public void setGameOverStateTicks(int gameOverStateTicks) {
        this.gameOverStateTicks = gameOverStateTicks;
    }

    public long levelStartTimeMillis() {
        return levelStartTimeMillis;
    }

    public void setLevelStartTimeMillis(long levelStartTimeMillis) {
        this.levelStartTimeMillis = levelStartTimeMillis;
    }
}
