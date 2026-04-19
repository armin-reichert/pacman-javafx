/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID {MR_PAC_MAN_MUNCHING}

    private final SpriteAnimationContainer container;

    public ArcadeMsPacMan_PacAnimations(SpriteAnimationContainer container) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        this.container = requireNonNull(container);
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Pac.AnimationID.PAC_FULL -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PACMAN_FULL))
                .build(container);

            case Pac.AnimationID.PAC_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().msPacManMunchingSprites(Direction.LEFT))
                .repeated()
                .build(container);

            case Pac.AnimationID.PAC_DYING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(SpriteID.MS_PACMAN_DYING))
                .frameTicks(8)
                .build(container);

            case AnimationID.MR_PAC_MAN_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PACMAN_MUNCHING_LEFT))
                .frameTicks(2)
                .repeated()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }
}