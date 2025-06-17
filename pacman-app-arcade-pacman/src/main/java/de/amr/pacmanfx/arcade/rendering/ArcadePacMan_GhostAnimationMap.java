/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.*;
import static de.amr.pacmanfx.arcade.rendering.SpriteID.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createAnimation;

public class ArcadePacMan_GhostAnimationMap extends SpriteAnimationMap {

    public ArcadePacMan_GhostAnimationMap(ArcadePacMan_SpriteSheet spriteSheet, byte personality) {
        super(spriteSheet);
        set(ANIM_GHOST_NORMAL,              createAnimation().ofSprites(ghostNormalSprites(personality, Direction.LEFT)).frameTicks(8).endless());
        set(ANIM_GHOST_FRIGHTENED,          createAnimation().ofSprites(spriteSheet.spriteSeq(SpriteID.GHOST_FRIGHTENED)).frameTicks(8).endless());
        set(ANIM_GHOST_FLASHING,            createAnimation().ofSprites(spriteSheet.spriteSeq(SpriteID.GHOST_FLASHING)).frameTicks(7).endless());
        set(ANIM_GHOST_EYES,                createAnimation().ofSprites(ghostEyesSprites(Direction.LEFT)).end());
        set(ANIM_GHOST_NUMBER,              createAnimation().ofSprites(spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS)).end());
        set(ANIM_BLINKY_DAMAGED,            createAnimation().ofSprites(spriteSheet.spriteSeq(SpriteID.RED_GHOST_DAMAGED)).end());
        set(ANIM_BLINKY_NAIL_DRESS_RAPTURE, createAnimation().ofSprites(spriteSheet.spriteSeq(SpriteID.RED_GHOST_STRETCHED)).end());
        set(ANIM_BLINKY_PATCHED,            createAnimation().ofSprites(spriteSheet.spriteSeq(SpriteID.RED_GHOST_PATCHED)).frameTicks(4).endless());
        set(ANIM_BLINKY_NAKED,              createAnimation().ofSprites(spriteSheet.spriteSeq(SpriteID.RED_GHOST_NAKED)).frameTicks(4).endless());
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
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
                currentAnimation().setSprites(ghostNormalSprites(ghost.personality(), ghost.wishDir()));
            }
            if (isCurrentAnimationID(ANIM_GHOST_EYES)) {
                currentAnimation().setSprites(ghostEyesSprites(ghost.wishDir()));
            }
        }
    }

    private Sprite[] ghostNormalSprites(byte id, Direction dir) {
        return spriteSheet().spriteSeq(switch (id) {
            case 0 -> switch (dir) {
                case RIGHT -> SpriteID.RED_GHOST_RIGHT;
                case LEFT ->  SpriteID.RED_GHOST_LEFT;
                case UP ->    SpriteID.RED_GHOST_UP;
                case DOWN ->  SpriteID.RED_GHOST_DOWN;
            };
            case 1 -> switch (dir) {
                case RIGHT -> SpriteID.PINK_GHOST_RIGHT;
                case LEFT ->  SpriteID.PINK_GHOST_LEFT;
                case UP ->    SpriteID.PINK_GHOST_UP;
                case DOWN ->  SpriteID.PINK_GHOST_DOWN;
            };
            case 2 -> switch (dir) {
                case RIGHT -> SpriteID.CYAN_GHOST_RIGHT;
                case LEFT ->  SpriteID.CYAN_GHOST_LEFT;
                case UP ->    SpriteID.CYAN_GHOST_UP;
                case DOWN ->  SpriteID.CYAN_GHOST_DOWN;
            };
            case 3 -> switch (dir) {
                case RIGHT -> SpriteID.ORANGE_GHOST_RIGHT;
                case LEFT ->  SpriteID.ORANGE_GHOST_LEFT;
                case UP ->    SpriteID.ORANGE_GHOST_UP;
                case DOWN ->  SpriteID.ORANGE_GHOST_DOWN;
            };
            default -> throw new IllegalArgumentException("Illegal ghost ID " + id);
        });
    }

    private Sprite[] ghostEyesSprites(Direction dir) {
        return spriteSheet().spriteSeq(switch (dir) {
            case Direction.RIGHT -> GHOST_EYES_RIGHT;
            case Direction.LEFT  -> GHOST_EYES_LEFT;
            case Direction.UP    -> GHOST_EYES_UP;
            case Direction.DOWN  -> GHOST_EYES_DOWN;
        });
    }
}