/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createSpriteAnimation;

public class TengenMsPacMan_GhostAnimationMap extends SpriteAnimationMap {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASH_TICKS = 7;  // TODO check this in emulator

    public TengenMsPacMan_GhostAnimationMap(GameSpriteSheet ss, byte personality) {
        super(ss);
        set(ANIM_GHOST_NORMAL,     createSpriteAnimation().sprites(ss.ghostNormalSprites(personality, Direction.LEFT)).frameTicks(NORMAL_TICKS).endless());
        set(ANIM_GHOST_FRIGHTENED, createSpriteAnimation().sprites(ss.ghostFrightenedSprites()).frameTicks(FRIGHTENED_TICKS).endless());
        set(ANIM_GHOST_FLASHING,   createSpriteAnimation().sprites(ss.ghostFlashingSprites()).frameTicks(FLASH_TICKS).endless());
        set(ANIM_GHOST_EYES,       createSpriteAnimation().sprites(ss.ghostEyesSprites(Direction.LEFT)).end());
        set(ANIM_GHOST_NUMBER,     createSpriteAnimation().sprites(ss.ghostNumberSprites()).end());
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
            GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
            if (isCurrentAnimationID(ANIM_GHOST_NORMAL)) {
                currentAnimation().setSprites(gss.ghostNormalSprites(ghost.personality(), ghost.wishDir()));
            }
            if (isCurrentAnimationID(ANIM_GHOST_EYES)) {
                currentAnimation().setSprites(gss.ghostEyesSprites(ghost.wishDir()));
            }
        }
    }
}