/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameActionProvider;
import de.amr.games.pacman.uilib.Carousel;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.ui.UIGlobals.THE_CLOCK;
import static de.amr.games.pacman.ui.UIGlobals.THE_UI;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPageSelectionView extends Carousel implements GameActionProvider {

    private final GameAction actionSelectGamePage = new GameAction() {
        @Override
        public void execute() {
            THE_UI.showGameView();
        }

        @Override
        public boolean isEnabled() {
            return !THE_CLOCK.isPaused();
        }
    };

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    public StartPageSelectionView() {
        setOnPrevSlideSelected(startPage -> {
            var variant = (GameVariant) startPage.getUserData();
            THE_UI.setGameVariant(variant);
            startPage.requestFocus();
        });
        setOnNextSlideSelected(startPage -> {
            var variant = (GameVariant) startPage.getUserData();
            THE_UI.setGameVariant(variant);
            startPage.requestFocus();
        });
        bindGameActions();
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    @Override
    public void bindGameActions() {
        bind(this::showPreviousSlide,     KeyCode.LEFT);
        bind(this::showNextSlide,         KeyCode.RIGHT);
        bind(actionSelectGamePage,        KeyCode.ENTER);
        bind(GameActions2D.TOGGLE_PAUSED, KeyCode.P);
    }

    public void addStartPage(GameVariant gameVariant, StartPage startPage) {
        Node slide = startPage.root();
        if (slides().contains(slide)) {
            Logger.warn("Start page {} is already in carousel", startPage);
            return;
        }
        slide.setUserData(gameVariant);
        addSlide(slide);

        setNavigationVisible(numSlides() >= 2);
        //TODO check this
        selectedIndexProperty().set(0);

        Logger.info("Start page {} added for game variant {}", startPage, gameVariant);
    }
}