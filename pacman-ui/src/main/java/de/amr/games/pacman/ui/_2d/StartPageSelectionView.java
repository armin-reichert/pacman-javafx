/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameActionProvider;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.uilib.Carousel;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.ui.input.Keyboard.naked;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPageSelectionView extends Carousel implements GameActionProvider {

    private final GameAction actionSelectGamePage = new GameAction() {
        @Override
        public void execute(GameContext context) {
            context.showGameView();
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return !context.gameClock().isPaused();
        }
    };

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private final GameContext context;

    public StartPageSelectionView(GameContext context) {
        this.context = assertNotNull(context);
        setOnPrevSlideSelected(startPage -> {
            var variant = (GameVariant) startPage.getUserData();
            context.selectGameVariant(variant);
            startPage.requestFocus();
        });
        setOnNextSlideSelected(startPage -> {
            var variant = (GameVariant) startPage.getUserData();
            context.selectGameVariant(variant);
            startPage.requestFocus();
        });
        bindGameActions();
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

    @Override
    public void bindGameActions() {
        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            JoypadKeyBinding joypad = context.currentJoypadKeyBinding();
            bind(context -> showPreviousSlide(), joypad.key(NES_JoypadButton.BTN_LEFT));
            bind(context -> showNextSlide(), joypad.key(NES_JoypadButton.BTN_RIGHT));
            //bind(actionSelectGamePage, joypad.key(NES_JoypadButton.BTN_START));
        } else {
            ArcadeKeyBinding arcadeKeys = context.arcadeKeys();
            bind(context -> showPreviousSlide(), arcadeKeys.keyLeft());
            bind(context -> showNextSlide(), arcadeKeys.keyRight());
            // START key is "1" which might be unclear on start page, so add ENTER
            bind(actionSelectGamePage, context.arcadeKeys().key(Arcade.Button.START), naked(KeyCode.ENTER));
        }
        // in case clock has been paused and start page selector got called, allow pause toggle
        bind(GameActions2D.TOGGLE_PAUSED, KeyCode.P);
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }
}