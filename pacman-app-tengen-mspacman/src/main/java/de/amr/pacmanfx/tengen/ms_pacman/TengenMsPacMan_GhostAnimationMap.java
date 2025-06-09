/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.SpriteID;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.getSprite;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.getSprites;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createAnimation;

public class TengenMsPacMan_GhostAnimationMap extends SpriteAnimationMap<RectArea> {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASH_TICKS = 7;  // TODO check this in emulator

    public TengenMsPacMan_GhostAnimationMap(TengenMsPacMan_SpriteSheet ss, byte personality) {
        super(ss);
        set(ANIM_GHOST_NORMAL,     createAnimation().sprites(ghostNormalSprites(personality, Direction.LEFT)).frameTicks(NORMAL_TICKS).endless());
        set(ANIM_GHOST_FRIGHTENED, createAnimation().sprites(getSprites(SpriteID.GHOST_FRIGHTENED)).frameTicks(FRIGHTENED_TICKS).endless());
        set(ANIM_GHOST_FLASHING,   createAnimation().sprites(getSprites(SpriteID.GHOST_FLASHING)).frameTicks(FLASH_TICKS).endless());
        set(ANIM_GHOST_EYES,       createAnimation().sprites(ghostEyesSprites(Direction.LEFT)).end());
        set(ANIM_GHOST_NUMBER,     createAnimation().sprites(ss.ghostNumberSprites()).end());
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
                currentAnimation().setSprites(ghostNormalSprites(ghost.personality(), ghost.wishDir()));
            }
            if (isCurrentAnimationID(ANIM_GHOST_EYES)) {
                currentAnimation().setSprites(ghostEyesSprites(ghost.wishDir()));
            }
        }
    }

    private RectArea[] ghostNormalSprites(byte id, Direction dir) {
        return switch (id) {
            case RED_GHOST_SHADOW -> switch (dir) {
                case Direction.RIGHT -> getSprites(SpriteID.RED_GHOST_RIGHT);
                case Direction.LEFT  -> getSprites(SpriteID.RED_GHOST_LEFT);
                case Direction.UP    -> getSprites(SpriteID.RED_GHOST_UP);
                case Direction.DOWN  -> getSprites(SpriteID.RED_GHOST_DOWN);
            };
            case PINK_GHOST_SPEEDY   -> switch (dir) {
                case Direction.RIGHT -> getSprites(SpriteID.PINK_GHOST_RIGHT);
                case Direction.LEFT  -> getSprites(SpriteID.PINK_GHOST_LEFT);
                case Direction.UP    -> getSprites(SpriteID.PINK_GHOST_UP);
                case Direction.DOWN  -> getSprites(SpriteID.PINK_GHOST_DOWN);
            };
            case CYAN_GHOST_BASHFUL  -> switch (dir) {
                case Direction.RIGHT -> getSprites(SpriteID.CYAN_GHOST_RIGHT);
                case Direction.LEFT  -> getSprites(SpriteID.CYAN_GHOST_LEFT);
                case Direction.UP    -> getSprites(SpriteID.CYAN_GHOST_UP);
                case Direction.DOWN  -> getSprites(SpriteID.CYAN_GHOST_DOWN);
            };
            case ORANGE_GHOST_POKEY  -> switch (dir) {
                case Direction.RIGHT -> getSprites(SpriteID.ORANGE_GHOST_RIGHT);
                case Direction.LEFT  -> getSprites(SpriteID.ORANGE_GHOST_LEFT);
                case Direction.UP    -> getSprites(SpriteID.ORANGE_GHOST_UP);
                case Direction.DOWN  -> getSprites(SpriteID.ORANGE_GHOST_DOWN);
            };
            default -> throw new IllegalArgumentException();
        };
    }

    private RectArea[] ghostEyesSprites(Direction dir) {
        return new RectArea[] {
            switch (dir) {
                case RIGHT -> getSprite(SpriteID.GHOST_EYES_RIGHT);
                case LEFT -> getSprite(SpriteID.GHOST_EYES_LEFT);
                case UP -> getSprite(SpriteID.GHOST_EYES_UP);
                case DOWN -> getSprite(SpriteID.GHOST_EYES_DOWN);
            }
        };
    }
}