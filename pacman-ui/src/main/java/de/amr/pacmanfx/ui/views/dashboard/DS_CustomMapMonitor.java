/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.tinylog.Logger;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class DS_CustomMapMonitor extends GameDashboardSection {

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

    public DS_CustomMapMonitor() {
        super(DashboardID.CUSTOM_MAPS);

        final var tableView = new TableView<WorldMap>();
        tableView.getColumns().add(createMapRowCountTableColumn());
        tableView.getColumns().add(createMapColCountTableColumn());
        tableView.getColumns().add(createMapURLTableColumn());
        tableView.getColumns().forEach(column -> {
            column.setSortable(false);
            column.setReorderable(false);
        });
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        //tableView.setPrefWidth(dashboard.config().width() - 20);
        tableView.setPrefHeight(500);
        tableView.setItems(customMaps);

        addRow(tableView);
    }

    @Override
    public void setGameActionContext(GameAppContext appContext) {
        requireNonNull(appContext);
        setCustomDirWatchDog(appContext.watchdog());
        setMapEditFunction(mapFile ->
            appContext.commonActions().editorActions().createEditMapFileAction(mapFile).execute());
    }

    public void setCustomDirWatchDog(DirectoryWatchdog watchdog) {
        watchdog.addEventListener(eventList -> {
            Logger.info("Custom map change(s) detected: {}",
                eventList.stream()
                    .map(watchEvent -> String.format("%s: '%s'", watchEvent.kind(), watchEvent.context()))
                    .toList());
            updateWorldMapList(watchdog.watchedDir());
        });
        expandedProperty().addListener((_, _, expanded) -> {
            if (expanded) {
                updateWorldMapList(watchdog.watchedDir());
            }
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
        //column.setPrefWidth(dashboard.config().width() - 80);
        column.setCellValueFactory(data -> new SimpleStringProperty(trimURL(data.getValue().url())));

        // Show link with map filename that edits the map when clicked
        column.setCellFactory(_ -> new TableCell<>() {
            private final Button linkButton = new Button();
            {
                linkButton.getStyleClass().add("link-button"); // see global posture.css
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

    private void updateWorldMapList(File customMapDir) {
        customMaps.clear();
        final File[] mapFiles = customMapDir.listFiles((_, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.error("An error occurred accessing custom map directory {}", customMapDir);
            return;
        }
        if (mapFiles.length == 0) {
            Logger.info("No custom maps found");
        } else {
            Logger.info("{} custom map(s) found", mapFiles.length);
        }
        for (File file : mapFiles) {
            WorldMap.fromFile(file).ifPresent(worldMap -> {
                customMaps.add(worldMap);
                Logger.info("Custom map loaded from file {}", file);
            });
        }
    }
}