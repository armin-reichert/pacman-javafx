/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ANIM_BIG_PAC_MAN;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createSpriteAnimation;

public class ArcadePacMan_PacAnimationMap extends SpriteAnimationMap<RectArea> {

    public ArcadePacMan_PacAnimationMap(ArcadePacMan_SpriteSheet ss) {
        super(ss);
        set(ANIM_PAC_MUNCHING, createSpriteAnimation().sprites(ss.pacMunchingSprites(Direction.LEFT)).endless());
        set(ANIM_PAC_DYING,    createSpriteAnimation().sprites(ss.pacDyingSprites()).frameTicks(8).end());
        set(ANIM_BIG_PAC_MAN,      createSpriteAnimation().sprites(ss.bigPacManSprites()).frameTicks(3).endless());
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    public SpriteAnimation animation(String id) {
        return super.animation(id);
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac && isCurrentAnimationID(ANIM_PAC_MUNCHING)) {
            currentAnimation().setSprites(spriteSheet().pacMunchingSprites(pac.moveDir()));
        }
    }
}