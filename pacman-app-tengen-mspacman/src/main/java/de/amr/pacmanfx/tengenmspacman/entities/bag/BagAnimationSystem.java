/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.bag;


import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;

public final class BagAnimationSystem {

    public static void update(Bag bag) {
        bag.spriteAnim().spriteAnimations().select(bag.isOpen() ? CommonSpriteAnimationID.JUNIOR : CommonSpriteAnimationID.BAG);
    }
}
