/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import java.util.Optional;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

class Bag extends Actor {
    private final SpriteAnimationManager<SpriteID> animationManager;
    private boolean open;

    public Bag(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        animationManager = new SpriteAnimationManager<>(spriteSheet);
        animationManager.setAnimation("junior", SpriteAnimation.build().ofSprite(spriteSheet.sprite(JUNIOR_PAC)).once());
        animationManager.setAnimation("bag", SpriteAnimation.build().ofSprite(spriteSheet.sprite(BLUE_BAG)).once());
    }

    @Override
    public Optional<AnimationManager> animationManager() {
        return Optional.of(animationManager);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animationManager.select(open ? "junior" : "bag");
    }

    public boolean isOpen() {
        return open;
    }
}
