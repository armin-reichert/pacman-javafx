/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.CustomMapSelectionMode;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.tinylog.Logger;

import java.io.File;
import java.util.Map;

import static de.amr.games.pacman.ui2d.GlobalProperties2d.PY_MAP_SELECTION_MODE;


/**
 * @author Armin Reichert
 */
public class InfoBoxCustomMaps extends InfoBox {

    record MapInfo(String fileName, int numRows, int numCols) {}

    private TableView<MapInfo> mapTable;
    private ComboBox<CustomMapSelectionMode> comboMapSelectionMode;

    @Override
    public void init(GameContext context) {
        super.init(context);

        comboMapSelectionMode = addComboBox("Map Selection", CustomMapSelectionMode.values());
        comboMapSelectionMode.setOnAction(e -> PY_MAP_SELECTION_MODE.set(comboMapSelectionMode.getValue()));
        comboMapSelectionMode.getSelectionModel().select(PY_MAP_SELECTION_MODE.get());

        mapTable = new TableView<>();
        mapTable.setPrefHeight(200);
        addRow(mapTable);

        var tcMapFile = new TableColumn<MapInfo, String>("Map File");
        tcMapFile.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fileName));

        var tcMapRowCount = new TableColumn<MapInfo, Integer>("Rows");
        tcMapRowCount.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().numRows).asObject());

        var tcMapColCount = new TableColumn<MapInfo, Integer>("Cols");
        tcMapColCount.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().numCols).asObject());

        mapTable.getColumns().add(tcMapFile);
        mapTable.getColumns().add(tcMapRowCount);
        mapTable.getColumns().add(tcMapColCount);
        mapTable.getColumns().forEach(column -> {
            column.setSortable(false);
            column.setReorderable(false);
        });

        expandedProperty().addListener((py,ov,nv) -> {
            if (nv) {
                updateTableView();
                Logger.info("Custom map table updated");
            }
        });
    }

    public void updateTableView() {
        ObservableList<MapInfo> items = FXCollections.observableArrayList();
        Map<File, WorldMap> customMaps = context.game().customMapsByFile();
        for (File file : customMaps.keySet().stream().sorted().toList()) {
            WorldMap worldMap = customMaps.get(file);
            items.add(new MapInfo(file.getName(), worldMap.numRows(), worldMap.numCols()));
        }
        mapTable.setItems(items);
    }
}