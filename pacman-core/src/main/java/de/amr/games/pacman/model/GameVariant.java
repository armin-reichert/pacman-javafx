/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Game models/variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameVariant {
    MS_PACMAN, PACMAN, PACMAN_XXL;

    private GameModel game;

    public void setGame(GameModel game) {
        this.game = checkNotNull(game);
    }

    public GameModel game() {
        return game;
    }
}