/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.Named;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    private final SpriteAnimationSet animationSet;

    public ArcadeMsPacMan_PacAnimations(SpriteAnimationSet animationSet) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        this.animationSet = requireNonNull(animationSet);
    }

    @Override
    protected SpriteAnimation createAnimation(Named animationID) {
        return switch (animationID) {
            case ArcadePacMan_AnimationID.PAC_FULL -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PACMAN_FULL))
                .build(animationSet);

            case ArcadePacMan_AnimationID.PAC_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().msPacManMunchingSprites(Direction.LEFT))
                .repeated()
                .build(animationSet);

            case ArcadePacMan_AnimationID.PAC_DYING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(SpriteID.MS_PACMAN_DYING))
                .frameTicks(8)
                .build(animationSet);

            case ArcadeMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PACMAN_MUNCHING_LEFT))
                .frameTicks(2)
                .repeated()
                .build(animationSet);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }
}