/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;

public class Sue extends Ghost {

    public Sue() {
        super(ORANGE_GHOST_POKEY);
        reset();
    }

    @Override
    public String name() {
        return "Sue";
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        return tile().euclideanDist(gameLevel.pac().tile()) < 8
            ? gameLevel.worldMap().terrainLayer().ghostScatterTile(personality())
            : gameLevel.pac().tile();
    }
}
