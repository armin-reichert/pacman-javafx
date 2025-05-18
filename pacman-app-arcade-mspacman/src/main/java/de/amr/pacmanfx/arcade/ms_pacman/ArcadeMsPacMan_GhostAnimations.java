/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostAnimations;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationSet implements GhostAnimations {

    public ArcadeMsPacMan_GhostAnimations(GameSpriteSheet ss, byte personality) {
        requireNonNull(ss);
        requireValidGhostPersonality(personality);
        add(ANIM_GHOST_NORMAL,     from(ss).take(ss.ghostNormalSprites(personality, Direction.LEFT)).frameTicks(8).endless());
        add(ANIM_GHOST_FRIGHTENED, from(ss).take(ss.ghostFrightenedSprites()).frameTicks(8).endless());
        add(ANIM_GHOST_FLASHING,   from(ss).take(ss.ghostFlashingSprites()).frameTicks(7).endless());
        add(ANIM_GHOST_EYES,       from(ss).take(ss.ghostEyesSprites(Direction.LEFT)).end());
        add(ANIM_GHOST_NUMBER,     from(ss).take(ss.ghostNumberSprites()).end());
        //TODO start animations when selected
        animation(ANIM_GHOST_EYES).play();
        animation(ANIM_GHOST_FRIGHTENED).play();
        animation(ANIM_GHOST_FLASHING).play();
    }

    @Override
    public void select(String id, int frameIndex) {
        super.select(id, frameIndex);
        if (ANIM_GHOST_NUMBER.equals(id)) {
            animation(ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet ss, Actor actor) {
        if (actor instanceof Ghost ghost) {
            GameSpriteSheet gss = (GameSpriteSheet) ss;
            switch (currentAnimationID) {
                case ANIM_GHOST_NORMAL -> {
                    return gss.ghostNormalSprites(ghost.personality(), ghost.wishDir());
                }
                case ANIM_GHOST_EYES -> {
                    return gss.ghostEyesSprites(ghost.wishDir());
                }
            }
        }
        return super.selectedSprites(ss, actor);
    }
}