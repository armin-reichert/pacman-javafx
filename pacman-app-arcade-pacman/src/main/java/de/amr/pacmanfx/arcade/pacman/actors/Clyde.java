/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.actors;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;

public class Clyde extends Ghost {

    protected Clyde() {
        reset();
    }

    @Override
    public void onFoodEaten(GameLevel gameLevel) {
    }

    @Override
    public void onPacKilled(GameLevel gameLevel) {
    }

    @Override
    public String name() {
        return "Clyde";
    }

    @Override
    public byte personality() {
        return ORANGE_GHOST_POKEY;
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        // Attacks directly or retreats towards scatter target if Pac is near
        return tile().euclideanDist(gameLevel.pac().tile()) < 8
            ? gameLevel.worldMap().terrainLayer().ghostScatterTile(personality())
            : gameLevel.pac().tile();
    }
}
