/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.buildAnimation;

public class ArcadePacMan_PacAnimations extends SpriteAnimationManager<SpriteID> {

    public enum AnimationID {
        ANIM_BIG_PAC_MAN,
    }

    public ArcadePacMan_PacAnimations(ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case CommonAnimationID.ANIM_PAC_FULL -> buildAnimation()
                .singleSprite(spriteSheet.sprite(SpriteID.PACMAN_FULL))
                .once();

            case CommonAnimationID.ANIM_PAC_MUNCHING -> buildAnimation()
                .sprites(pacMunchingSprites(Direction.LEFT))
                .repeated();

            case CommonAnimationID.ANIM_PAC_DYING -> buildAnimation()
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_DYING))
                .ticksPerFrame(8)
                .once();

            case AnimationID.ANIM_BIG_PAC_MAN -> buildAnimation()
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_BIG))
                .ticksPerFrame(3)
                .repeated();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac && isSelected(CommonAnimationID.ANIM_PAC_MUNCHING)) {
            currentAnimation().setSprites(pacMunchingSprites(pac.moveDir()));
        }
    }

    private RectShort[] pacMunchingSprites(Direction dir) {
        return switch (dir) {
            case RIGHT -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_RIGHT);
            case LEFT  -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_LEFT);
            case UP    -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_UP);
            case DOWN  -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_DOWN);
        };
    }
}