package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameAction;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;
import java.util.Map;

import static de.amr.pacmanfx.ui.PacManGames.theUI;

public class InfoBoxGlobalKeyShortcuts extends InfoBox {

    @Override
    public void update() {
        clearGrid();
        addEntries(theUI().currentView().actionBindings());
    }

    private void addEntries(Map<KeyCombination, GameAction> bindings) {
        bindings.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getDisplayText())).forEach(entry -> {
            KeyCombination keyCombination = entry.getKey();
            GameAction action = entry.getValue();
            addRow(keyCombination.getDisplayText(), createLabel(action.name()));
        });
    }
}
