/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.PacManGames_UI;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.uilib.widgets.Carousel;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
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

import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames.theKeyboard;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_BOOT_SHOW_GAME_VIEW;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_PAUSED;
import static de.amr.pacmanfx.ui.input.Keyboard.nude;
import static java.util.Objects.requireNonNull;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesView implements PacManGames_View {

    public static FancyButton createStartButton(Pos alignment) {
        var button = new FancyButton(
                theAssets().text("play_button"),
                theAssets().arcadeFont(30),
                Color.rgb(0, 155, 252, 0.7),
                Color.WHITE);
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

    public StartPagesView(PacManGames_UI ui) {
        carousel = new StartPagesCarousel();
        carousel.selectedIndexProperty().addListener((py,ov,nv) -> {
            Logger.info("Carousel selection changed from {} to {}", ov, nv);
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                startPageList.get(oldIndex).onExit();
            }
            if (newIndex != -1) {
                StartPage startPage = startPageList.get(newIndex);
                ui.selectGameVariant(startPage.currentGameVariant());
                startPage.onEnter();
                startPage.layoutRoot().requestFocus();
            }
        });
        setTitleBinding(Bindings.createStringBinding(() -> "JavaFX Pac-Man Games"));

        GameAction actionPrevSlide = new GameAction() {
            @Override
            public void execute(PacManGames_UI ui) {
                carousel.showPreviousSlide();
            }

            @Override
            public String name() {
                return "SHOW_PREV_SLIDE";
            }
        };
        GameAction actionNextSlide = new GameAction() {
            @Override
            public void execute(PacManGames_UI ui) {
                carousel.showNextSlide();
            }

            @Override
            public String name() {
                return "SHOW_NEXT_SLIDE";
            }
        };
        bindActionToKeyCombination(actionPrevSlide,            nude(KeyCode.LEFT));
        bindActionToKeyCombination(actionNextSlide,            nude(KeyCode.RIGHT));
        bindActionToKeyCombination(ACTION_BOOT_SHOW_GAME_VIEW, nude(KeyCode.ENTER));
        bindActionToKeyCombination(ACTION_TOGGLE_PAUSED,       nude(KeyCode.P));
    }

    @Override
    public void onGameEvent(GameEvent event) {}

    @Override
    public Keyboard keyboard() {
        return theKeyboard();
    }

    @Override
    public Region container() { return carousel; }

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