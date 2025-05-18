/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostAnimations;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.animation.Animation;

import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class ArcadePacMan_GhostAnimations extends SpriteAnimationSet implements GhostAnimations {

    public static final String ANIM_BLINKY_DAMAGED            = "damaged";
    public static final String ANIM_BLINKY_PATCHED            = "patched";
    public static final String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "nail_dress_rapture";
    public static final String ANIM_BLINKY_NAKED              = "naked";

    public ArcadePacMan_GhostAnimations(ArcadePacMan_SpriteSheet ss, byte personality) {
        add(Map.of(
            ANIM_GHOST_NORMAL,              from(ss).take(ss.ghostNormalSprites(personality, Direction.LEFT)).frameTicks(8).endless(),
            ANIM_GHOST_FRIGHTENED,          from(ss).take(ss.ghostFrightenedSprites()).frameTicks(8).endless(),
            ANIM_GHOST_FLASHING,            from(ss).take(ss.ghostFlashingSprites()).frameTicks(7).endless(),
            ANIM_GHOST_EYES,                from(ss).take(ss.ghostEyesSprites(Direction.LEFT)).end(),
            ANIM_GHOST_NUMBER,              from(ss).take(ss.ghostNumberSprites()).end(),
            ANIM_BLINKY_DAMAGED,            from(ss).take(ss.blinkyDamagedSprites()).end(),
            ANIM_BLINKY_NAIL_DRESS_RAPTURE, from(ss).take(ss.blinkyStretchedSprites()).end(),
            ANIM_BLINKY_PATCHED,            from(ss).take(ss.blinkyPatchedSprites()).frameTicks(4).endless(),
            ANIM_BLINKY_NAKED,              from(ss).take(ss.blinkyNakedSprites()).frameTicks(4).endless()
        ));
        //TODO start animations when selected
        Stream.of(ANIM_GHOST_EYES, ANIM_GHOST_FRIGHTENED, ANIM_GHOST_FLASHING).map(this::animation).forEach(Animation::play);
    }

    @Override
    public void select(String id, int frameIndex) {
        super.select(id, frameIndex);
        if (ANIM_GHOST_NUMBER.equals(id)) {
            animation(ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
        if (actor instanceof Ghost ghost) {
            GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
            if (isCurrentAnimationID(ANIM_GHOST_NORMAL)) {
                return gss.ghostNormalSprites(ghost.personality(), ghost.wishDir());
            }
            if (isCurrentAnimationID(ANIM_GHOST_EYES)) {
                return gss.ghostEyesSprites(ghost.wishDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}