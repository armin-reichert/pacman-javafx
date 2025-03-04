/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class InfoBoxCustomMaps extends InfoBox {

    private final TableView<WorldMap> mapsTableView = new TableView<>();

    public InfoBoxCustomMaps() {
        mapsTableView.setPrefWidth(300);
        mapsTableView.setPrefHeight(200);

        var tcMapURL = new TableColumn<WorldMap, String>("Map");
        tcMapURL.setCellValueFactory(data -> {
            String shortURL = "";
            URL url = data.getValue().url();
            if (url == null) {
                shortURL = InfoText.NO_INFO;
            } else {
                String urlString = URLDecoder.decode(url.toString(), StandardCharsets.UTF_8);
                int lastSlash = urlString.lastIndexOf('/');
                if (lastSlash != -1) {
                    shortURL = urlString.substring(lastSlash + 1);
                } else {
                    shortURL = urlString.substring(0, 10); //TODO
                }
            }
            return new SimpleStringProperty(shortURL);
        });

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

    public TableView<WorldMap> getMapsTableView() {
        return mapsTableView;
    }
}