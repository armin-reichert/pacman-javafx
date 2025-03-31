/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.View;
import de.amr.games.pacman.uilib.Carousel;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Background;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesCarousel implements View {

    private final GameAction actionSelectGamePage = new GameAction() {
        @Override
        public void execute() {
            THE_UI.showGameView();
        }

        @Override
        public boolean isEnabled() {
            return !THE_UI.clock().isPaused();
        }
    };

    private final List<StartPage> startPageList = new ArrayList<>();
    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private final Carousel carousel;

    public StartPagesCarousel() {
        carousel = new Carousel();
        carousel.selectedIndexProperty().addListener((py,ov,nv) -> {
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                StartPage startPage = startPageList.get(oldIndex);
                GameVariant gameVariant = (GameVariant) carousel.slide(oldIndex).getUserData();
                startPage.onExit(gameVariant);
            }
            if (newIndex != -1) {
                GameVariant gameVariant = (GameVariant) carousel.slide(newIndex).getUserData();
                startPageList.get(newIndex).onEnter(gameVariant);
            }
        });
        bindGameActions();
    }

    @Override
    public Node node() {
        return carousel;
    }

    @Override
    public void onTick() {}

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    @Override
    public void bindGameActions() {
        bind(carousel::showPreviousSlide, KeyCode.LEFT);
        bind(carousel::showNextSlide,     KeyCode.RIGHT);
        bind(actionSelectGamePage,        KeyCode.ENTER);
        bind(GameActions2D.TOGGLE_PAUSED, KeyCode.P);
    }

    @Override
    public void onGameVariantChanged(GameEvent event) {
        // TODO check if there is a cleaner solution
        THE_UI.onGameVariantChange(THE_GAME_CONTROLLER.selectedGameVariant());
    }

    public Optional<StartPage> currentStartPage() {
        if (carousel.selectedSlideIndex() == -1) {
            return Optional.empty();
        }
        return Optional.of(startPageList.get(carousel.selectedSlideIndex()));
    }

    public void addStartPage(GameVariant gameVariant, StartPage startPage) {
        assertNotNull(gameVariant);
        assertNotNull(startPage);
        if (startPageList.contains(startPage)) {
            Logger.warn("Start page {} has already been added", startPage);
            return;
        }
        startPageList.add(startPage);
        Node slide = startPage.root();
        slide.setUserData(gameVariant);
        carousel.addSlide(slide);
        carousel.setNavigationVisible(carousel.numSlides() >= 2);
        Logger.info("Start page {} added for game variant {}", startPage, gameVariant);
    }

    public void selectStartPage(int index) {
        carousel.selectedIndexProperty().set(index);
    }

    public void setBackground(Background background) {
        carousel.setBackground(background);
    }
}