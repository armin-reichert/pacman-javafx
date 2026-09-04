/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities.bag;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.spriteanim.LazySAM;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationBuilder;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

public class ArcadeMsPacMan_BagSAM extends LazySAM {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

    public ArcadeMsPacMan_BagSAM(SpriteAnimContainer container) {
        setFactory(id -> switch (id) {
            case CommonSpriteAnimationID.JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(JUNIOR_PAC))
                .build(container);

            case CommonSpriteAnimationID.BAG -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(BLUE_BAG))
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        });
    }
}
