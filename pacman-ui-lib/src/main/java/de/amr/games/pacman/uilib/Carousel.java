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

public class Carousel extends StackPane {

    private final IntegerProperty selectedIndexPy = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            int index = get();
            if (index >= 0 && index < slides.size()) {
                getChildren().setAll(slides.get(index), buttonsLayer);
            }
        }
    };

    protected final StackPane buttonsLayer = new StackPane();
    protected final List<Node> slides = new ArrayList<>();
    protected final Button btnPrevSlide;
    protected final Button btnNextSlide;

    protected Runnable onPrevSlide;
    protected Runnable onNextSlide;

    public Carousel() {
        ResourceManager rm = () -> Carousel.class;

        Image arrowLeftImage = rm.loadImage("graphics/arrow-left.png");
        btnPrevSlide = createCarouselButton(arrowLeftImage);
        btnPrevSlide.setOnAction(e -> showPreviousSlide());
        StackPane.setAlignment(btnPrevSlide, Pos.CENTER_LEFT);

        Image arrowRightImage = rm.loadImage("graphics/arrow-right.png");
        btnNextSlide = createCarouselButton(arrowRightImage);
        btnNextSlide.setOnAction(e -> showNextSlide());
        StackPane.setAlignment(btnNextSlide, Pos.CENTER_RIGHT);

        buttonsLayer.getChildren().setAll(btnPrevSlide, btnNextSlide);
        getChildren().add(buttonsLayer);
    }

    private Button createCarouselButton(Image image) {
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

    public void setNavigationVisible(boolean visible) {
        btnPrevSlide.setVisible(visible);
        btnNextSlide.setVisible(visible);
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndexPy;
    }

    public void setOnPrevSlideSelected(Runnable action) {
        onPrevSlide = action;
    }

    public void setOnNextSlideSelected(Runnable action) {
        onNextSlide = action;
    }

    public void showPreviousSlide() {
        int prevIndex = selectedIndexPy.get() > 0 ? selectedIndexPy.get() - 1: slides.size() - 1;
        selectedIndexPy.set(prevIndex);
        updateUI();
        if (onPrevSlide != null) {
            onPrevSlide.run();
        }
    }

    public void showNextSlide() {
        int newIndex = selectedIndexPy.get() < slides.size() - 1 ? selectedIndexPy.get() + 1 : 0;
        selectedIndexPy.set(newIndex);
        updateUI();
        if (onNextSlide != null) {
            onNextSlide.run();
        }
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

    public Node currentSlide() {
        return selectedIndexPy.get() != -1 ? slides.get(selectedIndexPy.get()) : null;
    }

    private void updateUI() {
        Node currentSlide = currentSlide();
        if (currentSlide != null) {
            getChildren().setAll(currentSlide(), buttonsLayer);
        } else {
            getChildren().setAll(buttonsLayer);
        }
    }
}