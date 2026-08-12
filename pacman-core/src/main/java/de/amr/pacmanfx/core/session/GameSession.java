/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.session;

import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gameplay.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gameplay.hunt.HuntingStep;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.HUDState;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSession {

    private final GameFlowController gameFlow;

    private FrameContext frame;

    private GameLevel level;

    private ArcadeHouseGateKeeper gateKeeper;

    private boolean attractMode;

    private boolean playing;

    private final HUDState hud;

    private final Score score;

    private final Score highScore;

    private final LevelCounter levelCounter;

    private final LivesCounter livesCounter;

    private GameCheats cheats;

    private final Map<Object, Object> values = new HashMap<>();

    public GameSession(String variantName, GameFlowController gameFlow) {
        requireNonNull(variantName);
        this.gameFlow = requireNonNull(gameFlow);
        score = new Score();
        final File highScoreFile = ScoreSystem.highScoreFile(variantName);
        highScore = ScoreSystem.createPersistentScore(highScoreFile);
        levelCounter = new LevelCounter();
        livesCounter = new LivesCounter();
        hud = new HUDState();
        gateKeeper = new ArcadeHouseGateKeeper();
    }

    public GameFlowController gameFlow() {
        return gameFlow;
    }

    public GameState gameState() {
        return gameFlow.state();
    }

    public void setLevel(GameLevel level) {
        this.level = level;
    }

    public Optional<GameLevel> optLevel() {
        return Optional.ofNullable(level);
    }

    public GameLevel assertLevel() {
        return optLevel().orElseThrow();
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

    public void setCheats(GameCheats cheats) {
        this.cheats = requireNonNull(cheats);
        cheats.cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                highScore.data().setEnabled(false);
            }
        });
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

    public <T> T value(Object key, Class<T> type) {
        requireNonNull(key);
        final Object value = values.get(key);
        if (value != null) {
            return type.cast(value);
        }
        return null;
    }

    public void setValue(Object key, Object value) {
        requireNonNull(key);
        values.put(key, value);
    }

    public FrameContext thisFrame() {
        return frame;
    }

    public void newFrameContext(long tick) {
        frame = new FrameContext(tick, new HuntingStep());
    }
}
