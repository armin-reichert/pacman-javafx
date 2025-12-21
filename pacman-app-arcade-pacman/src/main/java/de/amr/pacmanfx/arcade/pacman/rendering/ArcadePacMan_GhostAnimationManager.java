/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;

public class ArcadePacMan_GhostAnimationManager extends SpriteAnimationManager<SpriteID> {

    private final byte personality;

    public ArcadePacMan_GhostAnimationManager(ArcadePacMan_SpriteSheet spriteSheet, byte personality) {
        super(spriteSheet);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    public SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case CommonAnimationID.ANIM_GHOST_NORMAL -> SpriteAnimation.builder()
                .fromSprites(ghostNormalSprites(Direction.LEFT))
                .ticksPerFrame(8)
                .endless();

            case CommonAnimationID.ANIM_GHOST_FRIGHTENED -> SpriteAnimation.builder()
                .fromSprites(spriteSheet().spriteSequence(SpriteID.GHOST_FRIGHTENED))
                .ticksPerFrame(8)
                .endless();

            case CommonAnimationID.ANIM_GHOST_FLASHING -> SpriteAnimation.builder()
                .fromSprites(spriteSheet().spriteSequence(SpriteID.GHOST_FLASHING))
                .ticksPerFrame(7)
                .endless();

            case CommonAnimationID.ANIM_GHOST_EYES -> SpriteAnimation.builder()
                .fromSprites(ghostEyesSprites(Direction.LEFT))
                .once();

            case CommonAnimationID.ANIM_GHOST_NUMBER -> SpriteAnimation.builder()
                .fromSprites(spriteSheet().spriteSequence(SpriteID.GHOST_NUMBERS))
                .once();

            case ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_DAMAGED -> SpriteAnimation.builder()
                .fromSprites(spriteSheet().spriteSequence(SpriteID.RED_GHOST_DAMAGED))
                .once();

            case ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_NAIL_DRESS_RAPTURE -> SpriteAnimation.builder()
                .fromSprites(spriteSheet().spriteSequence(SpriteID.RED_GHOST_STRETCHED))
                .once();

            case ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_PATCHED -> SpriteAnimation.builder()
                .fromSprites(spriteSheet().spriteSequence(SpriteID.RED_GHOST_PATCHED))
                .ticksPerFrame(4)
                .endless();

            case ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_NAKED -> SpriteAnimation.builder()
                .fromSprites(spriteSheet().spriteSequence(SpriteID.RED_GHOST_NAKED))
                .ticksPerFrame(4)
                .endless();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
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
        return spriteSheet().spriteSequence(switch (personality) {
            case RED_GHOST_SHADOW -> switch (dir) {
                case RIGHT -> SpriteID.RED_GHOST_RIGHT;
                case LEFT ->  SpriteID.RED_GHOST_LEFT;
                case UP ->    SpriteID.RED_GHOST_UP;
                case DOWN ->  SpriteID.RED_GHOST_DOWN;
            };
            case PINK_GHOST_SPEEDY -> switch (dir) {
                case RIGHT -> SpriteID.PINK_GHOST_RIGHT;
                case LEFT ->  SpriteID.PINK_GHOST_LEFT;
                case UP ->    SpriteID.PINK_GHOST_UP;
                case DOWN ->  SpriteID.PINK_GHOST_DOWN;
            };
            case CYAN_GHOST_BASHFUL -> switch (dir) {
                case RIGHT -> SpriteID.CYAN_GHOST_RIGHT;
                case LEFT ->  SpriteID.CYAN_GHOST_LEFT;
                case UP ->    SpriteID.CYAN_GHOST_UP;
                case DOWN ->  SpriteID.CYAN_GHOST_DOWN;
            };
            case ORANGE_GHOST_POKEY -> switch (dir) {
                case RIGHT -> SpriteID.ORANGE_GHOST_RIGHT;
                case LEFT ->  SpriteID.ORANGE_GHOST_LEFT;
                case UP ->    SpriteID.ORANGE_GHOST_UP;
                case DOWN ->  SpriteID.ORANGE_GHOST_DOWN;
            };
            default -> throw new IllegalArgumentException();
        });
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