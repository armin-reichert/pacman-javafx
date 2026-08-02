/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities;


import de.amr.pacmanfx.core.model.entities.ActorAnimationID;

public class BagAnimationSystem {

    public static void update(Bag bag) {
        bag.spriteAnim().animation().select(bag.isOpen() ? ActorAnimationID.JUNIOR : ActorAnimationID.BAG);
    }

}
