package de.amr.games.pacman.ui2d.lib;

import de.amr.games.pacman.ui2d.assets.ResourceManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.ui2d.lib.Ufx.coloredRoundedBackground;

public class Carousel extends StackPane {

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

    private Node createSelectButton(Font font) {
        Color bgColor = Color.rgb(0, 155, 252, 0.7);
        Color fillColor = Color.WHITE;

        var buttonText = new Text();
        buttonText.setFill(fillColor);
        buttonText.setFont(font);
        buttonText.textProperty().bind(selectButtonTextPy);

        var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));
        buttonText.setEffect(shadow);

        var pane = new BorderPane(buttonText);
        pane.setMaxSize(200, 60);
        pane.setPadding(new Insets(5, 5, 5, 5));
        pane.setCursor(Cursor.HAND);
        pane.setBackground(coloredRoundedBackground(bgColor, 20));

        return pane;
    }

    private final IntegerProperty selectedIndexPy = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            int index = get();
            if (index >= 0 && index < slides.size()) {
                getChildren().setAll(slides.get(index), buttonsLayer);
            }
        }
    };

    private final StringProperty selectButtonTextPy = new SimpleStringProperty("SELECT");

    private final StackPane buttonsLayer = new StackPane();
    private final List<Node> slides = new ArrayList<>();
    private final Node btnStart;
    private final Button btnPrevSlide;
    private final Button btnNextSlide;

    private Runnable onPrevSlide;
    private Runnable onNextSlide;

    public Carousel() {
        ResourceManager rm = this::getClass;

        Image arrowLeftImage = rm.loadImage("arrow-left.png");
        Image arrowRightImage = rm.loadImage("arrow-right.png");
        Font startButtonFont = rm.loadFont("emulogic.ttf", 30);

        btnPrevSlide = createCarouselButton(arrowLeftImage);
        btnPrevSlide.setOnAction(e -> prevSlide());
        StackPane.setAlignment(btnPrevSlide, Pos.CENTER_LEFT);

        btnNextSlide = createCarouselButton(arrowRightImage);
        btnNextSlide.setOnAction(e -> nextSlide());
        StackPane.setAlignment(btnNextSlide, Pos.CENTER_RIGHT);

        btnStart = createSelectButton(startButtonFont);
        btnStart.setTranslateY(-50);
        StackPane.setAlignment(btnStart, Pos.BOTTOM_CENTER);

        buttonsLayer.getChildren().setAll(btnPrevSlide, btnNextSlide, btnStart);
        getChildren().add(buttonsLayer);
    }

    public void setNavigationVisible(boolean visible) {
        btnPrevSlide.setVisible(visible);
        btnNextSlide.setVisible(visible);
    }

    public Node getBtnStart() {
        return btnStart;
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndexPy;
    }

    public StringProperty selectButtonTextProperty() {
        return selectButtonTextPy;
    }

    public void setOnPrevSlideSelected(Runnable action) {
        onPrevSlide = action;
    }

    public void setOnNextSlideSelected(Runnable action) {
        onNextSlide = action;
    }

    public void setOnSelect(Runnable action) {
        btnStart.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                action.run();
            }
            e.consume();
        });
    }

    public void prevSlide() {
        int prevIndex = selectedIndexPy.get() > 0 ? selectedIndexPy.get() - 1: slides.size() - 1;
        selectedIndexPy.set(prevIndex);
        updateUI();
        if (onPrevSlide != null) {
            onPrevSlide.run();
        }
    }

    public void nextSlide() {
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