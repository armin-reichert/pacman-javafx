/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

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

public class Carousel extends StackPane implements ResourceManager {

    private static Button createCarouselButton(Image image) {
        ImageView icon = new ImageView(image);
        icon.setFitHeight(32);
        icon.setFitWidth(32);
        var button = new Button();
        // Without this, button gets input focus after being clicked with the mouse and the LEFT, RIGHT keys stop working!
        button.setFocusTraversable(false);
        button.setGraphic(icon);
        button.setOpacity(0.1);
        button.setOnMouseEntered(e -> button.setOpacity(0.4));
        button.setOnMouseExited(e -> button.setOpacity(0.1));
        return button;
    }

    private final IntegerProperty selectedIndexPy = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            getChildren().clear();
            currentSlide().ifPresent(slide -> getChildren().add(slide));
            // Buttons must be added last to stackpane!
            getChildren().addAll(btnPrevSlideSelector, btnNextSlideSelector);
        }
    };

    private final List<Node> slides = new ArrayList<>();
    private final Button btnPrevSlideSelector;
    private final Button btnNextSlideSelector;

    @Override
    public Class<?> resourceRootClass() {
        return Carousel.class;
    }

    public Carousel() {
        btnPrevSlideSelector = createCarouselButton(loadImage("graphics/arrow-left.png"));
        btnPrevSlideSelector.setOnAction(e -> showPreviousSlide());
        StackPane.setAlignment(btnPrevSlideSelector, Pos.CENTER_LEFT);

        btnNextSlideSelector = createCarouselButton(loadImage("graphics/arrow-right.png"));
        btnNextSlideSelector.setOnAction(e -> showNextSlide());
        StackPane.setAlignment(btnNextSlideSelector, Pos.CENTER_RIGHT);
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndexPy;
    }

    public int selectedIndex() { return selectedIndexPy.get(); }

    public void setSelectedIndex(int index) { selectedIndexPy.set(index); }

    public int numSlides() {
        return slides.size();
    }

    public void addSlide(Node slide) {
        slides.add(slide);
    }

    public Node slide(int index) {
        return slides.get(index);
    }

    public Optional<Node> currentSlide() {
        return selectedIndex() != -1
            ? Optional.of(slides.get(selectedIndex()))
            : Optional.empty();
    }

    public void setNavigationVisible(boolean visible) {
        btnPrevSlideSelector.setVisible(visible);
        btnNextSlideSelector.setVisible(visible);
    }

    public void showPreviousSlide() {
        if (slides.isEmpty()) return;
        int newIndex = selectedIndex() > 0 ? selectedIndex() - 1: slides.size() - 1;
        selectedIndexPy.set(newIndex);
    }

    public void showNextSlide() {
        if (slides.isEmpty()) return;
        int newIndex = selectedIndexPy.get() < slides.size() - 1 ? selectedIndexPy.get() + 1 : 0;
        selectedIndexPy.set(newIndex);
    }
}