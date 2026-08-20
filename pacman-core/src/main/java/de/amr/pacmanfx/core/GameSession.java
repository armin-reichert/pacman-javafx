/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gameplay.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.gamestate.FrameState;
import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;
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

    private ArcadeHouseGateKeeper gateKeeper;

    private boolean attractMode;

    private boolean playing;

    private final HUDState hud;

    private final Score score;

    private final Score highScore;

    private final LevelCounter levelCounter;

    private final LivesCounter livesCounter;

    private final GameCheats cheats;

    private final Map<GameSessionValueKey, Object> values = new HashMap<>();

    private int gameOverStateTicks;

    private long levelStartTimeMillis;

    private boolean cutScenesEnabled;

    public GameSession(String variantName, GameCheats cheats) {
        requireNonNull(variantName);
        requireNonNull(cheats);

        this.cheats = cheats;
        this.score = new Score();
        this.highScore = ScoreSystem.createPersistentScore(ScoreSystem.highScoreFile(variantName));
        this.levelCounter = new LevelCounter();
        this.livesCounter = new LivesCounter();
        this.hud = new HUDState();
        this.gateKeeper = new ArcadeHouseGateKeeper();

        newFrameState(0);

        cheats.cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                highScore.data().setEnabled(false);
            }
        });
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

    public void setGateKeeper(ArcadeHouseGateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    public ArcadeHouseGateKeeper gateKeeper() {
        return gateKeeper;
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

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean cutScenesEnabled() {
        return cutScenesEnabled;
    }

    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled = enabled;
    }

    public Score score() {
        return score;
    }

    public Score highScore() {
        return highScore;
    }

    public LevelCounter levelCounter() {
        return levelCounter;
    }

    public LivesCounter livesCounter() {
        return livesCounter;
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
