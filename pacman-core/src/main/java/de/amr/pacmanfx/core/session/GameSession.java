/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.session;


import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.HUDState;

import java.io.File;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSession {

    private GameLevel level;

    private boolean demoLevel;

    private boolean playing;

    private final HUDState hud;

    private final Score score;

    private final Score highScore;

    private final LevelCounter levelCounter;

    private final LivesCounter livesCounter;

    private GameCheats cheats;

    public GameSession(String variantName) {
        requireNonNull(variantName);
        score = new Score();
        final File highScoreFile = ScoreSystem.highScoreFile(variantName);
        highScore = ScoreSystem.createPersistentScore(highScoreFile);
        levelCounter = new LevelCounter();
        livesCounter = new LivesCounter();
        hud = new HUDState();
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

    public void setDemoLevel(boolean demoLevel) {
        this.demoLevel = demoLevel;
    }

    public boolean isDemoLevel() {
        return demoLevel;
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
}
