/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameUI;
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

import java.util.List;
import java.util.Map;

public class InfoBoxGameLevelAnimations extends InfoBox {

    private static final float RELATIVE_TABLE_HEIGHT = 0.80f;
    private static final float REFRESH_PERIOD_SECONDS = 0.5f;

    public static class TableData {
        private final StringProperty labelProperty;
        private final ObjectProperty<Animation> animationProperty;

        TableData(ManagedAnimation managedAnimation) {
            labelProperty = new SimpleStringProperty(managedAnimation.label());
            animationProperty = new SimpleObjectProperty<>(managedAnimation.animation().orElse(null));
        }

        public StringProperty labelProperty() { return labelProperty; }
        public ObjectProperty<Animation> animationProperty() { return animationProperty; }
    }

    private final TableView<TableData> tableView = new TableView<>();
    private final ObservableList<TableData> tableModel = FXCollections.observableArrayList();
    private final Timeline refreshTimer;

    // References the animation timer of the current 3D game level (if present)
    private final ObjectProperty<AnimationManager> animationManagerProperty = new SimpleObjectProperty<>();

    public InfoBoxGameLevelAnimations(GameUI ui) {
        super(ui);

        tableView.setItems(tableModel);
        tableView.setPlaceholder(new Text("No 3D animations"));
        tableView.setFocusTraversable(false);
        tableView.setPrefWidth(300);
        tableView.setPrefHeight(300);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<TableData, String> labelColumn = new TableColumn<>("Animation Name");
        labelColumn.setCellValueFactory(data -> data.getValue().labelProperty());
        labelColumn.setSortable(false);
        labelColumn.setMinWidth(180);
        tableView.getColumns().add(labelColumn);

        TableColumn<TableData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().animationProperty()
                .map(animation -> animation == null ? "unknown" : animation.getStatus().name()));
        statusColumn.setSortable(false);
        tableView.getColumns().add(statusColumn);

        addRow(tableView);

        refreshTimer = new Timeline(
            new KeyFrame(Duration.seconds(REFRESH_PERIOD_SECONDS), e -> {
                if (isVisible()) {
                    updateTableData();
                }
            }));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public void update() {
        super.update();
        tableView.setPrefHeight(ui.theStage().getHeight() * RELATIVE_TABLE_HEIGHT);
        boolean refresh = false;
        if (ui.currentGameScene().isPresent() && ui.currentGameScene().get() instanceof PlayScene3D scene3D) {
            scene3D.level3D().ifPresent(gameLevel3D -> animationManagerProperty.set(gameLevel3D.animationManager()));
            refresh = true;
        }
        if (refresh) {
            refreshTimer.play();
        } else {
            refreshTimer.pause();
            tableModel.clear();
        }
    }

    private void updateTableData() {
        tableModel.clear();
        if (animationManagerProperty.get() != null) {
            Map<String, ManagedAnimation> animationMap = animationManagerProperty.get().animationMap();
            tableModel.addAll(dataSortedByMapKey(animationMap, Animation.Status.RUNNING));
            tableModel.addAll(dataSortedByMapKey(animationMap, Animation.Status.PAUSED));
            tableModel.addAll(dataSortedByMapKey(animationMap, Animation.Status.STOPPED));
        }
    }

    private List<TableData> dataSortedByMapKey(Map<String, ManagedAnimation> animationMap, Animation.Status status) {
        return animationMap.entrySet().stream()
            .filter(entry -> hasStatus(entry.getValue(), status))
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new TableData(entry.getValue()))
            .toList();
    }

    private boolean hasStatus(ManagedAnimation ma, Animation.Status status) {
        return ma.animation().map(animation -> animation.getStatus() == status).orElse(false);
    }
}