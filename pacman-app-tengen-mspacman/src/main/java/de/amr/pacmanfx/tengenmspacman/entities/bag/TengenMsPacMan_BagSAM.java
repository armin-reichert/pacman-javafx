/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.bag;

import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.LazySAM;
import de.amr.basics.ui.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;

public class TengenMsPacMan_BagSAM extends LazySAM {

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance();

    public TengenMsPacMan_BagSAM(SpriteAnimationContainer container) {
        setFactory(id -> switch (id) {
            case CommonSpriteAnimationID.BAG -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.BLUE_BAG))
                .initiallyStopped()
                .build(container);

            case CommonSpriteAnimationID.JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.JUNIOR_PAC))
                .initiallyStopped()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        });
    }
}
