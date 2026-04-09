/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

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

    private final SpriteAnimationManager manager;
    
    public ArcadePacMan_PacAnimations(SpriteAnimationManager manager, ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
        this.manager = requireNonNull(manager);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case Pac.AnimationID.PAC_FULL -> builder(manager)
                .sprite(spriteSheet.sprite(SpriteID.PACMAN_FULL))
                .initiallyStopped()
                .build();

            // Renderer draws sprites depending on Pac-Man move direction!
            case Pac.AnimationID.PAC_MUNCHING -> builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_LEFT))
                .repeated()
                .build();

            case Pac.AnimationID.PAC_DYING -> builder(manager)
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_DYING))
                .frameTicks(8)
                .build();

            case AnimationID.ANIM_BIG_PAC_MAN -> builder(manager)
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