/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.buildAnimation;

public class TengenMsPacMan_GhostAnimations extends SpriteAnimationManager<SpriteID> {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASH_TICKS = 7;  // TODO check this in emulator

    private final byte personality;

    public TengenMsPacMan_GhostAnimations(byte personality) {
        super(TengenMsPacMan_SpriteSheet.instance());
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Ghost.AnimationID.GHOST_NORMAL -> buildAnimation()
                .sprites(ghostNormalSprites(Direction.LEFT))
                .ticksPerFrame(NORMAL_TICKS)
                .repeated();

            case Ghost.AnimationID.GHOST_FRIGHTENED -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FRIGHTENED))
                .ticksPerFrame(FRIGHTENED_TICKS)
                .repeated();

            case Ghost.AnimationID.GHOST_FLASHING -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FLASHING))
                .ticksPerFrame(FLASH_TICKS)
                .repeated();

            case Ghost.AnimationID.GHOST_EYES -> buildAnimation()
                .sprites(ghostEyesSprites(Direction.LEFT))
                .once();

            case Ghost.AnimationID.GHOST_POINTS -> buildAnimation()
                .sprites(spriteSheet()
                .sprites(SpriteID.GHOST_NUMBERS))
                .once();

            default -> throw new IllegalArgumentException("Illegal animation ID " + animationID);
        };
    }

    @Override
    public void setAnimationFrame(Object animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (Ghost.AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(Ghost.AnimationID.GHOST_POINTS).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Ghost ghost) {
            if (isSelected(Ghost.AnimationID.GHOST_NORMAL)) {
                currentAnimation().setSprites(ghostNormalSprites(ghost.wishDir()));
            }
            if (isSelected(Ghost.AnimationID.GHOST_EYES)) {
                currentAnimation().setSprites(ghostEyesSprites(ghost.wishDir()));
            }
        }
    }

    private RectShort[] ghostNormalSprites(Direction dir) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> switch (dir) {
                case Direction.RIGHT -> spriteSheet.sprites(SpriteID.RED_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet.sprites(SpriteID.RED_GHOST_LEFT);
                case Direction.UP    -> spriteSheet.sprites(SpriteID.RED_GHOST_UP);
                case Direction.DOWN  -> spriteSheet.sprites(SpriteID.RED_GHOST_DOWN);
            };
            case PINK_GHOST_SPEEDY   -> switch (dir) {
                case Direction.RIGHT -> spriteSheet.sprites(SpriteID.PINK_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet.sprites(SpriteID.PINK_GHOST_LEFT);
                case Direction.UP    -> spriteSheet.sprites(SpriteID.PINK_GHOST_UP);
                case Direction.DOWN  -> spriteSheet.sprites(SpriteID.PINK_GHOST_DOWN);
            };
            case CYAN_GHOST_BASHFUL  -> switch (dir) {
                case Direction.RIGHT -> spriteSheet.sprites(SpriteID.CYAN_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet.sprites(SpriteID.CYAN_GHOST_LEFT);
                case Direction.UP    -> spriteSheet.sprites(SpriteID.CYAN_GHOST_UP);
                case Direction.DOWN  -> spriteSheet.sprites(SpriteID.CYAN_GHOST_DOWN);
            };
            case ORANGE_GHOST_POKEY  -> switch (dir) {
                case Direction.RIGHT -> spriteSheet.sprites(SpriteID.ORANGE_GHOST_RIGHT);
                case Direction.LEFT  -> spriteSheet.sprites(SpriteID.ORANGE_GHOST_LEFT);
                case Direction.UP    -> spriteSheet.sprites(SpriteID.ORANGE_GHOST_UP);
                case Direction.DOWN  -> spriteSheet.sprites(SpriteID.ORANGE_GHOST_DOWN);
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