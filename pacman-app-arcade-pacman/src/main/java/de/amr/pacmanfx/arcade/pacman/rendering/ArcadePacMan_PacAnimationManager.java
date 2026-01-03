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

public class ArcadePacMan_PacAnimationManager extends SpriteAnimationManager<SpriteID> {

    public enum AnimationID {
        ANIM_BIG_PAC_MAN,
    }

    public ArcadePacMan_PacAnimationManager(ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case CommonAnimationID.ANIM_PAC_FULL -> SpriteAnimation.builder()
                .ofSprite(spriteSheet.sprite(SpriteID.PACMAN_FULL)).once();

            case CommonAnimationID.ANIM_PAC_MUNCHING -> SpriteAnimation.builder()
                .fromSprites(pacMunchingSprites(Direction.LEFT))
                .endless();

            case CommonAnimationID.ANIM_PAC_DYING -> SpriteAnimation.builder()
                .fromSprites(spriteSheet.spriteSequence(SpriteID.PACMAN_DYING))
                .ticksPerFrame(8)
                .once();

            case AnimationID.ANIM_BIG_PAC_MAN -> SpriteAnimation.builder()
                .fromSprites(spriteSheet.spriteSequence(SpriteID.PACMAN_BIG))
                .ticksPerFrame(3)
                .endless();

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
            case RIGHT -> spriteSheet().spriteSequence(SpriteID.PACMAN_MUNCHING_RIGHT);
            case LEFT  -> spriteSheet().spriteSequence(SpriteID.PACMAN_MUNCHING_LEFT);
            case UP    -> spriteSheet().spriteSequence(SpriteID.PACMAN_MUNCHING_UP);
            case DOWN  -> spriteSheet().spriteSequence(SpriteID.PACMAN_MUNCHING_DOWN);
        };
    }
}