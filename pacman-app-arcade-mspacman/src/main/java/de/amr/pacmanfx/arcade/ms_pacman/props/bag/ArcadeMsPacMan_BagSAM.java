/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.props.bag;

import de.amr.basics.ui.spriteanim.*;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

public class ArcadeMsPacMan_BagSAM extends LazySAM {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

    public ArcadeMsPacMan_BagSAM(SpriteAnimationSystem spriteAnimationSystem) {
        setFactory(id -> switch (id) {
            case CommonSpriteAnimationID.JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(JUNIOR_PAC))
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.BAG -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(BLUE_BAG))
                .build(spriteAnimationSystem);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        });
    }
}
