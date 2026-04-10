/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID {
        BLINKY_DAMAGED,
        BLINKY_DRESS_PATCHED,
        BLINKY_NAKED
    }

    private final SpriteAnimationManager manager;
    private final byte personality;

    public ArcadePacMan_GhostAnimations(SpriteAnimationManager manager, byte personality) {
        super(ArcadePacMan_SpriteSheet.instance());
        this.manager = requireNonNull(manager);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    public SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Ghost.AnimationID.GHOST_NORMAL -> SpriteAnimationBuilder.builder(manager)
                .sprites(ghostNormalSprites(spriteSheet, personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FRIGHTENED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FLASHING -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_EYES -> SpriteAnimationBuilder.builder(manager)
                .sprites(ghostEyesSprites(spriteSheet, Direction.LEFT))
                .build();

            case Ghost.AnimationID.GHOST_POINTS -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.GHOST_NUMBERS))
                .initiallyStopped()
                .build();

            case AnimationID.BLINKY_DAMAGED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_DAMAGED))
                .initiallyStopped()
                .build();

            case AnimationID.BLINKY_DRESS_PATCHED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_PATCHED))
                .frameTicks(4)
                .repeated()
                .initiallyStopped()
                .build();

            case AnimationID.BLINKY_NAKED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_NAKED))
                .frameTicks(4)
                .repeated()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(Object animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (Ghost.AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(Ghost.AnimationID.GHOST_POINTS).setCurrentFrame(frameIndex);
        }
    }

    public static RectShort[] ghostNormalSprites(SpriteSheet<SpriteID> spriteSheet, byte personality, Direction dir) {
        return spriteSheet.sprites(switch (personality) {
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

    public static RectShort[] ghostEyesSprites(SpriteSheet<SpriteID> spriteSheet, Direction dir) {
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