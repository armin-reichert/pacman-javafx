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
import java.util.Collections;
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

    private final IntegerProperty selectedSlideIndexPy = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            getChildren().clear();
            currentSlide().ifPresent(slide -> getChildren().add(slide));
            // Buttons must be added last!
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
        Image arrowLeftImage = loadImage("graphics/arrow-left.png");
        btnPrevSlideSelector = createCarouselButton(arrowLeftImage);
        btnPrevSlideSelector.setOnAction(e -> showPreviousSlide());
        StackPane.setAlignment(btnPrevSlideSelector, Pos.CENTER_LEFT);

        Image arrowRightImage = loadImage("graphics/arrow-right.png");
        btnNextSlideSelector = createCarouselButton(arrowRightImage);
        btnNextSlideSelector.setOnAction(e -> showNextSlide());
        StackPane.setAlignment(btnNextSlideSelector, Pos.CENTER_RIGHT);
    }

    public int selectedSlideIndex() { return selectedSlideIndexPy.get(); }

    public List<Node> slides() {
        return Collections.unmodifiableList(slides);
    }

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
        return selectedSlideIndex() != -1
            ? Optional.of(slides.get(selectedSlideIndex()))
            : Optional.empty();
    }

    public void setNavigationVisible(boolean visible) {
        btnPrevSlideSelector.setVisible(visible);
        btnNextSlideSelector.setVisible(visible);
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedSlideIndexPy;
    }

    public void showPreviousSlide() {
        if (slides.isEmpty()) return;
        int newIndex = selectedSlideIndex() > 0 ? selectedSlideIndex() - 1: slides.size() - 1;
        selectedSlideIndexPy.set(newIndex);
    }

    public void showNextSlide() {
        if (slides.isEmpty()) return;
        int newIndex = selectedSlideIndexPy.get() < slides.size() - 1 ? selectedSlideIndexPy.get() + 1 : 0;
        selectedSlideIndexPy.set(newIndex);
    }
}