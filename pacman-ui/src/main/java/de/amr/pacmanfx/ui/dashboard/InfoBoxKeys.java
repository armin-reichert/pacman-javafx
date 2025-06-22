/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameAction;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;
import java.util.Map;

import static de.amr.pacmanfx.ui.PacManGames.theUI;

/**
 * Keyboard shortcuts.
 */
public class InfoBoxKeys extends InfoBox {

    public void init() {
        super.init();
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

    @Override
    public void update() {
        clearGrid();
        addLabeledValue("-- General", "");
        addEntries(theUI().currentView().actionBindings());
        addLabeledValue("-- Scene specific", "");
        theUI().currentGameScene().ifPresent(gameScene -> addEntries(gameScene.actionBindings()));
    }

    private void addEntries(Map<KeyCombination, GameAction> bindings) {
        bindings.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getDisplayText())).forEach(entry -> {
            KeyCombination keyCombination = entry.getKey();
            GameAction action = entry.getValue();
            addRow(keyCombination.getDisplayText(), createLabel(action.name()));
        });
    }
}