/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class Carousel extends StackPane {

    private static final ResourceManager RESOURCES = () -> Carousel.class;

    private static final Image ARROW_LEFT_IMAGE  = RESOURCES.loadImage("arrow-left.png");
    private static final Image ARROW_RIGHT_IMAGE = RESOURCES.loadImage("arrow-right.png");

    private static final int NAVIGATION_BUTTON_SIZE = 32;

    private static final Duration NAVIGATION_LOCK_DURATION = Duration.seconds(1.0);

    private final IntegerProperty selectedIndex = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            arrangeChildren();
        }
    };

    private final List<Node> items = new ArrayList<>();
    private final Node btnBack;
    private final Node btnForward;

    private final ProgressBar progressBar;
    private final Timeline progressTimer;

    private final BooleanProperty navigationLocked = new SimpleBooleanProperty(false);
    private final PauseTransition navigationLockTimer = new PauseTransition(NAVIGATION_LOCK_DURATION);

    public Carousel() {
        this(Duration.seconds(5));
    }

    public Carousel(Duration itemChangeDuration) {
        requireNonNull(itemChangeDuration);

        btnBack = createNavigationButton(Direction.LEFT);
        btnBack.setOnMousePressed(_ -> showPreviousItem());
        btnBack.disableProperty().bind(navigationLocked);

        btnForward = createNavigationButton(Direction.RIGHT);
        btnForward.setOnMousePressed(_ -> showNextItem());
        btnForward.disableProperty().bind(navigationLocked);

        navigationLockTimer.setOnFinished(_ -> unlockNavigation());

        progressBar = new ProgressBar(0);
        progressBar.setPrefHeight(10);
        progressBar.setPrefWidth(400);

        progressTimer = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(itemChangeDuration,
                _ -> showNextItem(),
                new KeyValue(progressBar.progressProperty(), 1)));
        progressTimer.setCycleCount(Animation.INDEFINITE);

        // Note: timer must exist at this point
        progressBar.visibleProperty().bind(progressTimer.statusProperty().map(status -> status.equals(Animation.Status.RUNNING)));

        arrangeChildren();

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                toggleProgressTimer();
                e.consume();
            }
        });
    }

    public void restartProgressTimer() {
        progressTimer.stop();
        progressTimer.jumpTo(Duration.ZERO);
        startProgressTimer();
    }

    public void startProgressTimer() {
        if (progressTimer.getStatus() != Animation.Status.RUNNING) {
            progressTimer.play();
            Logger.info("Carousel timer started");
        }
    }

    public void pauseProgressTimer() {
        if (progressTimer.getStatus() == Animation.Status.RUNNING) {
            progressTimer.pause();
            Logger.info("Carousel timer paused");
        }
    }

    public void toggleProgressTimer() {
        if (progressTimer.getStatus() == Animation.Status.RUNNING) pauseProgressTimer(); else startProgressTimer();
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndex;
    }

    public int selectedIndex() { return selectedIndex.get(); }

    public void setSelectedIndex(int index) {
        if (index < -1 || index >= numItems()) {
            throw new IndexOutOfBoundsException("Invalid carousel index: " + index);
        }
        selectedIndex.set(index);
    }

    public int numItems() {
        return items.size();
    }

    public void addItem(Node item) {
        requireNonNull(item);
        items.add(item);
        if (selectedIndex() == -1 && !items.isEmpty()) {
            setSelectedIndex(0);
        }
    }

    public Optional<Node> currentItem() {
        return selectedIndex() != -1
            ? Optional.of(items.get(selectedIndex()))
            : Optional.empty();
    }

    public void setNavigationButtonsVisible(boolean visible) {
        btnBack.setVisible(visible);
        btnForward.setVisible(visible);
    }

    public void showPreviousItem() {
        if (items.isEmpty()) return;
        if (navigationLocked.get()) {
            return;
        }
        int prev = selectedIndex() > 0 ? selectedIndex() - 1: numItems() - 1;
        setSelectedIndex(prev);
        lockNavigation();
        restartProgressTimer();
    }

    public void showNextItem() {
        if (items.isEmpty()) return;
        if (navigationLocked.get()) {
            return;
        }
        int next = selectedIndex() < numItems() - 1 ? selectedIndex() + 1 : 0;
        setSelectedIndex(next);
        lockNavigation();
        restartProgressTimer();
    }

    private void unlockNavigation() {
        navigationLockTimer.stop();
        navigationLocked.set(false);
        Logger.info("Navigation unlocked");
    }

    private void lockNavigation() {
        navigationLocked.set(true);
        navigationLockTimer.playFromStart();
        Logger.info("Navigation locked");
    }

    private void arrangeChildren() {
        getChildren().clear();
        currentItem().ifPresent(item -> getChildren().add(item));
        // Buttons must be added last to stack pane!
        getChildren().addAll(progressBar, btnBack, btnForward);

        StackPane.setAlignment(btnBack, Pos.CENTER_LEFT);
        StackPane.setAlignment(btnForward, Pos.CENTER_RIGHT);
        StackPane.setAlignment(progressBar, Pos.BOTTOM_CENTER);
    }

    protected Node createNavigationButton(Direction dir) {
        final var icon = new ImageView(switch (dir) {
            case LEFT -> ARROW_LEFT_IMAGE;
            case RIGHT -> ARROW_RIGHT_IMAGE;
            default -> throw new IllegalArgumentException("Illegal carousel button direction: %s".formatted(dir));
        });
        icon.setFitHeight(NAVIGATION_BUTTON_SIZE);
        icon.setFitWidth(NAVIGATION_BUTTON_SIZE);

        final var button = new Button();
        button.setGraphic(icon);
        button.setOpacity(0.1);
        button.setOnMouseEntered(_ -> button.setOpacity(0.4));
        button.setOnMouseExited(_ -> button.setOpacity(0.1));
        // Without this, button gets input focus after being clicked with the mouse and the navigation keys stop working!
        button.setFocusTraversable(false);

        return button;
    }
}