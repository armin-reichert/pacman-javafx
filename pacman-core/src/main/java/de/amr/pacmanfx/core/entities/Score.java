/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.score.comp.ScoreDataComp;
import de.amr.pacmanfx.core.entities.score.comp.ScorePersistencyComp;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class Score extends GameEntity {

    public enum Type { GAME_SCORE, HIGH_SCORE }

    private final Type type;

    public Score(Type type) {
        this.type = requireNonNull(type);
        setComp(ScoreDataComp.class, new ScoreDataComp());
    }

    public Type type() {
        return type;
    }

    public ScoreDataComp data() {
        return reqComp(ScoreDataComp.class);
    }

    public Optional<ScorePersistencyComp> optPersistency() {
        return optComp(ScorePersistencyComp.class);
    }

    public ScorePersistencyComp requirePersistency() {
        return reqComp(ScorePersistencyComp.class);
    }
}