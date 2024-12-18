/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.apps;

import static de.amr.games.pacman.ui2d.lib.Ufx.launchApplication;

/**
 * @author Armin Reichert
 */
public class Main {
    public static void main(String[] args) {
        launchApplication(PacManGames2dApp.class, args);
    }
}