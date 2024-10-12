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

        labeledValue("F1, Alt+B", "Dashboard On/Off");
        labeledValue("F2", "Pic-in-Pic On/Off");
        labeledValue("F3", "Reboot");
        labeledValue("Alt+A", "Autopilot On/Off");
        labeledValue("Alt+C", "Play Cut-Scenes");
        labeledValue("Alt+E", "Eat All Pellets");
        labeledValue("Alt+I", "Player Immunity On/Off");
        labeledValue("Alt+M", "Mute On/Off");
        labeledValue("Alt+L", "Add 3 Lives");
        labeledValue("Alt+N", "Next Level");
        labeledValue("Alt+X", "Kill Hunting Ghosts");
        labeledValue("Alt+3", "3D Play Scene On/Off");
        labeledValue("P", "Pause On/Off");
        labeledValue("Shift+P, SPACE", "Single Step");
        labeledValue("Q", "Return to Intro");

        labeledValue("Start Screen Keys:", "");
        labeledValue("V, RIGHT, LEFT", "Switch Game Variant");
        labeledValue("1", "Start Playing");
        labeledValue("5", "Add Credit");
    }
}