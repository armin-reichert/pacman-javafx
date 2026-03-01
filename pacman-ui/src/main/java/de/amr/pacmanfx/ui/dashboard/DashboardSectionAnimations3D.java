/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
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

public class DashboardSectionAnimations3D extends DashboardSection {

    private static final float RELATIVE_TABLE_HEIGHT = 0.80f;
    private static final float REFRESH_PERIOD_SECONDS = 0.5f;

    public static final String NO_3_D_ANIMATIONS = "No 3D animations";
    public static final String ANIMATION_NAME = "Animation Name";
    public static final String STATUS = "Status";
    public static final String UNKNOWN = "unknown";

    public static class TableRow {
        private final StringProperty labelProperty;
        private final ObjectProperty<Animation> animationProperty;

        TableRow(ManagedAnimation managedAnimation) {
            labelProperty = new SimpleStringProperty(managedAnimation.label());
            animationProperty = new SimpleObjectProperty<>(managedAnimation.optAnimationFX().orElse(null));
        }

        public StringProperty labelProperty() { return labelProperty; }
        public ObjectProperty<Animation> animationProperty() { return animationProperty; }
    }

    private final ObservableList<TableRow> tableRows = FXCollections.observableArrayList();
    private final Timeline refreshTimer;
    private final TableView<TableRow> tableView = new TableView<>();

    private final ObjectProperty<AnimationRegistry> currentAnimationManager = new SimpleObjectProperty<>();

    public DashboardSectionAnimations3D(Dashboard dashboard) {
        super(dashboard);

        tableView.setItems(tableRows);
        tableView.setPlaceholder(new Text(NO_3_D_ANIMATIONS));
        tableView.setFocusTraversable(false);
        tableView.setPrefWidth(dashboard.style().width());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        final var labelColumn = new TableColumn<TableRow, String>(ANIMATION_NAME);
        labelColumn.setCellValueFactory(data -> data.getValue().labelProperty());
        labelColumn.setSortable(false);
        labelColumn.setMinWidth(0.66 * dashboard.style().width());
        tableView.getColumns().add(labelColumn);

        final var statusColumn = new TableColumn<TableRow, String>(STATUS);
        statusColumn.setCellValueFactory(data -> data.getValue().animationProperty()
                .map(animation -> animation == null ? UNKNOWN : animation.getStatus().name()));
        statusColumn.setSortable(false);
        tableView.getColumns().add(statusColumn);

        addRow(tableView);

        refreshTimer = new Timeline(
            new KeyFrame(Duration.seconds(REFRESH_PERIOD_SECONDS), _ -> {
                if (isVisible()) {
                    updateTableData();
                }
            }));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public void init(GameUI ui) {
        super.init(ui);
        tableView.prefHeightProperty()
            .bind(ui.stage().heightProperty().map(height -> height.doubleValue() * RELATIVE_TABLE_HEIGHT));
    }

    @Override
    public void update(GameUI ui) {
        super.update(ui);

        //TODO use data binding
        ui.optGameScene().ifPresent(gameScene -> currentAnimationManager.set(observedAnimationManager(gameScene)));
        if (currentAnimationManager.get() == null) {
            tableRows.clear();
            refreshTimer.pause();
        } else {
            refreshTimer.play();
        }
    }

    private AnimationRegistry observedAnimationManager(GameScene gameScene) {
        if (gameScene instanceof PlayScene3D playScene3D) {
            return playScene3D.level3D().map(GameLevel3D::animationManager).orElse(null);
        }
        return null;
    }

    private void updateTableData() {
        tableRows.clear();
        if (currentAnimationManager.get() != null) {
            final Set<ManagedAnimation> animations = currentAnimationManager.get().animations();
            tableRows.addAll(animationDataSortedByLabel(animations, Animation.Status.RUNNING));
            tableRows.addAll(animationDataSortedByLabel(animations, Animation.Status.PAUSED));
            tableRows.addAll(animationDataSortedByLabel(animations, Animation.Status.STOPPED));
        }
    }

    private List<TableRow> animationDataSortedByLabel(Set<ManagedAnimation> animations, Animation.Status status) {
        return animations.stream()
            .filter(animation -> hasStatus(animation, status))
            .sorted(Comparator.comparing(ManagedAnimation::label))
            .map(TableRow::new)
            .toList();
    }

    private boolean hasStatus(ManagedAnimation managedAnimation, Animation.Status status) {
        return managedAnimation.optAnimationFX().map(animation -> animation.getStatus() == status).orElse(false);
    }
}