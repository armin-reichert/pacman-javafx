/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createSpriteAnimation;

public class ArcadeMsPacMan_GhostAnimationMap extends SpriteAnimationMap {

    public ArcadeMsPacMan_GhostAnimationMap(ArcadeMsPacMan_SpriteSheet ss, byte personality) {
        super(ss);
        requireValidGhostPersonality(personality);
        set(ANIM_GHOST_NORMAL,     createSpriteAnimation().sprites(ss.ghostNormalSprites(personality, Direction.LEFT)).frameTicks(8).endless());
        set(ANIM_GHOST_FRIGHTENED, createSpriteAnimation().sprites(ss.ghostFrightenedSprites()).frameTicks(8).endless());
        set(ANIM_GHOST_FLASHING,   createSpriteAnimation().sprites(ss.ghostFlashingSprites()).frameTicks(7).endless());
        set(ANIM_GHOST_EYES,       createSpriteAnimation().sprites(ss.ghostEyesSprites(Direction.LEFT)).end());
        set(ANIM_GHOST_NUMBER,     createSpriteAnimation().sprites(ss.ghostNumberSprites()).end());
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
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
            switch (currentAnimationID) {
                case ANIM_GHOST_NORMAL -> currentAnimation().setSprites(spriteSheet().ghostNormalSprites(ghost.personality(), ghost.wishDir()));
                case ANIM_GHOST_EYES   -> currentAnimation().setSprites(spriteSheet().ghostEyesSprites(ghost.wishDir()));
            }
        }
    }
}