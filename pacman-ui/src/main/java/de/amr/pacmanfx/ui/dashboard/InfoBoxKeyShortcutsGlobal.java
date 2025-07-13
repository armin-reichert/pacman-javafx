package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.ActionBindingMap;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameUI;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

import static de.amr.pacmanfx.ui.GameUI.theUI;

public class InfoBoxKeyShortcutsGlobal extends InfoBox {

    public InfoBoxKeyShortcutsGlobal(GameUI ui) {
        super(ui);
    }

    @Override
    public void update() {
        clearGrid();
        addEntries(theUI().currentView().actionBindingMap());
    }

    private void addEntries(ActionBindingMap actionBindingMap) {
        if (actionBindingMap.isEmpty()) {
            addRow(createLabel(InfoText.NO_INFO, false));
        }
        else {
            actionBindingMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getDisplayText())).forEach(entry -> {
                KeyCombination keyCombination = entry.getKey();
                GameAction action = entry.getValue();
                String localizedActionText = theUI().theAssets().text(action.name());
                addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(theUI())));
            });
        }
    }
}
