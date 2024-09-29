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
        super.init(context);

        labelAndValue("F1, Alt+B", "Dashboard On/Off");
        labelAndValue("F2", "Pic-in-Pic On/Off");
        labelAndValue("F3", "Reboot");
        labelAndValue("Alt+A", "Autopilot On/Off");
        labelAndValue("Alt+C", "Play Cut-Scenes");
        labelAndValue("Alt+E", "Eat All Pellets");
        labelAndValue("Alt+I", "Player Immunity On/Off");
        labelAndValue("Alt+M", "Mute On/Off");
        labelAndValue("Alt+L", "Add 3 Lives");
        labelAndValue("Alt+N", "Next Level");
        labelAndValue("Alt+X", "Kill Hunting Ghosts");
        labelAndValue("Alt+3", "3D Play Scene On/Off");
        labelAndValue("P", "Pause On/Off");
        labelAndValue("Shift+P, SPACE", "Single Step");
        labelAndValue("Q", "Return to Intro");

        labelAndValue("Start Screen Keys:", "");
        labelAndValue("V, RIGHT, LEFT", "Switch Game Variant");
        labelAndValue("1", "Start Playing");
        labelAndValue("5", "Add Credit");
    }
}