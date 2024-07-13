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
            infoText("No custom maps found.", "");
            return;
        }
        var cbEnable = checkBox("Enable Custom Maps");
        cbEnable.setOnAction(e -> context.actionHandler().updateCustomMaps());
        cbEnable.selectedProperty().bindBidirectional(PY_CUSTOM_MAPS_ENABLED);
        for (File mapFile : customMapsByFile.keySet().stream().sorted().toList()) {
            WorldMap map = customMapsByFile.get(mapFile);
            String mapSize = "(%dx%d)".formatted(map.numRows(), map.numCols());
            infoText(mapFile.getName(), mapSize);
        }
        var btnReload = new Button("Reload");
        btnReload.setOnAction(e -> reloadCustomMaps());
        addRow(btnReload, null);
    }
}
