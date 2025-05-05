/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.allgames;

import de.amr.games.pacman.uilib.Ufx;

/**
 * Wraps application start, if an exception occurs, the stacktrace will be stored in file <code>oh_shit.txt</code>.
 */
public class Main {
    public static void main(String[] args) {
        Ufx.startApplication(PacManGames3dApp.class, args);
    }
}