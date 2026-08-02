/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.components.MovementComp;
import de.amr.pacmanfx.core.ecs.components.SpriteAnimComp;

public class Stork extends GameEntity {

    public Stork() {
        name = "Beatrix von";
        setComponent(MovementComp.class, new MovementComp());
        setComponent(SpriteAnimComp.class, new SpriteAnimComp());
    }

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public SpriteAnimComp spriteAnim() {
        return requireComponent(SpriteAnimComp.class);
    }
}