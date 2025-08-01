/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;

public class TengenMsPacMan_GhostAnimationMap extends SpriteAnimationMap<SpriteID> {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASH_TICKS = 7;  // TODO check this in emulator

    private final byte personality;

    public TengenMsPacMan_GhostAnimationMap(TengenMsPacMan_SpriteSheet spriteSheet, byte personality) {
        super(spriteSheet);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    protected SpriteAnimation createAnimation(String id) {
        return switch (id) {
            case ANIM_GHOST_NORMAL      -> SpriteAnimation.build().of(ghostNormalSprites(Direction.LEFT)).frameTicks(NORMAL_TICKS).forever();
            case ANIM_GHOST_FRIGHTENED  -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.GHOST_FRIGHTENED)).frameTicks(FRIGHTENED_TICKS).forever();
            case ANIM_GHOST_FLASHING    -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.GHOST_FLASHING)).frameTicks(FLASH_TICKS).forever();
            case ANIM_GHOST_EYES        -> SpriteAnimation.build().of(ghostEyesSprites(Direction.LEFT)).once();
            case ANIM_GHOST_NUMBER      -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.GHOST_NUMBERS)).once();
            default -> throw new IllegalArgumentException("Illegal animation ID " + id);
        };
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    public void selectAnimationAtFrame(String id, int frameIndex) {
        super.selectAnimationAtFrame(id, frameIndex);
        if (ANIM_GHOST_NUMBER.equals(id)) {
            animation(ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Ghost ghost) {
            if (isCurrentAnimationID(ANIM_GHOST_NORMAL)) {
                currentAnimation().setSprites(ghostNormalSprites(ghost.wishDir()));
            }
            if (isCurrentAnimationID(ANIM_GHOST_EYES)) {
                currentAnimation().setSprites(ghostEyesSprites(ghost.wishDir()));
            }
        }
    }

    private RectShort[] ghostNormalSprites(Direction dir) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> switch (dir) {
                case Direction.RIGHT -> spriteSheet().spriteSeq(SpriteID.RED_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet().spriteSeq(SpriteID.RED_GHOST_LEFT);
                case Direction.UP    -> spriteSheet().spriteSeq(SpriteID.RED_GHOST_UP);
                case Direction.DOWN  -> spriteSheet().spriteSeq(SpriteID.RED_GHOST_DOWN);
            };
            case PINK_GHOST_SPEEDY   -> switch (dir) {
                case Direction.RIGHT -> spriteSheet().spriteSeq(SpriteID.PINK_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet().spriteSeq(SpriteID.PINK_GHOST_LEFT);
                case Direction.UP    -> spriteSheet().spriteSeq(SpriteID.PINK_GHOST_UP);
                case Direction.DOWN  -> spriteSheet().spriteSeq(SpriteID.PINK_GHOST_DOWN);
            };
            case CYAN_GHOST_BASHFUL  -> switch (dir) {
                case Direction.RIGHT -> spriteSheet().spriteSeq(SpriteID.CYAN_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet().spriteSeq(SpriteID.CYAN_GHOST_LEFT);
                case Direction.UP    -> spriteSheet().spriteSeq(SpriteID.CYAN_GHOST_UP);
                case Direction.DOWN  -> spriteSheet().spriteSeq(SpriteID.CYAN_GHOST_DOWN);
            };
            case ORANGE_GHOST_POKEY  -> switch (dir) {
                case Direction.RIGHT -> spriteSheet().spriteSeq(SpriteID.ORANGE_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet().spriteSeq(SpriteID.ORANGE_GHOST_LEFT);
                case Direction.UP    -> spriteSheet().spriteSeq(SpriteID.ORANGE_GHOST_UP);
                case Direction.DOWN  -> spriteSheet().spriteSeq(SpriteID.ORANGE_GHOST_DOWN);
            };
            default -> throw new IllegalArgumentException();
        };
    }

    private RectShort[] ghostEyesSprites(Direction dir) {
        return new RectShort[] {
            switch (dir) {
                case RIGHT -> spriteSheet().sprite(SpriteID.GHOST_EYES_RIGHT);
                case LEFT  -> spriteSheet().sprite(SpriteID.GHOST_EYES_LEFT);
                case UP    -> spriteSheet().sprite(SpriteID.GHOST_EYES_UP);
                case DOWN  -> spriteSheet().sprite(SpriteID.GHOST_EYES_DOWN);
            }
        };
    }
}