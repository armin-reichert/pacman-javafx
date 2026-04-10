/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder.builder;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    private final SpriteAnimationManager manager;
    private final byte personality;

    public ArcadeMsPacMan_GhostAnimations(SpriteAnimationManager spriteAnimationManager, byte personality) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        this.personality = requireValidGhostPersonality(personality);
        this.manager = requireNonNull(spriteAnimationManager);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case Ghost.AnimationID.GHOST_NORMAL -> builder(manager)
                .sprites(ghostNormalSprites(Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FRIGHTENED -> builder(manager)
                .sprites(spriteSheet().sprites(GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FLASHING -> builder(manager)
                .sprites(spriteSheet().sprites(GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_EYES -> builder(manager)
                .sprites(ghostEyesSprites(Direction.LEFT))
                .build();

            case Ghost.AnimationID.GHOST_POINTS -> builder(manager)
                .sprites(spriteSheet().sprites(GHOST_NUMBERS))
                .initiallyStopped()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(Object animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (Ghost.AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(Ghost.AnimationID.GHOST_POINTS).setCurrentFrame(frameIndex);
        }
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Ghost ghost) {
            switch (selectedID) {
                case Ghost.AnimationID.GHOST_NORMAL -> currentAnimation().setSprites(ghostNormalSprites(ghost.wishDir()));
                case Ghost.AnimationID.GHOST_EYES -> currentAnimation().setSprites(ghostEyesSprites(ghost.wishDir()));
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