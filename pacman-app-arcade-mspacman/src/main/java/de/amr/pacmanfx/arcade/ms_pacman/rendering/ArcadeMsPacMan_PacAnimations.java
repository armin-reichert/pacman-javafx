/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.buildAnimation;

public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationManager<SpriteID> {

    public enum AnimationID { PAC_MAN_MUNCHING }

    public ArcadeMsPacMan_PacAnimations() {
        super(ArcadeMsPacMan_SpriteSheet.INSTANCE);
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Pac.AnimationID.PAC_FULL -> buildAnimation()
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PACMAN_FULL))
                .once();

            case Pac.AnimationID.PAC_MUNCHING -> buildAnimation()
                .sprites(msPacManMunchingSprites(Direction.LEFT))
                .repeated();

            case Pac.AnimationID.PAC_DYING -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.MS_PACMAN_DYING))
                .ticksPerFrame(8)
                .once();

            case AnimationID.PAC_MAN_MUNCHING -> buildAnimation()
                .sprites(mrPacManMunchingSprites(Direction.LEFT))
                .ticksPerFrame(2)
                .repeated();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac) {
            switch (selectedID) {
                case Pac.AnimationID.PAC_MUNCHING -> currentAnimation().setSprites(msPacManMunchingSprites(pac.moveDir()));
                case AnimationID.PAC_MAN_MUNCHING -> currentAnimation().setSprites(mrPacManMunchingSprites(pac.moveDir()));
                default -> {}
            }
        }
    }

    private RectShort[] msPacManMunchingSprites(Direction dir) {
        return spriteSheet().sprites(switch (dir) {
            case RIGHT -> SpriteID.MS_PACMAN_MUNCHING_RIGHT;
            case LEFT  -> SpriteID.MS_PACMAN_MUNCHING_LEFT;
            case UP    -> SpriteID.MS_PACMAN_MUNCHING_UP;
            case DOWN  -> SpriteID.MS_PACMAN_MUNCHING_DOWN;
        });
    }

    private RectShort[] mrPacManMunchingSprites(Direction dir) {
        return spriteSheet().sprites(switch (dir) {
            case RIGHT -> SpriteID.MR_PACMAN_MUNCHING_RIGHT;
            case LEFT  -> SpriteID.MR_PACMAN_MUNCHING_LEFT;
            case UP    -> SpriteID.MR_PACMAN_MUNCHING_UP;
            case DOWN  -> SpriteID.MR_PACMAN_MUNCHING_DOWN;
        });
    }
}