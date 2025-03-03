/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import static de.amr.games.pacman.ui2d.GlobalProperties2d.PY_MAP_SELECTION_MODE;

/**
 * @author Armin Reichert
 */
public class InfoBoxCustomMaps extends InfoBox {

    private TableView<WorldMap> mapsTableView;
    private ComboBox<MapSelectionMode> comboMapSelectionMode;

    @Override
    public void init(GameContext context) {
        super.init(context);

        comboMapSelectionMode = addComboBox("Map Selection", MapSelectionMode.values());
        comboMapSelectionMode.setOnAction(e -> PY_MAP_SELECTION_MODE.set(comboMapSelectionMode.getValue()));
        comboMapSelectionMode.getSelectionModel().select(PY_MAP_SELECTION_MODE.get());

        mapsTableView = new TableView<>();
        mapsTableView.setPrefHeight(200);
        addRow(mapsTableView);

        var tcMapFile = new TableColumn<WorldMap, String>("Map File");
        tcMapFile.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().url().toString()));

        var tcMapRowCount = new TableColumn<WorldMap, Integer>("Rows");
        tcMapRowCount.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().numRows()).asObject());

        var tcMapColCount = new TableColumn<WorldMap, Integer>("Cols");
        tcMapColCount.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().numCols()).asObject());

        mapsTableView.getColumns().add(tcMapFile);
        mapsTableView.getColumns().add(tcMapRowCount);
        mapsTableView.getColumns().add(tcMapColCount);
        mapsTableView.getColumns().forEach(column -> {
            column.setSortable(false);
            column.setReorderable(false);
        });
    }

    public TableView<WorldMap> getMapsTableView() {
        return mapsTableView;
    }
}