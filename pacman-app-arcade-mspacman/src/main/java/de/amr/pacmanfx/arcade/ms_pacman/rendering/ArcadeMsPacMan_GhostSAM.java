/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.LazySAM;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.model.GhostPersonality;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GhostSAM extends LazySAM {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

    private final GhostPersonality personality;

    public ArcadeMsPacMan_GhostSAM(SpriteAnimationContainer container, GhostPersonality personality) {
        this.personality = requireNonNull(personality);
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimationContainer container) {

        return switch (animationID) {
            case CommonSpriteAnimationID.GHOST_NORMAL -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_FRIGHTENED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_FLASHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_EYES -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.ghostEyesSprite(Direction.LEFT))
                .build(container);

            case CommonSpriteAnimationID.GHOST_POINTS -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public void setAnimationFrame(Named name, int frameIndex) {
        super.setAnimationFrame(name, frameIndex);
        if (CommonSpriteAnimationID.GHOST_POINTS.equals(name)) {
            animation(CommonSpriteAnimationID.GHOST_POINTS).setFrame(frameIndex);
        }
    }
}