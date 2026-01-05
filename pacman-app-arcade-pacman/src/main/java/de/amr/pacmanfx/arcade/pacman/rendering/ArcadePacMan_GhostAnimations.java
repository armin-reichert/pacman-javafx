/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.buildAnimation;

public class ArcadePacMan_GhostAnimations extends SpriteAnimationManager<SpriteID> {

    public enum AnimationID {
        BLINKY_DAMAGED,
        BLINKY_DRESS_PATCHED,
        BLINKY_NAKED
    }

    private final byte personality;

    public ArcadePacMan_GhostAnimations(byte personality) {
        super(ArcadePacMan_SpriteSheet.INSTANCE);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    public SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Ghost.AnimationID.GHOST_NORMAL -> buildAnimation()
                .sprites(ghostNormalSprites(Direction.LEFT))
                .ticksPerFrame(8)
                .repeated();

            case Ghost.AnimationID.GHOST_FRIGHTENED -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FRIGHTENED))
                .ticksPerFrame(8)
                .repeated();

            case Ghost.AnimationID.GHOST_FLASHING -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FLASHING))
                .ticksPerFrame(7)
                .repeated();

            case Ghost.AnimationID.GHOST_EYES -> buildAnimation()
                .sprites(ghostEyesSprites(Direction.LEFT))
                .once();

            case Ghost.AnimationID.GHOST_POINTS -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.GHOST_NUMBERS))
                .once();

            case AnimationID.BLINKY_DAMAGED -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_DAMAGED))
                .once();

            case AnimationID.BLINKY_DRESS_PATCHED -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_PATCHED))
                .ticksPerFrame(4)
                .repeated();

            case AnimationID.BLINKY_NAKED -> buildAnimation()
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_NAKED))
                .ticksPerFrame(4)
                .repeated();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.INSTANCE;
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
        return spriteSheet().sprites(switch (personality) {
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