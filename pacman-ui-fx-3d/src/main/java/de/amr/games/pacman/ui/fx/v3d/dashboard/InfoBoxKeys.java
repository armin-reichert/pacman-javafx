/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

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
        addInfo("Alt+C", "Play Cut-Scenes");
        addInfo("Alt+E", "Eat All Simple Pellets");
        addInfo("Alt+I", "Player Immunity On/Off");
        addInfo("Alt+L", "Add 3 Player Lives");
        addInfo("Alt+N", "Next Level");
        addInfo("Alt+X", "Kill Hunting Ghosts");
        addInfo("Alt+LEFT", () -> Perspective.previous(PY_3D_PERSPECTIVE.get()).name());
        addInfo("Alt+RIGHT", () -> Perspective.next(PY_3D_PERSPECTIVE.get()).name());
        addInfo("Alt+3", "3D Play Scene On/Off");
        addInfo("P", "Pause On/Off");
        addInfo("Shift+P, SPACE", "Single Step");
        addInfo("Q", "Return to Intro");
        addInfo("V", "Switch Game Variant");
        addInfo("1", "Start Playing (Credit?)");
        addInfo("5", "Add Credit");
    }
}