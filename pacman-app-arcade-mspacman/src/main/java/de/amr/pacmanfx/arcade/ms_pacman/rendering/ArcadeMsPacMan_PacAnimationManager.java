/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

public class ArcadeMsPacMan_PacAnimationManager extends SpriteAnimationManager<SpriteID> {

    public ArcadeMsPacMan_PacAnimationManager(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case CommonAnimationID.ANIM_PAC_FULL -> SpriteAnimation.builder()
                    .ofSprite(spriteSheet.sprite(SpriteID.MS_PACMAN_FULL))
                    .once();

            case CommonAnimationID.ANIM_PAC_MUNCHING -> SpriteAnimation.builder()
                .fromSprites(msPacManMunchingSprites(Direction.LEFT))
                .endless();

            case CommonAnimationID.ANIM_PAC_DYING -> SpriteAnimation.builder()
                .fromSprites(spriteSheet().spriteSequence(SpriteID.MS_PACMAN_DYING))
                .ticksPerFrame(8)
                .once();

            case ArcadeMsPacMan_UIConfig.AnimationID.PAC_MAN_MUNCHING -> SpriteAnimation.builder()
                .fromSprites(mrPacManMunchingSprites(Direction.LEFT))
                .ticksPerFrame(2)
                .endless();

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
                case CommonAnimationID.ANIM_PAC_MUNCHING -> currentAnimation().setSprites(msPacManMunchingSprites(pac.moveDir()));
                case ArcadeMsPacMan_UIConfig.AnimationID.PAC_MAN_MUNCHING -> currentAnimation().setSprites(mrPacManMunchingSprites(pac.moveDir()));
                default -> {}
            }
        }
    }

    private RectShort[] msPacManMunchingSprites(Direction dir) {
        return spriteSheet().spriteSequence(switch (dir) {
            case RIGHT -> SpriteID.MS_PACMAN_MUNCHING_RIGHT;
            case LEFT  -> SpriteID.MS_PACMAN_MUNCHING_LEFT;
            case UP    -> SpriteID.MS_PACMAN_MUNCHING_UP;
            case DOWN  -> SpriteID.MS_PACMAN_MUNCHING_DOWN;
        });
    }

    private RectShort[] mrPacManMunchingSprites(Direction dir) {
        return spriteSheet().spriteSequence(switch (dir) {
            case RIGHT -> SpriteID.MR_PACMAN_MUNCHING_RIGHT;
            case LEFT  -> SpriteID.MR_PACMAN_MUNCHING_LEFT;
            case UP    -> SpriteID.MR_PACMAN_MUNCHING_UP;
            case DOWN  -> SpriteID.MR_PACMAN_MUNCHING_DOWN;
        });
    }
}