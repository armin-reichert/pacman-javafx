/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui.PacManGamesUI;

/**
 * User interface for all Pac-Man game variants with a 3D play scene. All others scenes are in 2D.
 * </p>
 * <p>The separation of the 2D-only UI into its own project originally was done to create a
 * <a href="https://github.com/armin-reichert/webfx-pacman">WebFX-version</a> of the game.
 * WebFX is a technology that transpiles a JavaFX application into a (very small) GWT application that runs inside
 * any browser supported by GWT. Unfortunately, WebFX has no support for JavaFX 3D so far.</p>
 *
 * @author Armin Reichert
 */
public class PacManGamesUI_3D extends PacManGamesUI {

    public PacManGamesUI_3D() {
        assets().addAssets3D();
    }

    @Override
    protected void createGameView() {
        gameView = new GameView3D(this);
    }
}