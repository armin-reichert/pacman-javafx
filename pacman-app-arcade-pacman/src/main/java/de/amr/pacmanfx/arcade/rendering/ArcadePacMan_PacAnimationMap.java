/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ANIM_BIG_PAC_MAN;
import static de.amr.pacmanfx.arcade.rendering.SpriteID.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;

public class ArcadePacMan_PacAnimationMap extends SpriteAnimationMap<SpriteID> {

    public ArcadePacMan_PacAnimationMap(ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
    }

    @Override
    protected SpriteAnimation createAnimation(String id) {
        return switch (id) {
            case ANIM_PAC_MUNCHING -> SpriteAnimation.build().of(pacMunchingSprites(Direction.LEFT)).forever();
            case ANIM_PAC_DYING    -> SpriteAnimation.build().of(spriteSheet().spriteSeq(PACMAN_DYING)).frameTicks(8).once();
            case ANIM_BIG_PAC_MAN  -> SpriteAnimation.build().of(spriteSheet().spriteSeq(PACMAN_BIG)).frameTicks(3).forever();
            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac && isCurrentAnimationID(ANIM_PAC_MUNCHING)) {
            currentAnimation().setSprites(pacMunchingSprites(pac.moveDir()));
        }
    }

    private Sprite[] pacMunchingSprites(Direction dir) {
        return switch (dir) {
            case RIGHT -> spriteSheet().spriteSeq(PACMAN_MUNCHING_RIGHT);
            case LEFT  -> spriteSheet().spriteSeq(PACMAN_MUNCHING_LEFT);
            case UP    -> spriteSheet().spriteSeq(PACMAN_MUNCHING_UP);
            case DOWN  -> spriteSheet().spriteSeq(PACMAN_MUNCHING_DOWN);
        };
    }
}