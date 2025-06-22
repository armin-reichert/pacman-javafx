package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameAction;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;
import java.util.Map;

import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames.theUI;

public class InfoBoxKeyShortcutsGlobal extends InfoBox {

    @Override
    public void update() {
        clearGrid();
        addEntries(theUI().currentView().actionBindings());
    }

    private void addEntries(Map<KeyCombination, GameAction> bindings) {
        if (bindings.isEmpty()) {
            addRow(createLabel(InfoText.NO_INFO, false));
        }
        else {
            bindings.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getDisplayText())).forEach(entry -> {
                KeyCombination keyCombination = entry.getKey();
                GameAction action = entry.getValue();
                String localizedActionText = theAssets().text(action.name());
                addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(theUI())));
            });
        }
    }
}
