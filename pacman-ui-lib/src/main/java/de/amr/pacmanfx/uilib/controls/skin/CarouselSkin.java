/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.controls.skin;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.uilib.controls.Carousel;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class CarouselSkin extends SkinBase<Carousel> {

    private final StackPane root = new StackPane();

    private final ProgressBar progressBar = new ProgressBar(0);
    private final Timeline progressTimer;

    private final PauseTransition lockTimer =
        new PauseTransition(Duration.seconds(1));

    private final Node navBackArea;
    private final Node navForwardArea;

    public CarouselSkin(Carousel control) {
        super(control);

        navBackArea = createNavArea(Direction.LEFT, this::showPrevious);

        navForwardArea = createNavArea(Direction.RIGHT, this::showNext);

        progressBar.getStyleClass().add("carousel-progress");
        progressBar.setPrefHeight(10);

        progressTimer = createProgressTimer();

        // Bind progress bar visibility to timer status
        progressBar.visibleProperty().bind(
            progressTimer.statusProperty().map(s -> s == Animation.Status.RUNNING)
        );

        // Unlock navigation after lock timer
        lockTimer.setOnFinished(_ -> control.setNavigationLocked(false));

        // React to item list changes
        control.getItems().addListener((ListChangeListener<Node>) _ -> updateView());

        // React to selected index changes
        ChangeListener<Number> indexListener = (_, _, _) -> updateView();
        control.selectedIndexProperty().addListener(indexListener);

        // Keyboard: SPACE toggles timer
        control.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case SPACE -> {
                    if (progressTimer.getStatus() == Animation.Status.PAUSED) {
                        progressTimer.play();
                    } else {
                        progressTimer.pause();
                    }
                    e.consume();
                }
                case LEFT -> showPrevious();
                case RIGHT -> showNext();
            }
        });

        control.progressRunningProperty().addListener((_, _, running) -> {
            if (running) {
                progressTimer.play();
            } else {
                progressTimer.pause();
            }
        });

        if (control.progressRunningProperty().get()) {
            progressTimer.play();
        } else {
            progressTimer.pause();
        }

        updateView();
        getChildren().add(root);
    }

    private Timeline createProgressTimer() {
        Carousel c = getSkinnable();
        double duration = c.getChangeDuration();

        final Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(duration),
                _ -> showNext(),
                new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    private void updateView() {
        Carousel c = getSkinnable();
        root.getChildren().clear();

        int idx = c.getSelectedIndex();
        if (idx >= 0 && idx < c.getItems().size()) {
            root.getChildren().add(c.getItems().get(idx));
        }

        root.getChildren().addAll(progressBar, navBackArea, navForwardArea);

        StackPane.setAlignment(progressBar, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(navBackArea, Pos.CENTER_LEFT);
        StackPane.setAlignment(navForwardArea, Pos.CENTER_RIGHT);
    }

    private Node createNavArea(Direction dir, Runnable action) {
        FontAwesomeIcon icon = switch (dir) {
            case LEFT  -> new FontAwesomeIcon(FontAwesomeSymbol.CHEVRON_CIRCLE_LEFT, 48);
            case RIGHT -> new FontAwesomeIcon(FontAwesomeSymbol.CHEVRON_CIRCLE_RIGHT, 48);
            default -> throw new IllegalArgumentException();
        };
        icon.setMouseTransparent(true);
        icon.setFill(Color.gray(0.42));

        HBox box = new HBox(icon);
        box.getStyleClass().add("carousel-nav");
        box.setPadding(new Insets(5));
        box.setPickOnBounds(true);

        box.disableProperty().bind(getSkinnable().navigationLockedProperty());

        box.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                e.consume();
                action.run();
            }
        });

        return box;
    }

    private void showNext() {
        Carousel c = getSkinnable();
        if (c.getItems().isEmpty() || c.isNavigationLocked()) return;

        int next = (c.getSelectedIndex() + 1) % c.getItems().size();
        c.setSelectedIndex(next);
        lockNavigation();

        progressTimer.playFromStart();
    }

    private void showPrevious() {
        Carousel c = getSkinnable();
        if (c.getItems().isEmpty() || c.isNavigationLocked()) return;

        int prev = c.getSelectedIndex() > 0
            ? c.getSelectedIndex() - 1
            : c.getItems().size() - 1;

        c.setSelectedIndex(prev);
        lockNavigation();

        progressTimer.playFromStart();
    }

    private void lockNavigation() {
        Carousel c = getSkinnable();
        c.setNavigationLocked(true);
        lockTimer.playFromStart();
    }
}
