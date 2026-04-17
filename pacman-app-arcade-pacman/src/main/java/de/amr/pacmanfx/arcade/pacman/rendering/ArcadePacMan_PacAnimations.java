/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationDriver;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID {
        ANIM_BIG_PAC_MAN,
    }

    private final SpriteAnimationDriver manager;
    
    public ArcadePacMan_PacAnimations(SpriteAnimationDriver manager, ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
        this.manager = requireNonNull(manager);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case Pac.AnimationID.PAC_FULL -> SpriteAnimationBuilder.builder(manager)
                .singleSprite(spriteSheet.sprite(SpriteID.PACMAN_FULL))
                .initiallyStopped()
                .build();

            // Renderer draws sprites depending on Pac-Man move direction!
            case Pac.AnimationID.PAC_MUNCHING -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().pacMunchingSprites(Direction.LEFT))
                .repeated()
                .build();

            case Pac.AnimationID.PAC_DYING -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_DYING))
                .frameTicks(8)
                .build();

            case AnimationID.ANIM_BIG_PAC_MAN -> SpriteAnimationBuilder.builder(manager)
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
}