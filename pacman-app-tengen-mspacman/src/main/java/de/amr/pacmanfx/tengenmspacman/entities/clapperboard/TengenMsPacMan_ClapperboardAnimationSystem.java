/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.clapperboard;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.clapperboard.comp.ClapperboardStateComp;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;

import java.util.Optional;

public class TengenMsPacMan_ClapperboardAnimationSystem {

    public static Optional<RectShort> sprite(Clapperboard clapperboard) {
        final ClapperboardStateComp state = clapperboard.state();
        final RectShort[] sprites = TengenMsPacMan_SpriteSheet.instance().findSprites(SpriteID.CLAPPERBOARD);
        return switch (state.state()) {
            case WIDE_OPEN -> Optional.of(sprites[0]);
            case OPEN -> Optional.of(sprites[1]);
            case CLOSED -> Optional.of(sprites[2]);
        };
    }
}
