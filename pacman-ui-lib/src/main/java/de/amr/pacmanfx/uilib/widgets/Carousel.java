/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Carousel extends StackPane {

    private static final ResourceManager RESOURCES = () -> Carousel.class;

    private static final Image ARROW_LEFT_IMAGE  = RESOURCES.loadImage("arrow-left.png");
    private static final Image ARROW_RIGHT_IMAGE = RESOURCES.loadImage("arrow-right.png");

    private static final int NAVIGATION_BUTTON_SIZE = 32;

    private final IntegerProperty selectedIndexPy = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            getChildren().clear();
            currentItem().ifPresent(item -> getChildren().add(item));
            // Buttons must be added last to stackpane!
            getChildren().addAll(btnPrevSlideSelector, btnNextSlideSelector);
        }
    };

    private final List<Node> items = new ArrayList<>();
    private final Node btnPrevSlideSelector;
    private final Node btnNextSlideSelector;

    protected Node createCarouselButton(Direction dir) {
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

        StackPane.setAlignment(button, dir == Direction.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return button;
    }

    public Carousel() {
        btnPrevSlideSelector = createCarouselButton(Direction.LEFT);
        btnPrevSlideSelector.setOnMousePressed(e -> showPreviousItem());
        btnNextSlideSelector = createCarouselButton(Direction.RIGHT);
        btnNextSlideSelector.setOnMousePressed(e -> showNextItem());
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndexPy;
    }

    public int selectedIndex() { return selectedIndexPy.get(); }

    public void setSelectedIndex(int index) { selectedIndexPy.set(index); }

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

    public void setNavigationVisible(boolean visible) {
        btnPrevSlideSelector.setVisible(visible);
        btnNextSlideSelector.setVisible(visible);
    }

    public void showPreviousItem() {
        if (items.isEmpty()) return;
        int newIndex = selectedIndex() > 0 ? selectedIndex() - 1: items.size() - 1;
        selectedIndexPy.set(newIndex);
    }

    public void showNextItem() {
        if (items.isEmpty()) return;
        int newIndex = selectedIndexPy.get() < items.size() - 1 ? selectedIndexPy.get() + 1 : 0;
        selectedIndexPy.set(newIndex);
    }
}