/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

class BlinkingEnergizer extends GameEntity implements Renderable {

    private Pulse pulse;

    public BlinkingEnergizer() {
    }

    public Pulse pulse() {
        return pulse;
    }

    public void setPulse(Pulse pulse) {
        this.pulse = pulse;
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
