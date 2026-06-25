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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class CarouselSkin extends SkinBase<Carousel> {

    public enum NavigationDirection { BACK, FORWARD }

    private final StackPane root = new StackPane();

    private final ProgressBar progressBar = new ProgressBar(0);
    private final Timeline progressTimer;

    private final PauseTransition lockTimer = new PauseTransition(Duration.seconds(1));

    private final Node navButtonBack;
    private final Node navButtonForward;

    public CarouselSkin(Carousel carousel) {
        super(carousel);

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
        lockTimer.setOnFinished(_ -> carousel.setNavigationLocked(false));

        // React to item list changes
        carousel.getItems().addListener((ListChangeListener<Node>) _ -> updateLayout());

        // React to selected index changes
        ChangeListener<Number> indexListener = (_, _, _) -> updateLayout();
        carousel.selectedIndexProperty().addListener(indexListener);

        // Keyboard: SPACE toggles timer
        carousel.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case SPACE -> {
                    e.consume();
                    if (progressTimer.getStatus() == Animation.Status.PAUSED) {
                        progressTimer.play();
                    } else {
                        progressTimer.pause();
                    }
                }
                case LEFT -> {
                    e.consume();
                    showPrevious();
                }
                case RIGHT -> {
                    e.consume();
                    showNext();
                }
            }
        });

        carousel.progressRunningProperty().addListener((_, _, running) -> {
            if (running) {
                progressTimer.play();
            } else {
                progressTimer.pause();
            }
        });

        if (carousel.progressRunningProperty().get()) {
            progressTimer.play();
        } else {
            progressTimer.pause();
        }

        updateLayout();
        getChildren().add(root);
    }

    private Timeline createProgressTimer() {
        final Carousel carousel = getSkinnable();
        final double duration = carousel.getChangeDuration();

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

    private void updateLayout() {
        final Carousel carousel = getSkinnable();
        root.getChildren().clear();

        final int i = carousel.getSelectedIndex();
        if (0 <= i && i < carousel.getItems().size()) {
            final Node selectedItem = carousel.getItems().get(i);
            root.getChildren().add(selectedItem);
        }

        root.getChildren().addAll(progressBar, navButtonBack, navButtonForward);

        StackPane.setAlignment(progressBar, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(navButtonBack, Pos.CENTER_LEFT);
        StackPane.setAlignment(navButtonForward, Pos.CENTER_RIGHT);
    }

    private Button createNavButton(NavigationDirection dir) {
        final Carousel carousel = getSkinnable();

        final Button button = new Button();
        button.getStyleClass().add("carousel-nav");

        button.disableProperty().bind(carousel.navigationLockedProperty());

        final Tooltip tooltip = new Tooltip();
        tooltip.setFont(Font.font(16)); //TODO CSS
        button.setTooltip(tooltip);

        switch (dir) {
            case BACK -> {
                final var icon = new FontAwesomeIcon(FontAwesomeSymbol.CHEVRON_CIRCLE_LEFT);
                icon.setMouseTransparent(true); // important!
                button.setGraphic(icon);
                button.setOnAction(_ -> showPrevious());
                button.visibleProperty().bind(carousel.backButtonVisibleProperty());
                tooltip.textProperty().bind(carousel.backButtonTooltipProperty());
            }
            case FORWARD -> {
                final var icon = new FontAwesomeIcon(FontAwesomeSymbol.CHEVRON_CIRCLE_RIGHT);
                icon.setMouseTransparent(true); // important!
                button.setGraphic(icon);
                button.setOnAction(_ -> showNext());
                button.visibleProperty().bind(carousel.forwardButtonVisibleProperty());
                tooltip.textProperty().bind(carousel.forwardButtonTooltipProperty());
            }
        }

        return button;
    }

    private void showNext() {
        final Carousel carousel = getSkinnable();
        if (carousel.getItems().isEmpty() || carousel.isNavigationLocked()) return;

        final int count = carousel.getItems().size();
        final int current = carousel.getSelectedIndex();
        carousel.setSelectedIndex((current + 1) % count);

        lockNavigation();
        progressTimer.playFromStart();
    }

    private void showPrevious() {
        final Carousel carousel = getSkinnable();
        if (carousel.getItems().isEmpty() || carousel.isNavigationLocked()) return;

        final int last = carousel.getItems().size() - 1;
        final int current = carousel.getSelectedIndex();
        carousel.setSelectedIndex(current > 0 ? current - 1 : last);

        lockNavigation();
        progressTimer.playFromStart();
    }

    private void lockNavigation() {
        final Carousel carousel = getSkinnable();
        carousel.setNavigationLocked(true);
        lockTimer.playFromStart();
    }
}
