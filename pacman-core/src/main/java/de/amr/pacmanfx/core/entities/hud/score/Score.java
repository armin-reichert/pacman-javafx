/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.hud.score;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.rendering.Renderable;

import static java.util.Objects.requireNonNull;

public class Score extends GameEntity implements Renderable {

    public enum Type { GAME_SCORE, HIGH_SCORE }

    private final Type type;

    public Score(Type type) {
        this.type = requireNonNull(type);
        setComp(ScoreDataComp.class, new ScoreDataComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.HUD;
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