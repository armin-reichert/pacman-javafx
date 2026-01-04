/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.buildAnimation;

public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationManager<SpriteID> {

    private final byte personality;

    public ArcadeMsPacMan_GhostAnimations(byte personality) {
        super(ArcadeMsPacMan_SpriteSheet.INSTANCE);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case CommonAnimationID.GHOST_NORMAL -> buildAnimation()
                .sprites(ghostNormalSprites(Direction.LEFT))
                .ticksPerFrame(8)
                .repeated();

            case CommonAnimationID.GHOST_FRIGHTENED -> buildAnimation()
                .sprites(spriteSheet().sprites(GHOST_FRIGHTENED))
                .ticksPerFrame(8)
                .repeated();

            case CommonAnimationID.GHOST_FLASHING -> buildAnimation()
                .sprites(spriteSheet().sprites(GHOST_FLASHING))
                .ticksPerFrame(7)
                .repeated();

            case CommonAnimationID.GHOST_EYES -> buildAnimation()
                .sprites(ghostEyesSprites(Direction.LEFT))
                .once();

            case CommonAnimationID.GHOST_POINTS -> buildAnimation()
                .sprites(spriteSheet().sprites(GHOST_NUMBERS))
                .once();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    public void selectFrame(Object animationID, int frameIndex) {
        super.selectFrame(animationID, frameIndex);
        if (CommonAnimationID.GHOST_POINTS.equals(animationID)) {
            animation(CommonAnimationID.GHOST_POINTS).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Ghost ghost) {
            switch (selectedID) {
                case CommonAnimationID.GHOST_NORMAL -> currentAnimation().setSprites(ghostNormalSprites(ghost.wishDir()));
                case CommonAnimationID.GHOST_EYES -> currentAnimation().setSprites(ghostEyesSprites(ghost.wishDir()));
                default -> {}
            }
        }
    }

    private RectShort[] ghostNormalSprites(Direction dir) {
        return spriteSheet().sprites(switch (personality) {
            case RED_GHOST_SHADOW -> switch (dir) {
                case RIGHT -> RED_GHOST_RIGHT;
                case LEFT -> RED_GHOST_LEFT;
                case UP -> RED_GHOST_UP;
                case DOWN -> RED_GHOST_DOWN;
            };
            case PINK_GHOST_SPEEDY -> switch (dir) {
                case RIGHT -> PINK_GHOST_RIGHT;
                case LEFT -> PINK_GHOST_LEFT;
                case UP -> PINK_GHOST_UP;
                case DOWN -> PINK_GHOST_DOWN;
            };
            case CYAN_GHOST_BASHFUL -> switch (dir) {
                case RIGHT -> CYAN_GHOST_RIGHT;
                case LEFT -> CYAN_GHOST_LEFT;
                case UP -> CYAN_GHOST_UP;
                case DOWN -> CYAN_GHOST_DOWN;
            };
            case ORANGE_GHOST_POKEY -> switch (dir) {
                case RIGHT -> ORANGE_GHOST_RIGHT;
                case LEFT -> ORANGE_GHOST_LEFT;
                case UP -> ORANGE_GHOST_UP;
                case DOWN -> ORANGE_GHOST_DOWN;
            };
            default -> throw new IllegalArgumentException();
        });
    }

    private RectShort[] ghostEyesSprites(Direction dir) {
        return new RectShort[] {
            switch (dir) {
                case RIGHT -> spriteSheet.sprite(GHOST_EYES_RIGHT);
                case LEFT  -> spriteSheet.sprite(GHOST_EYES_LEFT);
                case UP    -> spriteSheet.sprite(GHOST_EYES_UP);
                case DOWN  -> spriteSheet.sprite(GHOST_EYES_DOWN);
            }
        };
    }
}