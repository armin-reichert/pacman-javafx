/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.Globals.theGameEventManager;
import static de.amr.pacmanfx.ui.PacManGames.theUI;

public class InfoBoxAnimationInfo extends InfoBox {

    private static final float REFRESH_TIME_SEC = 1.0f;

    public static class TableData {
        private final StringProperty labelProperty;
        private final ObjectProperty<Animation> animationProperty;

        TableData(String label, Animation animation) {
            labelProperty = new SimpleStringProperty(label);
            animationProperty = new SimpleObjectProperty<>(animation);
        }

        public StringProperty labelProperty() { return labelProperty; }
        public ObjectProperty<Animation> animationProperty() {
            return animationProperty;
        }
    }

    private AnimationManager animationManager;
    private final TableView<TableData> tableView = new TableView<>();
    private final ObservableList<TableData> tableModel = FXCollections.observableArrayList();
    private final Timeline refreshTimer;

    public InfoBoxAnimationInfo() {
        tableView.setItems(tableModel);
        tableView.setPlaceholder(new Text("No animations"));
        tableView.setFocusTraversable(false);

        TableColumn<TableData, String> labelColumn = new TableColumn<>("Animation Name");
        labelColumn.setCellValueFactory(data -> data.getValue().labelProperty());
        labelColumn.setSortable(false);

        TableColumn<TableData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().animationProperty()
                .map(animation -> animation == null ? "unknown" : animation.getStatus().name()));
        statusColumn.setSortable(false);

        tableView.getColumns().add(labelColumn);
        tableView.getColumns().add(statusColumn);

        addRow(tableView);

        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(REFRESH_TIME_SEC), e -> updateTableData()));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public void init() {
        super.init();
        tableView.setPrefWidth(300);
        tableView.setPrefHeight(600);

        theGameEventManager().addEventListener(new DefaultGameEventListener() {
            @Override
            public void onLevelStarted(GameEvent e) {
                theUI().currentGameScene().ifPresent(gameScene -> {
                    animationManager = gameScene instanceof PlayScene3D playScene3D ? playScene3D.animationManager() : null;
                    updateTableData();
                });
            }
        });
        refreshTimer.play();
    }

    @Override
    public void update() {
        super.update();
        tableView.setPrefHeight(theUI().stage().getHeight() * 0.85);
    }


    private void updateTableData() {
        if (!isVisible()) return;
        tableModel.clear();
        if (animationManager != null) {
            tableModel.addAll(createTableDataSortedByKey(Animation.Status.RUNNING));
            tableModel.addAll(createTableDataSortedByKey(Animation.Status.PAUSED));
            Logger.info("Animation table updated");
        }
    }

    private List<TableData> createTableDataSortedByKey(Animation.Status status) {
        return animationManager.animationMap().entrySet().stream()
            .filter(entry -> testAnimationStatus(entry.getValue(), status))
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                ManagedAnimation managedAnimation = entry.getValue();
                return new TableData(managedAnimation.label(), managedAnimation.animation().orElse(null));
            }).toList();
    }

    private boolean testAnimationStatus(ManagedAnimation ma, Animation.Status status) {
        if (ma.animation().isPresent()) {
            return ma.animation().get().getStatus() == status;
        }
        return false;
    }
}