/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID {
        ANIM_BIG_PAC_MAN,
    }

    private final SpriteAnimationManager timer;
    
    public ArcadePacMan_PacAnimations(SpriteAnimationManager timer, ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
        this.timer = requireNonNull(timer);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case Pac.AnimationID.PAC_FULL -> builder(timer)
                .singleSprite(spriteSheet.sprite(SpriteID.PACMAN_FULL))
                .initiallyStopped()
                .build();

            case Pac.AnimationID.PAC_MUNCHING -> builder(timer)
                .sprites(pacMunchingSprites(Direction.LEFT))
                .repeated()
                .build();

            case Pac.AnimationID.PAC_DYING -> builder(timer)
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_DYING))
                .frameTicks(8)
                .build();

            case AnimationID.ANIM_BIG_PAC_MAN -> builder(timer)
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_BIG))
                .frameTicks(3)
                .repeated()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac && isSelected(Pac.AnimationID.PAC_MUNCHING)) {
            currentAnimation().setSprites(pacMunchingSprites(pac.moveDir()));
        }
    }

    private RectShort[] pacMunchingSprites(Direction dir) {
        return switch (dir) {
            case RIGHT -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_RIGHT);
            case LEFT  -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_LEFT);
            case UP    -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_UP);
            case DOWN  -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_DOWN);
        };
    }
}