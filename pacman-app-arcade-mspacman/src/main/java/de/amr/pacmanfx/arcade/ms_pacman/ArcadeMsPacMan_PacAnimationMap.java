/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createAnimation;

public class ArcadeMsPacMan_PacAnimationMap extends SpriteAnimationMap<Sprite> {

    public static final String PAC_MAN_MUNCHING = "pac_man_munching";

    public ArcadeMsPacMan_PacAnimationMap(ArcadeMsPacMan_SpriteSheet ss) {
        super(ss);
        set(ANIM_PAC_MUNCHING, createAnimation().ofSprites(msPacManMunchingSprites(Direction.LEFT)).endless());
        set(ANIM_PAC_DYING,    createAnimation().ofSprites(ss.spriteSeq(SpriteID.MS_PACMAN_DYING)).frameTicks(8).end());
        set(PAC_MAN_MUNCHING,  createAnimation().ofSprites(mrPacManMunchingSprites(Direction.LEFT)).frameTicks(2).endless());
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac) {
            switch (currentAnimationID) {
                case ANIM_PAC_MUNCHING -> currentAnimation().setSprites(msPacManMunchingSprites(pac.moveDir()));
                case PAC_MAN_MUNCHING -> currentAnimation().setSprites(mrPacManMunchingSprites(pac.moveDir()));
            }
        }
    }

    private Sprite[] msPacManMunchingSprites(Direction dir) {
        return spriteSheet().spriteSeq(switch (dir) {
            case RIGHT -> SpriteID.MS_PACMAN_MUNCHING_RIGHT;
            case LEFT -> SpriteID.MS_PACMAN_MUNCHING_LEFT;
            case UP -> SpriteID.MS_PACMAN_MUNCHING_UP;
            case DOWN -> SpriteID.MS_PACMAN_MUNCHING_DOWN;
        });
    }

    private Sprite[] mrPacManMunchingSprites(Direction dir) {
        return spriteSheet().spriteSeq(switch (dir) {
            case RIGHT -> SpriteID.MR_PACMAN_MUNCHING_RIGHT;
            case LEFT -> SpriteID.MR_PACMAN_MUNCHING_LEFT;
            case UP -> SpriteID.MR_PACMAN_MUNCHING_UP;
            case DOWN -> SpriteID.MR_PACMAN_MUNCHING_DOWN;
        });
    }
}