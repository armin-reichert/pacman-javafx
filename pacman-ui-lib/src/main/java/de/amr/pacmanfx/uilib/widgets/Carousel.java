/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class Carousel extends StackPane {

    private static final ResourceManager RESOURCES = () -> Carousel.class;

    private static final Image ARROW_LEFT_IMAGE  = RESOURCES.loadImage("arrow-left.png");
    private static final Image ARROW_RIGHT_IMAGE = RESOURCES.loadImage("arrow-right.png");

    private static final int NAVIGATION_BUTTON_SIZE = 32;

    private final IntegerProperty selectedIndexPy = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            arrangeChildren();
        }
    };

    private final List<Node> items = new ArrayList<>();
    private final Node btnBack;
    private final Node btnForward;

    private final Timeline timer;
    private final ProgressBar progressBar;

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
        button.setOnMouseEntered(e -> button.setOpacity(0.4));
        button.setOnMouseExited(e -> button.setOpacity(0.1));
        // Without this, button gets input focus after being clicked with the mouse and the LEFT, RIGHT keys stop working!
        button.setFocusTraversable(false);

        return button;
    }

    public Carousel() {
        this(Duration.seconds(5));
    }

    public Carousel(Duration itemChangeDuration) {
        requireNonNull(itemChangeDuration);

        btnBack = createNavigationButton(Direction.LEFT);
        btnBack.setOnMousePressed(e -> showPreviousItem());

        btnForward = createNavigationButton(Direction.RIGHT);
        btnForward.setOnMousePressed(e -> showNextItem());

        progressBar = new ProgressBar(0);
        progressBar.setPrefHeight(10);
        progressBar.setPrefWidth(400);

        timer = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(itemChangeDuration,
                e -> showNextItem(),
                new KeyValue(progressBar.progressProperty(), 1)));
        timer.setCycleCount(Animation.INDEFINITE);

        arrangeChildren();
    }

    public void start() {
        timer.play();
    }

    public void pause() {
        timer.pause();
    }

    public boolean isPlaying() {
        return timer.getStatus() == Animation.Status.RUNNING;
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndexPy;
    }

    public int selectedIndex() { return selectedIndexPy.get(); }

    public void setSelectedIndex(int index) {
        selectedIndexPy.set(index);
    }

    public int numItems() {
        return items.size();
    }

    public void addItem(Node item) {
        items.add(item);
    }

    public Node itemAt(int index) {
        return items.get(index);
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
        int prev = selectedIndex() > 0 ? selectedIndex() - 1: numItems() - 1;
        setSelectedIndex(prev);
    }

    public void showNextItem() {
        if (items.isEmpty()) return;
        int next = selectedIndex() < numItems() - 1 ? selectedIndex() + 1 : 0;
        setSelectedIndex(next);
    }
}