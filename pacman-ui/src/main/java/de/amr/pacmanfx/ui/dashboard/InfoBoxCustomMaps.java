/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class InfoBoxCustomMaps extends InfoBox {

    private static String trimURL(String url) {
        if (url == null) return NO_INFO;
        url = URLDecoder.decode(url, StandardCharsets.UTF_8);
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash != -1) {
            return url.substring(lastSlash + 1);
        } else {
            return url.substring(0, 10); //TODO
        }
    }

    private final ObservableList<WorldMap> customMaps = FXCollections.observableArrayList();

    public InfoBoxCustomMaps(GameUI ui) {
        super(ui);
        
        TableView<WorldMap> mapsTableView = new TableView<>();
        mapsTableView.setItems(customMaps);

        mapsTableView.setPrefWidth(300);
        mapsTableView.setPrefHeight(200);

        var tcMapURL = new TableColumn<WorldMap, String>("Map");
        tcMapURL.setCellValueFactory(data -> new SimpleStringProperty(trimURL(data.getValue().url())));

        var tcMapRowCount = new TableColumn<WorldMap, Integer>("Rows");
        tcMapRowCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().numRows()).asObject());

        var tcMapColCount = new TableColumn<WorldMap, Integer>("Cols");
        tcMapColCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().numCols()).asObject());

        mapsTableView.getColumns().add(tcMapURL);
        mapsTableView.getColumns().add(tcMapRowCount);
        mapsTableView.getColumns().add(tcMapColCount);

        mapsTableView.getColumns().forEach(column -> {
            column.setSortable(false);
            column.setReorderable(false);
        });

        addRow(mapsTableView);
        updateCustomMapList();

        ui.directoryWatchdog().addEventListener(eventList -> {
            Logger.info("Custom map change(s) detected: {}",
                eventList.stream()
                    .map(watchEvent -> String.format("%s: '%s'", watchEvent.kind(), watchEvent.context()))
                    .toList());
            updateCustomMapList();
        });
    }

    private void updateCustomMapList() {
        customMaps.clear();
        File[] mapFiles = ui.gameContext().customMapDir().listFiles((dir, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.error("An error occurred accessing custom map directory {}", ui.gameContext().customMapDir());
            return;
        }
        if (mapFiles.length == 0) {
            Logger.info("No custom maps found");
        } else {
            Logger.info("{} custom map(s) found", mapFiles.length);
        }
        for (File file : mapFiles) {
            try {
                WorldMap worldMap = WorldMap.mapFromFile(file);
                customMaps.add(worldMap);
                Logger.info("Custom map loaded from file {}", file);
            } catch (IOException x) {
                Logger.error(x);
                Logger.error("Could not read custom map from file {}", file);
            }
        }
    }
}