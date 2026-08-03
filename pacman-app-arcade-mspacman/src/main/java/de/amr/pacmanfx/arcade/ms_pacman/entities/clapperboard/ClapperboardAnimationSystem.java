/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities.clapperboard;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.model.entities.clapperboard.Clapperboard;
import de.amr.pacmanfx.core.model.entities.clapperboard.ClapperboardStateComp;

import java.util.Optional;

public class ClapperboardAnimationSystem {

    public static Optional<RectShort> sprite(Clapperboard clapperboard) {
        final ClapperboardStateComp state = clapperboard.state();
        final RectShort[] sprites = ArcadeMsPacMan_SpriteSheet.instance().findSprites(SpriteID.CLAPPERBOARD);
        return switch (state.state()) {
            case WIDE_OPEN -> Optional.of(sprites[0]);
            case OPEN -> Optional.of(sprites[1]);
            case CLOSED -> Optional.of(sprites[2]);
        };
    }
}
