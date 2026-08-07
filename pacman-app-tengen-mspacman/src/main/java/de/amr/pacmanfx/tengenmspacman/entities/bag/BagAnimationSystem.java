/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.bag;


import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Bag;

public final class BagAnimationSystem {

    public static void update(Bag bag) {
        bag.spriteAnim().animation().select(bag.isOpen() ? CommonSpriteAnimationID.JUNIOR : CommonSpriteAnimationID.BAG);
    }
}
