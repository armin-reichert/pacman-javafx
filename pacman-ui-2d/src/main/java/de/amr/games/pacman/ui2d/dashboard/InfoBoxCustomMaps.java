/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.pacmanxxl.MapSelectionMode;
import de.amr.games.pacman.model.pacmanxxl.PacManXXLGameModel;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;

/**
 * @author Armin Reichert
 */
public class InfoBoxCustomMaps extends InfoBox {

    record MapInfo(String fileName, int numRows, int numCols) {}

    private TableView<MapInfo> mapTable;
    private ComboBox<MapSelectionMode> comboMapSelectionMode;

    @Override
    public void init(GameContext context) {
        this.context = context;

        comboMapSelectionMode = addComboBoxRow("Map Selection", MapSelectionMode.values());
        comboMapSelectionMode.setOnAction(e -> {
            PacManXXLGameModel xxlGame = context.gameController().gameModel(GameVariant.PACMAN_XXL);
            xxlGame.setMapSelectionMode(comboMapSelectionMode.getValue());
            xxlGame.loadCustomMaps();
            reloadCustomMapsAndUpdateTableView();
        });

        mapTable = new TableView<>();
        mapTable.setPrefHeight(200);
        var col1 = new TableColumn<MapInfo, String>("Map File");
        col1.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fileName));
        var col2 = new TableColumn<MapInfo, Integer>("Rows");
        col2.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().numRows).asObject());
        var col3 = new TableColumn<MapInfo, Integer>("Cols");
        col3.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().numCols).asObject());
        mapTable.getColumns().add(col1);
        mapTable.getColumns().add(col2);
        mapTable.getColumns().add(col3);
        addRow(mapTable);

        reloadCustomMapsAndUpdateTableView();
    }

    @Override
    public void update() {
        super.update();
        PacManXXLGameModel xxlGame = context.gameController().gameModel(GameVariant.PACMAN_XXL);
        comboMapSelectionMode.getSelectionModel().select(xxlGame.mapSelectionMode());
    }

    private void reloadCustomMapsAndUpdateTableView() {
        ObservableList<MapInfo> items = FXCollections.observableArrayList();
        PacManXXLGameModel xxlGame = context.gameController().gameModel(GameVariant.PACMAN_XXL);
        xxlGame.loadCustomMaps();
        for (File file  : xxlGame.customMapsByFile().keySet().stream().sorted().toList()) {
            WorldMap map = xxlGame.customMapsByFile().get(file);
            items.add(new MapInfo(file.getName(), map.terrain().numRows(), map.terrain().numCols()));
        }
        mapTable.setItems(items);
    }
}
