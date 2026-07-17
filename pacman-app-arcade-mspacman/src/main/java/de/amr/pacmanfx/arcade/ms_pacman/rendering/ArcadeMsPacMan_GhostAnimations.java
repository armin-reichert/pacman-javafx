/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.Identifier;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static de.amr.pacmanfx.core.Validations.requireValidGhostPersonality;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    private final SpriteAnimationContainer container;
    private final byte personality;

    public ArcadeMsPacMan_GhostAnimations(SpriteAnimationContainer container, byte personality) {
        super(ArcadeMsPacMan_SpriteSheet.instance());

        this.container = requireNonNull(container);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    public SpriteAnimationContainer container() {
        return container;
    }

    @Override
    protected SpriteAnimation createAnimation(Identifier animationID) {

        return switch (animationID) {
            case ArcadePacMan_AnimationID.GHOST_NORMAL -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_FRIGHTENED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_FLASHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_EYES -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_POINTS -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(Identifier animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (ArcadePacMan_AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(ArcadePacMan_AnimationID.GHOST_POINTS).setFrame(frameIndex);
        }
    }
}