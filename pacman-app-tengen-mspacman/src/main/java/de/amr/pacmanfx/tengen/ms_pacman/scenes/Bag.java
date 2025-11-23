/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

public class Bag extends Actor {

    private static final String ANIM_ID_BAG = "bag";
    private static final String ANIM_ID_JUNIOR = "junior";

    private boolean open;

    public Bag(TengenMsPacMan_SpriteSheet spriteSheet) {
        var spriteAnimationManager = new SpriteAnimationManager<>(spriteSheet);
        spriteAnimationManager.setAnimation(ANIM_ID_BAG, SpriteAnimation.builder().ofSprite(spriteSheet.sprite(SpriteID.BLUE_BAG)).once());
        spriteAnimationManager.setAnimation(ANIM_ID_JUNIOR, SpriteAnimation.builder().ofSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC)).once());
        setAnimationManager(spriteAnimationManager);
        setOpen(false);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animationManager.select(open ? ANIM_ID_JUNIOR : ANIM_ID_BAG);
    }

    public boolean isOpen() {
        return open;
    }
}
