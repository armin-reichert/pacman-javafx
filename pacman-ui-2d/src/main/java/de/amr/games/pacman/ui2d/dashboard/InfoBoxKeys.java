/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui.GameContext;

/**
 * Keyboard shortcuts.
 *
 * @author Armin Reichert
 */
public class InfoBoxKeys extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        addLabeledValue("F1, Alt+B", "Dashboard On/Off");
        addLabeledValue("F2", "Pic-in-Pic On/Off");
        addLabeledValue("F3", "Reboot");
        addLabeledValue("Alt+A", "Autopilot On/Off");
        addLabeledValue("Alt+C", "Play Cut-Scenes");
        addLabeledValue("Alt+E", "Eat All Pellets");
        addLabeledValue("Alt+I", "Player Immunity On/Off");
        addLabeledValue("Alt+M", "Mute On/Off");
        addLabeledValue("Alt+L", "Add 3 Lives");
        addLabeledValue("Alt+N", "Next Level");
        addLabeledValue("Alt+X", "Kill Hunting Ghosts");
        addLabeledValue("Alt+3", "3D Play Scene On/Off");
        addLabeledValue("P", "Pause On/Off");
        addLabeledValue("Shift+P, SPACE", "Single Step");
        addLabeledValue("Q", "Return to Intro");

        addLabeledValue("Start Screen Keys:", "");
        addLabeledValue("V, RIGHT, LEFT", "Switch Game Variant");
        addLabeledValue("1", "Start Playing");
        addLabeledValue("5", "Add Credit");
    }
}