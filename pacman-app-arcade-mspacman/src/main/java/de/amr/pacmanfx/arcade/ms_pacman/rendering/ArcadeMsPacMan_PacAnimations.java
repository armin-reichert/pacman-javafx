/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder.builder;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID { PAC_MAN_MUNCHING }

    private final SpriteAnimationManager manager;
    
    public ArcadeMsPacMan_PacAnimations(SpriteAnimationManager spriteAnimationManager) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        this.manager = requireNonNull(spriteAnimationManager);
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Pac.AnimationID.PAC_FULL -> builder(manager)
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PACMAN_FULL))
                .build();

            case Pac.AnimationID.PAC_MUNCHING -> builder(manager)
                .sprites(spriteSheet.sprites(SpriteID.MS_PACMAN_MUNCHING_LEFT))
                .repeated()
                .build();

            case Pac.AnimationID.PAC_DYING -> builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.MS_PACMAN_DYING))
                .frameTicks(8)
                .build();

            case AnimationID.PAC_MAN_MUNCHING -> builder(manager)
                .sprites(spriteSheet.sprites(SpriteID.MR_PACMAN_MUNCHING_LEFT))
                .frameTicks(2)
                .repeated()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }
}