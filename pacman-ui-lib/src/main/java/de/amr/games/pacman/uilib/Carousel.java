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
import java.util.function.Consumer;

public class Carousel extends StackPane {

    private static final ResourceManager RESOURCE_MANAGER = () -> Carousel.class;
    private static final Image ARROW_LEFT_IMAGE = RESOURCE_MANAGER.loadImage("graphics/arrow-left.png");
    private static final Image ARROW_RIGHT_IMAGE = RESOURCE_MANAGER.loadImage("graphics/arrow-right.png");

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
            // Buttons must be added last!
            getChildren().addAll(btnPrevSlideSelector, btnNextSlideSelector);
        }
    };

    private final List<Node> slides = new ArrayList<>();
    private final Button btnPrevSlideSelector;
    private final Button btnNextSlideSelector;
    private Consumer<Node> actionPrevSlideSelected;
    private Consumer<Node> actionNextSlideSelected;

    public Carousel() {
        btnPrevSlideSelector = createCarouselButton(ARROW_LEFT_IMAGE);
        btnPrevSlideSelector.setOnAction(e -> showPreviousSlide());
        StackPane.setAlignment(btnPrevSlideSelector, Pos.CENTER_LEFT);

        btnNextSlideSelector = createCarouselButton(ARROW_RIGHT_IMAGE);
        btnNextSlideSelector.setOnAction(e -> showNextSlide());
        StackPane.setAlignment(btnNextSlideSelector, Pos.CENTER_RIGHT);
    }

    public int selectedIndex() { return selectedIndexPy.get(); }

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
        return selectedIndex() != -1
            ? Optional.of(slides.get(selectedIndex()))
            : Optional.empty();
    }

    public void setNavigationVisible(boolean visible) {
        btnPrevSlideSelector.setVisible(visible);
        btnNextSlideSelector.setVisible(visible);
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndexPy;
    }

    public void setOnPrevSlideSelected(Consumer<Node> action) {
        actionPrevSlideSelected = action;
    }

    public void setOnNextSlideSelected(Consumer<Node> action) {
        actionNextSlideSelected = action;
    }

    public void showPreviousSlide() {
        if (slides.isEmpty()) return;
        int newIndex = selectedIndex() > 0 ? selectedIndex() - 1: slides.size() - 1;
        selectedIndexPy.set(newIndex);
        if (actionPrevSlideSelected != null) {
            actionPrevSlideSelected.accept(slides.get(newIndex));
        }
    }

    public void showNextSlide() {
        int newIndex = selectedIndexPy.get() < slides.size() - 1 ? selectedIndexPy.get() + 1 : 0;
        selectedIndexPy.set(newIndex);
        if (actionNextSlideSelected != null) {
            actionNextSlideSelected.accept(slides.get(newIndex));
        }
    }
}