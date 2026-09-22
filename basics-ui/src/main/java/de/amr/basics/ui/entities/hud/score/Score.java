/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.hud.score;

import de.amr.basics.ui.ecs.GameEntity;

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

    public ScorePersistencyComp reqPersistency() {
        return reqComp(ScorePersistencyComp.class);
    }
}