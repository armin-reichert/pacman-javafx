/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.timer.Pulse;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.ecs.GameEntity;

class Energizer extends GameEntity implements Renderable {

    private Pulse pulse;

    public Energizer() {
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
