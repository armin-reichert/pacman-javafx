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

    public enum AnimationID { BAG, JUNIOR }

    private boolean open;

    public Bag(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        final var animations = new SpriteAnimationManager<>(spriteSheet) {
            @Override
            protected SpriteAnimation createAnimation(Object animationID) {
                return switch (animationID) {
                    case AnimationID.JUNIOR -> SpriteAnimation.buildAnimation()
                        .singleSprite(spriteSheet.sprite(JUNIOR_PAC))
                        .once();

                    case AnimationID.BAG -> SpriteAnimation.buildAnimation()
                        .singleSprite(spriteSheet.sprite(BLUE_BAG))
                        .once();
                    default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
                };
            }
        };
        setAnimationManager(animations);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animationManager.select(open ? AnimationID.JUNIOR : AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}
