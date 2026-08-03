/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.Named;
import de.amr.basics.spriteanim.LazySpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static java.util.Objects.requireNonNull;

public class SpritesheetAnimationMap<ID extends Named> extends LazySpriteAnimationMap {

    protected final SpriteSheet<ID> spriteSheet;

    public SpritesheetAnimationMap(SpriteSheet<ID> spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    public SpriteSheet<ID> spriteSheet() { return spriteSheet; }
}
