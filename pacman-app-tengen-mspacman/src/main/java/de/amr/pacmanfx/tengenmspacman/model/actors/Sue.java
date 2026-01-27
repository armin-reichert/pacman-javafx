/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;

public class Sue extends Ghost {

    public Sue() {
        super(ORANGE_GHOST_POKEY, "Sue");
        reset();
    }

    @Override
    public void onPacKilled(GameLevel gameLevel) {}

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        return tile().euclideanDist(gameLevel.pac().tile()) < 8
            ? gameLevel.worldMap().terrainLayer().ghostScatterTile(personality())
            : gameLevel.pac().tile();
    }
}
