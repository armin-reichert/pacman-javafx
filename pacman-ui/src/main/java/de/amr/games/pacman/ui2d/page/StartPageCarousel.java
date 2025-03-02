/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.action.GameAction;
import de.amr.games.pacman.ui2d.action.GameActionProvider;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui2d.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import de.amr.games.pacman.uilib.Carousel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.ui2d.input.Keyboard.naked;

/**
 * Carousel used to select the start page for each game variant.
 */
public class StartPageCarousel extends Carousel implements GameActionProvider {

    private final GameAction actionSelectGamePage = new GameAction() {
        @Override
        public void execute(GameContext context) {
            context.selectGamePage();
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return !context.gameClock().isPaused();
        }
    };

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private final GameContext context;

    public StartPageCarousel(PacManGamesUI ui) {
        this.context = assertNotNull(ui);
        setOnPrevSlideSelected(() -> {
            var variant = (GameVariant) currentSlide().getUserData();
            context.selectGameVariant(variant);
            currentSlide().requestFocus();
        });
        setOnNextSlideSelected(() -> {
            var variant = (GameVariant) currentSlide().getUserData();
            context.selectGameVariant(variant);
            currentSlide().requestFocus();
        });

        bindGameActions();
    }

    private StartPage startPage(int index) {
        return (StartPage) slide(index);
    }

    public void addStartPage(GameVariant variant, StartPage startPage) {
        if (slides.contains(startPage.root())) {
            Logger.warn("Start page {} already in carousel", startPage);
            return;
        }
        addSlide(startPage.root());
        startPage.root().setUserData(variant);
        setNavigationVisible(numSlides() >= 2);
        selectedIndexProperty().set(0);
        Logger.info("Start page {} added for game variant {}", startPage, variant);
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