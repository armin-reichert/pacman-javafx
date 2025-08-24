/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
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

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class InfoBoxGameLevelAnimations extends InfoBox {

    private static final float RELATIVE_TABLE_HEIGHT = 0.80f;
    private static final float REFRESH_PERIOD_SECONDS = 0.5f;

    public static class TableRow {
        private final StringProperty labelProperty;
        private final ObjectProperty<Animation> animationProperty;

        TableRow(ManagedAnimation managedAnimation) {
            labelProperty = new SimpleStringProperty(managedAnimation.label());
            animationProperty = new SimpleObjectProperty<>(managedAnimation.animationFX().orElse(null));
        }

        public StringProperty labelProperty() { return labelProperty; }
        public ObjectProperty<Animation> animationProperty() { return animationProperty; }
    }

    private final TableView<TableRow> tableView = new TableView<>();
    private final ObservableList<TableRow> tableRows = FXCollections.observableArrayList();
    private final Timeline refreshTimer;

    private final ObjectProperty<AnimationRegistry> currentAnimationManager = new SimpleObjectProperty<>();

    public InfoBoxGameLevelAnimations(GameUI ui) {
        super(ui);

        tableView.setItems(tableRows);
        tableView.setPlaceholder(new Text("No 3D animations"));
        tableView.setFocusTraversable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.prefHeightProperty().bind(ui.stage().heightProperty().map(height -> height.doubleValue() * RELATIVE_TABLE_HEIGHT));

        TableColumn<TableRow, String> labelColumn = new TableColumn<>("Animation Name");
        labelColumn.setCellValueFactory(data -> data.getValue().labelProperty());
        labelColumn.setSortable(false);
        labelColumn.setMinWidth(180);
        tableView.getColumns().add(labelColumn);

        TableColumn<TableRow, String> statusColumn = new TableColumn<>("Status");
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
        //TODO use data binding
        currentAnimationManager.set(observedAnimationManager());
        if (currentAnimationManager.get() == null) {
            tableRows.clear();
            refreshTimer.pause();
        } else {
            refreshTimer.play();
        }
    }

    private AnimationRegistry observedAnimationManager() {
        if (ui.currentGameScene().isPresent()
            && ui.currentGameScene().get() instanceof PlayScene3D playScene3D
            && playScene3D.level3D().isPresent()) {
            return playScene3D.level3D().get().animationManager();
        }
        return null;
    }

    private void updateTableData() {
        tableRows.clear();
        if (currentAnimationManager.get() != null) {
            Set<ManagedAnimation> animations = currentAnimationManager.get().animations();
            tableRows.addAll(tableDataSortedByAnimationLabel(animations, Animation.Status.RUNNING));
            tableRows.addAll(tableDataSortedByAnimationLabel(animations, Animation.Status.PAUSED));
            tableRows.addAll(tableDataSortedByAnimationLabel(animations, Animation.Status.STOPPED));
        }
    }

    private List<TableRow> tableDataSortedByAnimationLabel(Set<ManagedAnimation> animations, Animation.Status status) {
        return animations.stream()
            .filter(animation -> hasStatus(animation, status))
            .sorted(Comparator.comparing(ManagedAnimation::label))
            .map(TableRow::new)
            .toList();
    }

    private boolean hasStatus(ManagedAnimation ma, Animation.Status status) {
        return ma.animationFX().map(animation -> animation.getStatus() == status).orElse(false);
    }
}