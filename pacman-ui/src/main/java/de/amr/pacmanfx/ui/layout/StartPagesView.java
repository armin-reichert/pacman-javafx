/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.uilib.GameAction;
import de.amr.pacmanfx.uilib.widgets.Carousel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.ui.PacManGames_Actions.TOGGLE_PAUSED;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.uilib.Ufx.createRoundedButton;
import static java.util.Objects.requireNonNull;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesView implements PacManGames_View {

    private static Color START_BUTTON_BACKGROUND_COLOR = Color.rgb(0, 155, 252, 0.7);
    private static Color START_BUTTON_FILL_COLOR = Color.WHITE;


    private static class StartPagesCarousel extends Carousel {
        @Override
        protected Node createCarouselButton(Direction dir) {
            int size = 48;
            FontIcon icon = switch (dir) {
                case LEFT -> FontIcon.of(FontAwesomeSolid.CHEVRON_CIRCLE_LEFT, size, Color.gray(0.69));
                case RIGHT -> FontIcon.of(FontAwesomeSolid.CHEVRON_CIRCLE_RIGHT, size, Color.gray(0.69));
                default -> throw new IllegalArgumentException("Illegal carousel button direction: %s".formatted(dir));
            };
            icon.setOpacity(0.2);
            icon.setOnMouseEntered(e -> icon.setOpacity(0.8));
            icon.setOnMouseExited(e -> icon.setOpacity(0.2));
            var box = new BorderPane(icon);
            box.setMaxHeight(size);
            box.setMaxWidth(size);
            box.setPadding(new Insets(5));
            StackPane.setAlignment(box, dir == Direction.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
            return box;
        }
    }

    public static Node createStartButton(Pos alignment, double y, GameAction action) {
        Node button = createRoundedButton(
            theAssets().text("play_button"),
            theAssets().arcadeFontAtSize(30),
            START_BUTTON_BACKGROUND_COLOR, START_BUTTON_FILL_COLOR,
            () -> { if (action.isEnabled()) action.execute(); }
        );
        button.setTranslateY(y);
        StackPane.setAlignment(button, alignment);
        return button;
    }

    private final GameAction actionSelectGamePage = new GameAction() {
        @Override
        public void execute() {
            theUI().showGameView();
        }

        @Override
        public boolean isEnabled() {
            return !theClock().isPaused();
        }
    };

    private final List<StartPage> startPageList = new ArrayList<>();
    private final Map<KeyCombination, GameAction> actionBindings = new HashMap<>();
    private final Carousel carousel;
    private StringExpression titleExpression;

    public StartPagesView() {
        carousel = new StartPagesCarousel();
        carousel.selectedIndexProperty().addListener((py,ov,nv) -> {
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                StartPage startPage = startPageList.get(oldIndex);
                startPage.onExit();
            }
            if (newIndex != -1) {
                StartPage startPage = startPageList.get(newIndex);
                theUI().selectGameVariant(startPage.currentGameVariant());
                startPage.requestFocus();
                startPage.onEnter();
            }
        });
        setTitleExpression(Bindings.createStringBinding(() -> "JavaFX Pac-Man Games"));

        bind(carousel::showPreviousSlide, KeyCode.LEFT);
        bind(carousel::showNextSlide,     KeyCode.RIGHT);
        bind(actionSelectGamePage,        KeyCode.ENTER);
        bind(TOGGLE_PAUSED,               KeyCode.P);
    }

    @Override
    public Region layoutRoot() {
        return carousel;
    }

    @Override
    public void update() {}

    @Override
    public StringExpression title() {
        return titleExpression;
    }

    @Override
    public Map<KeyCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    public void setTitleExpression(StringExpression stringExpression) {
        titleExpression = requireNonNull(stringExpression);
    }

    public Optional<StartPage> currentStartPage() {
        int selectedIndex = carousel.selectedIndex();
        return selectedIndex >= 0 ? Optional.of(startPageList.get(selectedIndex)) : Optional.empty();
    }

    public void addStartPage(StartPage startPage) {
        requireNonNull(startPage);
        startPageList.add(startPage);
        carousel.addSlide(startPage.layoutRoot());
        carousel.setNavigationVisible(carousel.numSlides() >= 2);
        Logger.debug("Start page {} added", startPage.getClass().getSimpleName());
    }

    public void selectStartPage(int index) {
        carousel.setSelectedIndex(index);
    }

    public void setBackground(Background background) {
        carousel.setBackground(background);
    }
}