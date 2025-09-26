/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationSupport;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ANIM_BIG_PAC_MAN;

public class ArcadePacMan_PacAnimationManager extends SpriteAnimationManager<SpriteID> {

    public ArcadePacMan_PacAnimationManager(ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
    }

    @Override
    protected SpriteAnimation createAnimation(String id) {
        return switch (id) {
            case AnimationSupport.ANIM_PAC_MUNCHING -> SpriteAnimation.build().of(pacMunchingSprites(Direction.LEFT)).forever();
            case AnimationSupport.ANIM_PAC_DYING    -> SpriteAnimation.build().of(spriteSheet().spriteSequence(SpriteID.PACMAN_DYING)).frameTicks(8).once();
            case ANIM_BIG_PAC_MAN  -> SpriteAnimation.build().of(spriteSheet().spriteSequence(SpriteID.PACMAN_BIG)).frameTicks(3).forever();
            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac && isCurrentAnimationID(AnimationSupport.ANIM_PAC_MUNCHING)) {
            currentAnimation().setSprites(pacMunchingSprites(pac.moveDir()));
        }
    }

    private RectShort[] pacMunchingSprites(Direction dir) {
        return switch (dir) {
            case RIGHT -> spriteSheet().spriteSequence(SpriteID.PACMAN_MUNCHING_RIGHT);
            case LEFT  -> spriteSheet().spriteSequence(SpriteID.PACMAN_MUNCHING_LEFT);
            case UP    -> spriteSheet().spriteSequence(SpriteID.PACMAN_MUNCHING_UP);
            case DOWN  -> spriteSheet().spriteSequence(SpriteID.PACMAN_MUNCHING_DOWN);
        };
    }
}