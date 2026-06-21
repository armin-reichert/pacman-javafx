/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.controls.skin;

import de.amr.pacmanfx.uilib.controls.Carousel;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class CarouselSkin extends SkinBase<Carousel> {

    public enum NavigationDirection { BACK, FORWARD }

    private final StackPane root = new StackPane();

    private final ProgressBar progressBar = new ProgressBar(0);
    private final Timeline progressTimer;

    private final PauseTransition lockTimer =
        new PauseTransition(Duration.seconds(1));

    private final Node navButtonBack;
    private final Node navButtonForward;

    public CarouselSkin(Carousel control) {
        super(control);

        navButtonBack    = createNavButton(NavigationDirection.BACK);
        navButtonForward = createNavButton(NavigationDirection.FORWARD);

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

        root.getChildren().addAll(progressBar, navButtonBack, navButtonForward);

        StackPane.setAlignment(progressBar, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(navButtonBack, Pos.CENTER_LEFT);
        StackPane.setAlignment(navButtonForward, Pos.CENTER_RIGHT);
    }

    private Button createNavButton(NavigationDirection dir) {
        Button button = new Button();
        button.getStyleClass().add("carousel-nav");
        button.setPickOnBounds(true);

        button.disableProperty().bind(getSkinnable().navigationLockedProperty());

        switch (dir) {
            case BACK -> {
                final var icon = new FontAwesomeIcon(FontAwesomeSymbol.CHEVRON_CIRCLE_LEFT);
                icon.setId("nav-icon-back");
                icon.setMouseTransparent(true);
                button.setGraphic(icon);
                button.setOnAction(_ -> showPrevious());
            }
            case FORWARD -> {
                final var icon = new FontAwesomeIcon(FontAwesomeSymbol.CHEVRON_CIRCLE_RIGHT);
                icon.setId("nav-icon-forward");
                icon.setMouseTransparent(true);
                button.setGraphic(icon);
                button.setOnAction(_ -> showNext());
            }
        }

        return button;
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
