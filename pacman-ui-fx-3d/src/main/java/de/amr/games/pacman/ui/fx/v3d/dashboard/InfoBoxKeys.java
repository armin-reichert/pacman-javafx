/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.scene3d.Perspective;

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_3D_PERSPECTIVE;

/**
 * Keyboard shortcuts.
 *
 * @author Armin Reichert
 */
public class InfoBoxKeys extends InfoBox {

    public InfoBoxKeys(Theme theme, String title) {
        super(theme, title);

        addInfo("F1, Alt+B", "Dashboard On/Off");
        addInfo("F2", "Picture-in-Picture");
        addInfo("F3", "Reboot");
        addInfo("Alt+A", "Autopilot On/Off");
        addInfo("Alt+C", "Play Cut-Scenes")
            .available(() -> context.gameState() == GameState.INTRO);
        addInfo("Alt+E", "Eat All Simple Pellets")
            .available(() -> context.game().isPlaying());
        addInfo("Alt+I", "Player Immunity On/Off");
        addInfo("Alt+L", "Add 3 Player Lives")
            .available(() -> context.game().isPlaying());
        addInfo("Alt+N", "Next Level")
            .available(() -> context.game().isPlaying());
        addInfo("Alt+X", "Kill Hunting Ghosts")
            .available(() -> context.game().isPlaying());
        addInfo("Alt+LEFT", () -> Perspective.previous(PY_3D_PERSPECTIVE.get()).name())
            .available(this::isCurrentGameScene3D);
        addInfo("Alt+RIGHT", () -> Perspective.next(PY_3D_PERSPECTIVE.get()).name())
            .available(this::isCurrentGameScene3D);
        addInfo("Alt+3", "3D Play Scene On/Off");
        addInfo("P", "Pause On/Off");
        addInfo("Shift+P, SPACE", "Single Step");
        addInfo("Q", "Quit, Return to Intro Scene");
        addInfo("V", "Switch Game Variant");
        addInfo("1", "Start Playing (Credit Required)");
        addInfo("5", "Add Credit");
    }
}