/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

public class Bag extends Actor {
    private boolean open;

    public Bag(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        var spriteAnimationManager = new SpriteAnimationManager<>(spriteSheet);
        spriteAnimationManager.setAnimation("junior", SpriteAnimation.builder().ofSprite(spriteSheet.sprite(JUNIOR_PAC)).once());
        spriteAnimationManager.setAnimation("bag", SpriteAnimation.builder().ofSprite(spriteSheet.sprite(BLUE_BAG)).once());
        setAnimationManager(spriteAnimationManager);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animationManager.select(open ? "junior" : "bag");
    }

    public boolean isOpen() {
        return open;
    }
}
