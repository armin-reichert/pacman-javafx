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
import java.util.function.Consumer;

public class Carousel extends StackPane {

    private final IntegerProperty selectedIndexPy = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            updateUI();
        }
    };

    protected final List<Node> slides = new ArrayList<>();
    protected final Button btnPrevSlideArrow;
    protected final Button btnNextSlideArrow;

    protected Consumer<Node> actionPrevSlideSelected;
    protected Consumer<Node> actionNextSlideSelected;

    public Carousel() {
        ResourceManager rm = () -> Carousel.class;
        Image arrowLeftImage = rm.loadImage("graphics/arrow-left.png");
        Image arrowRightImage = rm.loadImage("graphics/arrow-right.png");

        btnPrevSlideArrow = createCarouselButton(arrowLeftImage);
        btnPrevSlideArrow.setOnAction(e -> showPreviousSlide());
        StackPane.setAlignment(btnPrevSlideArrow, Pos.CENTER_LEFT);

        btnNextSlideArrow = createCarouselButton(arrowRightImage);
        btnNextSlideArrow.setOnAction(e -> showNextSlide());
        StackPane.setAlignment(btnNextSlideArrow, Pos.CENTER_RIGHT);
    }

    public int selectedIndex() { return selectedIndexPy.get(); }

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

    private void updateUI() {
        getChildren().setAll(btnPrevSlideArrow, btnNextSlideArrow);
        currentSlide().ifPresent(slide -> getChildren().add(slide));
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
        btnPrevSlideArrow.setVisible(visible);
        btnNextSlideArrow.setVisible(visible);
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