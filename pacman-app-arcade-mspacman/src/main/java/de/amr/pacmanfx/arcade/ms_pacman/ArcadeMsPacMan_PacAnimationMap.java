/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createSpriteAnimation;

public class ArcadeMsPacMan_PacAnimationMap extends SpriteAnimationMap<RectArea> {

    public static final String PAC_MAN_MUNCHING = "pac_man_munching";

    public ArcadeMsPacMan_PacAnimationMap(ArcadeMsPacMan_SpriteSheet ss) {
        super(ss);
        set(ANIM_PAC_MUNCHING, createSpriteAnimation().sprites(ss.pacMunchingSprites(Direction.LEFT)).endless());
        set(ANIM_PAC_DYING,    createSpriteAnimation().sprites(ss.pacDyingSprites()).frameTicks(8).end());
        set(PAC_MAN_MUNCHING,      createSpriteAnimation().sprites(ss.mrPacManMunchingSprites(Direction.LEFT)).frameTicks(2).endless());
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac) {
            switch (currentAnimationID) {
                case ANIM_PAC_MUNCHING -> currentAnimation().setSprites(spriteSheet().pacMunchingSprites(pac.moveDir()));
                case PAC_MAN_MUNCHING -> currentAnimation().setSprites(spriteSheet().mrPacManMunchingSprites(pac.moveDir()));
            }
        }
    }
}