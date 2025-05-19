/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;
import javafx.animation.Animation;

import java.util.stream.Stream;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class TengenMsPacMan_GhostAnimationSet extends SpriteAnimationSet {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASH_TICKS = 7;  // TODO check this in emulator

    public TengenMsPacMan_GhostAnimationSet(GameSpriteSheet ss, byte personality) {
        super(ss);
        add(ANIM_GHOST_NORMAL,     from(ss).take(ss.ghostNormalSprites(personality, Direction.LEFT)).frameTicks(NORMAL_TICKS).endless());
        add(ANIM_GHOST_FRIGHTENED, from(ss).take(ss.ghostFrightenedSprites()).frameTicks(FRIGHTENED_TICKS).endless());
        add(ANIM_GHOST_FLASHING,   from(ss).take(ss.ghostFlashingSprites()).frameTicks(FLASH_TICKS).endless());
        add(ANIM_GHOST_EYES,       from(ss).take(ss.ghostEyesSprites(Direction.LEFT)).end());
        add(ANIM_GHOST_NUMBER,     from(ss).take(ss.ghostNumberSprites()).end());
        // TODO start animations when selected
        Stream.of(ANIM_GHOST_EYES, ANIM_GHOST_FRIGHTENED, ANIM_GHOST_FLASHING).map(this::animation).forEach(Animation::play);
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