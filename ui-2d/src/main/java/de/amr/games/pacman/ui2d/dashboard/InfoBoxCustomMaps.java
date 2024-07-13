/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.scene.control.Button;

import java.io.File;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_CUSTOM_MAPS_ENABLED;

/**
 * @author Armin Reichert
 */
public class InfoBoxCustomMaps extends InfoBox {

    @Override
    public void init(GameContext context) {
        this.context = context;
        reloadCustomMaps();
    }

    private void reloadCustomMaps() {
        context.actionHandler().updateCustomMaps();
        clearGrid();
        var customMapsByFile = context.gameController().customMapsByFile();
        if (customMapsByFile.isEmpty()) {
            addTextRow("No custom maps found.", "");
            return;
        }
        var cbCustomMapEnabled = checkBox("Use Custom Maps", context.actionHandler()::updateCustomMaps);
        cbCustomMapEnabled.selectedProperty().bindBidirectional(PY_CUSTOM_MAPS_ENABLED);

        var btnReload = new Button("Reload");
        btnReload.setOnAction(e -> reloadCustomMaps());
        addRow(cbCustomMapEnabled, btnReload);

        for (File mapFile : customMapsByFile.keySet().stream().sorted().toList()) {
            WorldMap map = customMapsByFile.get(mapFile);
            String mapSize = "(%dx%d)".formatted(map.numRows(), map.numCols());
            addTextRow(mapFile.getName(), mapSize);
        }
    }
}
