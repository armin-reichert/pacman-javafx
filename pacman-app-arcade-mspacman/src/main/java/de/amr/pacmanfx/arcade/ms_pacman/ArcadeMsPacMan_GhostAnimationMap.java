/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_SpriteSheet.SpriteID;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_SpriteSheet.SpriteID.*;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_SpriteSheet.getSprites;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createAnimation;

public class ArcadeMsPacMan_GhostAnimationMap extends SpriteAnimationMap<RectArea> {

    public ArcadeMsPacMan_GhostAnimationMap(ArcadeMsPacMan_SpriteSheet ss, byte personality) {
        super(ss);
        requireValidGhostPersonality(personality);
        set(ANIM_GHOST_NORMAL,     createAnimation().sprites(ghostNormalSprites(personality, Direction.LEFT)).frameTicks(8).endless());
        set(ANIM_GHOST_FRIGHTENED, createAnimation().sprites(getSprites(SpriteID.GHOST_FRIGHTENED)).frameTicks(8).endless());
        set(ANIM_GHOST_FLASHING,   createAnimation().sprites(getSprites(GHOST_FLASHING)).frameTicks(7).endless());
        set(ANIM_GHOST_EYES,       createAnimation().sprites(ss.ghostEyesSprites(Direction.LEFT)).end());
        set(ANIM_GHOST_NUMBER,     createAnimation().sprites(ss.ghostNumberSprites()).end());
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
                case ANIM_GHOST_NORMAL -> currentAnimation().setSprites(ghostNormalSprites(ghost.personality(), ghost.wishDir()));
                case ANIM_GHOST_EYES   -> currentAnimation().setSprites(spriteSheet().ghostEyesSprites(ghost.wishDir()));
            }
        }
    }

    private RectArea[] ghostNormalSprites(byte id, Direction dir) {
        return getSprites(switch (id) {
            case 0 -> switch (dir) {
                case RIGHT -> RED_GHOST_RIGHT;
                case LEFT -> RED_GHOST_LEFT;
                case UP -> RED_GHOST_UP;
                case DOWN -> RED_GHOST_DOWN;
            };
            case 1 -> switch (dir) {
                case RIGHT -> PINK_GHOST_RIGHT;
                case LEFT -> PINK_GHOST_LEFT;
                case UP -> PINK_GHOST_UP;
                case DOWN -> PINK_GHOST_DOWN;
            };
            case 2 -> switch (dir) {
                case RIGHT -> CYAN_GHOST_RIGHT;
                case LEFT -> CYAN_GHOST_LEFT;
                case UP -> CYAN_GHOST_UP;
                case DOWN -> CYAN_GHOST_DOWN;
            };
            case 3 -> switch (dir) {
                case RIGHT -> ORANGE_GHOST_RIGHT;
                case LEFT -> ORANGE_GHOST_LEFT;
                case UP -> ORANGE_GHOST_UP;
                case DOWN -> ORANGE_GHOST_DOWN;
            };
            default -> throw new IllegalArgumentException("Illegal ghost ID " + id);
        });
    }
}