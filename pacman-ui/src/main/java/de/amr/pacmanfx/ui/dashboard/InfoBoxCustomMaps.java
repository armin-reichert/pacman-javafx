/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class InfoBoxCustomMaps extends InfoBox {

    private final TableView<WorldMap> mapsTableView = new TableView<>();

    private static String trimURL(String url) {
        if (url == null) return InfoText.NO_INFO;
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash != -1) {
            return url.substring(lastSlash + 1);
        } else {
            return url.substring(0, 10); //TODO
        }
    }

    public InfoBoxCustomMaps() {
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
    }

    public void setTableItems(ObservableList<WorldMap> items) {
        mapsTableView.setItems(items);
    }
}