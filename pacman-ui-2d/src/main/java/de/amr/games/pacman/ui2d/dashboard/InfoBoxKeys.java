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

        labelledValue("F1, Alt+B", "Dashboard On/Off");
        labelledValue("F2", "Pic-in-Pic On/Off");
        labelledValue("F3", "Reboot");
        labelledValue("Alt+A", "Autopilot On/Off");
        labelledValue("Alt+C", "Play Cut-Scenes");
        labelledValue("Alt+E", "Eat All Pellets");
        labelledValue("Alt+I", "Player Immunity On/Off");
        labelledValue("Alt+M", "Mute On/Off");
        labelledValue("Alt+L", "Add 3 Lives");
        labelledValue("Alt+N", "Next Level");
        labelledValue("Alt+X", "Kill Hunting Ghosts");
        labelledValue("Alt+3", "3D Play Scene On/Off");
        labelledValue("P", "Pause On/Off");
        labelledValue("Shift+P, SPACE", "Single Step");
        labelledValue("Q", "Return to Intro");

        labelledValue("Start Screen Keys:", "");
        labelledValue("V, RIGHT, LEFT", "Switch Game Variant");
        labelledValue("1", "Start Playing");
        labelledValue("5", "Add Credit");
    }
}