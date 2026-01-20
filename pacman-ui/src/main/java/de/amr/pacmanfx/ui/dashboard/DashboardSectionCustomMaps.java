/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapParseException;
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
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class DashboardSectionCustomMaps extends DashboardSection {

    private static class MapLinkUserData {
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

    private Consumer<File> mapEditFunction = file -> Logger.info("Open map file {}", file);
    private final ObservableList<WorldMap> customMaps = FXCollections.observableArrayList();

    public DashboardSectionCustomMaps(Dashboard dashboard) {
        super(dashboard);

        final var tableView = new TableView<WorldMap>();
        tableView.getColumns().add(createMapRowCountTableColumn());
        tableView.getColumns().add(createMapColCountTableColumn());
        tableView.getColumns().add(createMapURLTableColumn());
        tableView.getColumns().forEach(column -> {
            column.setSortable(false);
            column.setReorderable(false);
        });
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.setPrefWidth(dashboard.style().width() - 20);
        tableView.setPrefHeight(500);
        tableView.setItems(customMaps);

        addRow(tableView);

        expandedProperty().addListener((_, _, expanded) -> {
            if (expanded) {
                updateCustomMapList();
            }
        });
    }

    public void setCustomDirWatchDog(DirectoryWatchdog watchdog) {
        watchdog.addEventListener(eventList -> {
            Logger.info("Custom map change(s) detected: {}",
                eventList.stream()
                    .map(watchEvent -> String.format("%s: '%s'", watchEvent.kind(), watchEvent.context()))
                    .toList());
            updateCustomMapList();
        });
    }

    public void setMapEditFunction(Consumer<File> mapEditFunction) {
        this.mapEditFunction = requireNonNull(mapEditFunction);
    }

    private TableColumn<WorldMap, Integer> createMapRowCountTableColumn() {
        final var column = new TableColumn<WorldMap, Integer>("Rows");
        column.setPrefWidth(40);
        column.setResizable(false);
        column.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().numRows()).asObject());
        column.setStyle("-fx-alignment: CENTER-RIGHT;");
        return column;
    }

    private TableColumn<WorldMap, Integer> createMapColCountTableColumn() {
        final var column = new TableColumn<WorldMap, Integer>("Cols");
        column.setPrefWidth(40);
        column.setResizable(false);
        column.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().numCols()).asObject());
        column.setStyle("-fx-alignment: CENTER-RIGHT;");
        return column;
    }

    private TableColumn<WorldMap, String> createMapURLTableColumn() {
        final var column = new TableColumn<WorldMap, String>("Map");
        column.setPrefWidth(dashboard.style().width() - 80);
        column.setCellValueFactory(data -> new SimpleStringProperty(trimURL(data.getValue().url())));

        // Show link with map filename that edits the map when clicked
        column.setCellFactory(_ -> new TableCell<>() {
            private final Button linkButton = new Button();
            {
                linkButton.getStyleClass().add("link-button"); // see global style.css
                linkButton.setOnAction(_ -> {
                    final MapLinkUserData data = (MapLinkUserData) linkButton.getUserData();
                    try {
                        final String mapURL = data.worldMap.url();
                        if (mapURL != null && mapURL.startsWith("file:")) {
                            final File mapFile = new File(URI.create(mapURL));
                            mapEditFunction.accept(mapFile);
                        } else {
                            Logger.error("World map does not provide file URL to load it from file system");
                        }
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
        return column;
    }

    private void updateCustomMapList() {
        customMaps.clear();
        final File[] mapFiles = GameBox.CUSTOM_MAP_DIR.listFiles((_, name) -> name.endsWith(".world"));
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
                final WorldMap worldMap = WorldMap.loadFromFile(file);
                customMaps.add(worldMap);
                Logger.info("Custom map loaded from file {}", file);
            } catch (IOException x) {
                Logger.error("Could not open world map");
            }
            catch (WorldMapParseException x) {
                Logger.error("Could not parse world map");
            }
        }
    }
}