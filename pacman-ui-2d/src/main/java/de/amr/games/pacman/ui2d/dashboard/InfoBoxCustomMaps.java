/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.arcade.pacman_xxl.MapSelectionMode;
import de.amr.games.pacman.arcade.pacman_xxl.PacManGameXXL;
import de.amr.games.pacman.ui.GameContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.games.pacman.ui2d.GlobalProperties2d.PY_MAP_SELECTION_MODE;


/**
 * @author Armin Reichert
 */
public class InfoBoxCustomMaps extends InfoBox {

    record MapInfo(String fileName, int numRows, int numCols) {}

    private TableView<MapInfo> mapTable;
    private ComboBox<MapSelectionMode> comboMapSelectionMode;

    @Override
    public void init(GameContext context) {
        super.init(context);

        comboMapSelectionMode = addComboBox("Map Selection", MapSelectionMode.values());
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
        PacManGameXXL xxlGame = context.gameController().gameModel(GameVariant.PACMAN_XXL);
        ObservableList<MapInfo> items = FXCollections.observableArrayList();
        for (File file  : xxlGame.customMapsByFile().keySet().stream().sorted().toList()) {
            WorldMap map = xxlGame.customMapsByFile().get(file);
            items.add(new MapInfo(file.getName(), map.terrain().numRows(), map.terrain().numCols()));
        }
        mapTable.setItems(items);
    }
}