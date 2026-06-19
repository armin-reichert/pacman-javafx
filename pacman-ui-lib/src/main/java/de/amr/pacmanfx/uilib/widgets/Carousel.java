/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import de.amr.basics.math.Direction;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

//TODO make a JavaFX control from this
public class Carousel extends StackPane {

    public static final int NAV_AREA_SIZE = 48;

    public static final double DEFAULT_CHANGE_SECONDS = 5;
    public static final double DEFAULT_LOCK_SECONDS = 1.0;

    private final IntegerProperty selectedIndex = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            arrangeChildren();
        }
    };

    private final List<Node> items = new ArrayList<>();
    private final Node navBackArea;
    private final Node navForwardArea;

    private final ProgressBar progressBar;
    private final Timeline progressTimer;

    private final BooleanProperty navigationLocked = new SimpleBooleanProperty(false);
    private final PauseTransition navigationLockTimer = new PauseTransition(Duration.seconds(DEFAULT_LOCK_SECONDS));

    public Carousel() {
        this(DEFAULT_CHANGE_SECONDS);
    }

    public Carousel(double changeDuration) {
        navBackArea = createNavigationArea(Direction.LEFT, this::showPreviousItem);
        navForwardArea = createNavigationArea(Direction.RIGHT, this::showNextItem);

        navigationLockTimer.setOnFinished(_ -> unlockNavigation());

        progressBar = new ProgressBar(0);
        progressBar.setPrefHeight(10);
        progressBar.setPrefWidth(400);

        progressTimer = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(changeDuration),
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
    }

    public Optional<Node> currentItem() {
        return selectedIndex() != -1
            ? Optional.of(items.get(selectedIndex()))
            : Optional.empty();
    }

    public void setNavigationButtonsVisible(boolean visible) {
        navBackArea.setVisible(visible);
        navForwardArea.setVisible(visible);
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
        getChildren().addAll(progressBar, navBackArea, navForwardArea);

        StackPane.setAlignment(navBackArea, Pos.CENTER_LEFT);
        StackPane.setAlignment(navForwardArea, Pos.CENTER_RIGHT);
        StackPane.setAlignment(progressBar, Pos.BOTTOM_CENTER);
    }

    protected Node createNavigationArea(Direction dir, Runnable navAction) {
        final FontAwesomeIcon icon = switch (dir) {
            case LEFT  -> new FontAwesomeIcon(FontAwesomeSymbol.CHEVRON_CIRCLE_LEFT, NAV_AREA_SIZE);
            case RIGHT -> new FontAwesomeIcon(FontAwesomeSymbol.CHEVRON_CIRCLE_RIGHT, NAV_AREA_SIZE);
            default -> throw new IllegalArgumentException("Illegal navigation direction: %s".formatted(dir));
        };
        icon.fillProperty().set(Color.gray(0.42));
        // Mouse clicks are handled by the containing navigation area
        icon.setMouseTransparent(true);

        final var navArea = new HBox(icon);
        navArea.setMaxHeight(NAV_AREA_SIZE);
        navArea.setMaxWidth(NAV_AREA_SIZE);
        navArea.setPadding(new Insets(5));
        navArea.setPickOnBounds(true);
        navArea.setOpacity(0.25);
        navArea.setOnMouseEntered(_ -> navArea.setOpacity(0.8));
        navArea.setOnMouseExited(_ -> navArea.setOpacity(0.25));

        StackPane.setAlignment(navArea, dir == Direction.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

        navArea.disableProperty().bind(navigationLocked);

        navArea.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                e.consume();
                navAction.run();
            }
        });

        return navArea;
    }
}