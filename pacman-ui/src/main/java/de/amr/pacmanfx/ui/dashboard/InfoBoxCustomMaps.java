/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.layout.EditorView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class InfoBoxCustomMaps extends InfoBox {

    static class MapLinkUserData {
        WorldMap worldMap;
    }

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

        final var tcMapRowCount = new TableColumn<WorldMap, Integer>("Rows");
        tcMapRowCount.setPrefWidth(40);
        tcMapRowCount.setResizable(false);
        tcMapRowCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().numRows()).asObject());
        tcMapRowCount.setStyle("-fx-alignment: CENTER-RIGHT;");

        final var tcMapColCount = new TableColumn<WorldMap, Integer>("Cols");
        tcMapColCount.setPrefWidth(40);
        tcMapColCount.setResizable(false);
        tcMapColCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().numCols()).asObject());
        tcMapColCount.setStyle("-fx-alignment: CENTER-RIGHT;");

        final var tcMapURL = new TableColumn<WorldMap, String>("Map");
        tcMapURL.setPrefWidth(Region.USE_COMPUTED_SIZE);
        tcMapURL.setCellValueFactory(data -> new SimpleStringProperty(trimURL(data.getValue().url())));

        // Show link with map filename that edits the map when clicked
        tcMapURL.setCellFactory(_ -> new TableCell<>() {
            private final Button linkButton = new Button();
            {
                linkButton.getStyleClass().add("link-button"); // see global style.css
                linkButton.setOnAction(_ -> {
                    final MapLinkUserData data = (MapLinkUserData) linkButton.getUserData();
                    try {
                        final String mapURL = data.worldMap.url();
                        if (mapURL != null && mapURL.startsWith("file:")) {
                            editWorldMap(new File(URI.create(mapURL)));
                        } else Logger.error("World map does not provide file URL to load it from file system");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank()) {
                    setGraphic(null);
                } else {
                    final MapLinkUserData data = new MapLinkUserData();
                    data.worldMap = getTableRow().getItem();
                    linkButton.setUserData(data);
                    linkButton.setText(trimURL(data.worldMap.url()));
                    setGraphic(linkButton);
                }
            }
        });


        final var mapsTableView = new TableView<WorldMap>();
        mapsTableView.setItems(customMaps);
        mapsTableView.setPrefWidth(Dashboard.INFOBOX_MIN_WIDTH - 20);
        mapsTableView.setPrefHeight(400);
        mapsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        mapsTableView.getColumns().add(tcMapRowCount);
        mapsTableView.getColumns().add(tcMapColCount);
        // add as last column, gets all remaining space
        mapsTableView.getColumns().add(tcMapURL);

        mapsTableView.getColumns().forEach(column -> {
            column.setSortable(false);
            column.setReorderable(false);
        });

        addRow(mapsTableView);

        expandedProperty().addListener((_, _, expanded) -> {
            if (expanded) {
                updateCustomMapList();
            }
        });

        ui.customDirWatchdog().addEventListener(eventList -> {
            Logger.info("Custom map change(s) detected: {}",
                eventList.stream()
                    .map(watchEvent -> String.format("%s: '%s'", watchEvent.kind(), watchEvent.context()))
                    .toList());
            updateCustomMapList();
        });
    }

    private void updateCustomMapList() {
        customMaps.clear();
        File[] mapFiles = GameBox.CUSTOM_MAP_DIR.listFiles((_, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.error("An error occurred accessing custom map directory {}", GameBox.CUSTOM_MAP_DIR);
            return;
        }
        if (mapFiles.length == 0) {
            Logger.info("No custom maps found");
        } else {
            Logger.info("{} custom map(s) found", mapFiles.length);
        }
        for (File file : mapFiles) {
            try {
                WorldMap worldMap = WorldMap.loadFromFile(file);
                customMaps.add(worldMap);
                Logger.info("Custom map loaded from file {}", file);
            } catch (IOException x) {
                Logger.error("Could not open world map");
                throw new RuntimeException(x);
            }
            catch (WorldMapParseException x) {
                Logger.error("Could not parse world map");
                throw new RuntimeException(x);
            }
        }
    }

    private void editWorldMap(File mapFile) {
        Logger.info("Edit map file {}", mapFile);
        ui.showEditorView();
        ui.optEditorView().ifPresent(view -> {
            if (view instanceof EditorView editorView) {
                try {
                    final WorldMap map = WorldMap.loadFromFile(mapFile);
                    editorView.editor().setCurrentWorldMap(map);
                    editorView.editor().setCurrentFile(mapFile);
                } catch (Exception x) {
                    Logger.error("Could not open map file {}", mapFile);
                }
            }
        });
    }
}