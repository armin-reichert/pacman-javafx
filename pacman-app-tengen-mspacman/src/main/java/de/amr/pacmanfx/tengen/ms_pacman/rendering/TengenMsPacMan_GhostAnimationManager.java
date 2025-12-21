/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;

public class TengenMsPacMan_GhostAnimationManager extends SpriteAnimationManager<SpriteID> {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASH_TICKS = 7;  // TODO check this in emulator

    private final byte personality;

    public TengenMsPacMan_GhostAnimationManager(TengenMsPacMan_SpriteSheet spriteSheet, byte personality) {
        super(spriteSheet);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case CommonAnimationID.ANIM_GHOST_NORMAL      -> SpriteAnimation.builder().fromSprites(ghostNormalSprites(Direction.LEFT)).ticksPerFrame(NORMAL_TICKS).endless();
            case CommonAnimationID.ANIM_GHOST_FRIGHTENED  -> SpriteAnimation.builder().fromSprites(spriteSheet().spriteSequence(SpriteID.GHOST_FRIGHTENED)).ticksPerFrame(FRIGHTENED_TICKS).endless();
            case CommonAnimationID.ANIM_GHOST_FLASHING    -> SpriteAnimation.builder().fromSprites(spriteSheet().spriteSequence(SpriteID.GHOST_FLASHING)).ticksPerFrame(FLASH_TICKS).endless();
            case CommonAnimationID.ANIM_GHOST_EYES        -> SpriteAnimation.builder().fromSprites(ghostEyesSprites(Direction.LEFT)).once();
            case CommonAnimationID.ANIM_GHOST_NUMBER      -> SpriteAnimation.builder().fromSprites(spriteSheet().spriteSequence(SpriteID.GHOST_NUMBERS)).once();
            default -> throw new IllegalArgumentException("Illegal animation ID " + animationID);
        };
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    public void selectFrame(Object animationID, int frameIndex) {
        super.selectFrame(animationID, frameIndex);
        if (CommonAnimationID.ANIM_GHOST_NUMBER.equals(animationID)) {
            animation(CommonAnimationID.ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Ghost ghost) {
            if (isCurrentAnimationID(CommonAnimationID.ANIM_GHOST_NORMAL)) {
                currentAnimation().setSprites(ghostNormalSprites(ghost.wishDir()));
            }
            if (isCurrentAnimationID(CommonAnimationID.ANIM_GHOST_EYES)) {
                currentAnimation().setSprites(ghostEyesSprites(ghost.wishDir()));
            }
        }
    }

    private RectShort[] ghostNormalSprites(Direction dir) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> switch (dir) {
                case Direction.RIGHT -> spriteSheet.spriteSequence(SpriteID.RED_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet.spriteSequence(SpriteID.RED_GHOST_LEFT);
                case Direction.UP    -> spriteSheet.spriteSequence(SpriteID.RED_GHOST_UP);
                case Direction.DOWN  -> spriteSheet.spriteSequence(SpriteID.RED_GHOST_DOWN);
            };
            case PINK_GHOST_SPEEDY   -> switch (dir) {
                case Direction.RIGHT -> spriteSheet.spriteSequence(SpriteID.PINK_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet.spriteSequence(SpriteID.PINK_GHOST_LEFT);
                case Direction.UP    -> spriteSheet.spriteSequence(SpriteID.PINK_GHOST_UP);
                case Direction.DOWN  -> spriteSheet.spriteSequence(SpriteID.PINK_GHOST_DOWN);
            };
            case CYAN_GHOST_BASHFUL  -> switch (dir) {
                case Direction.RIGHT -> spriteSheet.spriteSequence(SpriteID.CYAN_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet.spriteSequence(SpriteID.CYAN_GHOST_LEFT);
                case Direction.UP    -> spriteSheet.spriteSequence(SpriteID.CYAN_GHOST_UP);
                case Direction.DOWN  -> spriteSheet.spriteSequence(SpriteID.CYAN_GHOST_DOWN);
            };
            case ORANGE_GHOST_POKEY  -> switch (dir) {
                case Direction.RIGHT -> spriteSheet.spriteSequence(SpriteID.ORANGE_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet.spriteSequence(SpriteID.ORANGE_GHOST_LEFT);
                case Direction.UP    -> spriteSheet.spriteSequence(SpriteID.ORANGE_GHOST_UP);
                case Direction.DOWN  -> spriteSheet.spriteSequence(SpriteID.ORANGE_GHOST_DOWN);
            };
            default -> throw new IllegalArgumentException();
        };
    }

    private RectShort[] ghostEyesSprites(Direction dir) {
        return new RectShort[] {
            switch (dir) {
                case RIGHT -> spriteSheet.sprite(SpriteID.GHOST_EYES_RIGHT);
                case LEFT  -> spriteSheet.sprite(SpriteID.GHOST_EYES_LEFT);
                case UP    -> spriteSheet.sprite(SpriteID.GHOST_EYES_UP);
                case DOWN  -> spriteSheet.sprite(SpriteID.GHOST_EYES_DOWN);
            }
        };
    }
}