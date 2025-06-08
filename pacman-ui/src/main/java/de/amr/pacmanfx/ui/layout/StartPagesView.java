/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.uilib.GameAction;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.Carousel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.ui.PacManGames_Actions.SHOW_GAME_VIEW_AND_RESTART_GAME;
import static de.amr.pacmanfx.ui.PacManGames_Actions.TOGGLE_PAUSED;
import static de.amr.pacmanfx.ui.PacManGames_Env.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;
import static java.util.Objects.requireNonNull;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesView implements PacManGames_View {

    public static class FancyButton extends BorderPane {

        private GameAction action;

        public FancyButton(String buttonText, Font font, Color bgColor, Color fillColor) {
            var shadow = new DropShadow();
            shadow.setOffsetY(3.0f);
            shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));

            var text = new Text();
            text.setFill(fillColor);
            text.setFont(font);
            text.setText(buttonText);
            text.setEffect(shadow);

            setCenter(text);
            setCursor(Cursor.HAND);
            setMaxSize(200, 60);
            setPadding(new Insets(5, 5, 5, 5));
            setBackground(Ufx.coloredRoundedBackground(bgColor, 20));
            addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (action != null && action.isEnabled()) action.execute();
                e.consume();
            });
        }

        public void setAction(GameAction action) {
            this.action = action;
        }
    }

    public static FancyButton createStartButton(Pos alignment) {
        var button = new FancyButton(theAssets().text("play_button"), theAssets().arcadeFontAtSize(30),
            Color.rgb(0, 155, 252, 0.7), Color.WHITE);
        StackPane.setAlignment(button, alignment);
        return button;
    }

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

    private final List<StartPage> startPageList = new ArrayList<>();
    private final Map<KeyCombination, GameAction> actionBindings = new HashMap<>();
    private final Carousel carousel;
    private StringBinding titleBinding;

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
        setTitleBinding(Bindings.createStringBinding(() -> "JavaFX Pac-Man Games"));

        bind(carousel::showPreviousSlide,     KeyCode.LEFT);
        bind(carousel::showNextSlide,         KeyCode.RIGHT);
        bind(SHOW_GAME_VIEW_AND_RESTART_GAME, KeyCode.ENTER);
        bind(TOGGLE_PAUSED,                   KeyCode.P);
    }

    @Override
    public Region layoutRoot() { return carousel; }

    @Override
    public StringBinding titleBinding() {
        return titleBinding;
    }

    @Override
    public Map<KeyCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    public void setTitleBinding(StringBinding binding) {
        titleBinding = requireNonNull(binding);
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