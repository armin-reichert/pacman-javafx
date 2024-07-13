/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;

/**
 * Keyboard shortcuts.
 *
 * @author Armin Reichert
 */
public class InfoBoxKeys extends InfoBox {

    public void init(GameContext context) {
        this.context = context;

        addTextRow("F1, Alt+B", "Dashboard On/Off");
        addTextRow("F2", "Picture-in-Picture");
        addTextRow("F3", "Reboot");
        addTextRow("Alt+A", "Autopilot On/Off");
        addTextRow("Alt+C", "Play Cut-Scenes");
        addTextRow("Alt+E", "Eat All Simple Pellets");
        addTextRow("Alt+I", "Player Immunity On/Off");
        addTextRow("Alt+L", "Add 3 Player Lives");
        addTextRow("Alt+N", "Next Level");
        addTextRow("Alt+X", "Kill Hunting Ghosts");
//        infoText("Alt+LEFT", () -> Perspective.previous(PacManGames3dUI.PY_3D_PERSPECTIVE.get()).name());
//        infoText("Alt+RIGHT", () -> Perspective.next(PacManGames3dUI.PY_3D_PERSPECTIVE.get()).name());
        addTextRow("Alt+3", "3D Play Scene On/Off");
        addTextRow("P", "Pause On/Off");
        addTextRow("Shift+P, SPACE", "Single Step");
        addTextRow("Q", "Return to Intro");
        addTextRow("V", "Switch Game Variant");
        addTextRow("1", "Start Playing (Credit?)");
        addTextRow("5", "Add Credit");
    }
}