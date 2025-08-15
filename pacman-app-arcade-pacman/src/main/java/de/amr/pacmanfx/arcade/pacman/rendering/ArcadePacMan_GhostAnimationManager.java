/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.*;
import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;

public class ArcadePacMan_GhostAnimationManager extends SpriteAnimationManager<SpriteID> {

    private final byte personality;

    public ArcadePacMan_GhostAnimationManager(ArcadePacMan_SpriteSheet spriteSheet, byte personality) {
        super(spriteSheet);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    public SpriteAnimation createAnimation(String id) {
        return switch (id) {
            case ANIM_GHOST_NORMAL              -> SpriteAnimation.build().of(ghostNormalSprites(Direction.LEFT)).frameTicks(8).forever();
            case ANIM_GHOST_FRIGHTENED          -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.GHOST_FRIGHTENED)).frameTicks(8).forever();
            case ANIM_GHOST_FLASHING            -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.GHOST_FLASHING)).frameTicks(7).forever();
            case ANIM_GHOST_EYES                -> SpriteAnimation.build().of(ghostEyesSprites(Direction.LEFT)).once();
            case ANIM_GHOST_NUMBER              -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.GHOST_NUMBERS)).once();
            case ANIM_BLINKY_DAMAGED            -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.RED_GHOST_DAMAGED)).once();
            case ANIM_BLINKY_NAIL_DRESS_RAPTURE -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.RED_GHOST_STRETCHED)).once();
            case ANIM_BLINKY_PATCHED            -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.RED_GHOST_PATCHED)).frameTicks(4).forever();
            case ANIM_BLINKY_NAKED              -> SpriteAnimation.build().of(spriteSheet().spriteSeq(SpriteID.RED_GHOST_NAKED)).frameTicks(4).forever();
            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    public void selectFrame(String id, int frameIndex) {
        super.selectFrame(id, frameIndex);
        if (ANIM_GHOST_NUMBER.equals(id)) {
            animation(ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Ghost ghost) {
            if (isCurrentAnimationID(ANIM_GHOST_NORMAL)) {
                current().setSprites(ghostNormalSprites(ghost.wishDir()));
            }
            if (isCurrentAnimationID(ANIM_GHOST_EYES)) {
                current().setSprites(ghostEyesSprites(ghost.wishDir()));
            }
        }
    }

    private RectShort[] ghostNormalSprites(Direction dir) {
        return spriteSheet().spriteSeq(switch (personality) {
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
        return spriteSheet().spriteSeq(switch (dir) {
            case Direction.RIGHT -> GHOST_EYES_RIGHT;
            case Direction.LEFT  -> GHOST_EYES_LEFT;
            case Direction.UP    -> GHOST_EYES_UP;
            case Direction.DOWN  -> GHOST_EYES_DOWN;
        });
    }
}