/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.dashboard;

import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui3d.scene.Perspective;

import static de.amr.games.pacman.ui3d.PacManGames3dUI.PY_3D_PERSPECTIVE;

/**
 * Keyboard shortcuts.
 *
 * @author Armin Reichert
 */
public class InfoBoxKeys extends InfoBox {

    public InfoBoxKeys(Theme theme, String title) {
        super(theme, title);

        infoText("F1, Alt+B", "Dashboard On/Off");
        infoText("F2", "Picture-in-Picture");
        infoText("F3", "Reboot");
        infoText("Alt+A", "Autopilot On/Off");
        infoText("Alt+C", "Play Cut-Scenes");
        infoText("Alt+E", "Eat All Simple Pellets");
        infoText("Alt+I", "Player Immunity On/Off");
        infoText("Alt+L", "Add 3 Player Lives");
        infoText("Alt+N", "Next Level");
        infoText("Alt+X", "Kill Hunting Ghosts");
        infoText("Alt+LEFT", () -> Perspective.previous(PY_3D_PERSPECTIVE.get()).name());
        infoText("Alt+RIGHT", () -> Perspective.next(PY_3D_PERSPECTIVE.get()).name());
        infoText("Alt+3", "3D Play Scene On/Off");
        infoText("P", "Pause On/Off");
        infoText("Shift+P, SPACE", "Single Step");
        infoText("Q", "Return to Intro");
        infoText("V", "Switch Game Variant");
        infoText("1", "Start Playing (Credit?)");
        infoText("5", "Add Credit");
    }
}