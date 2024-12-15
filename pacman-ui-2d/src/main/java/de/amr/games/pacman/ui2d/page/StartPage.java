/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.action.GameAction;
import de.amr.games.pacman.ui.action.GameActionProvider;
import de.amr.games.pacman.ui.action.GameActions2D;
import de.amr.games.pacman.ui.lib.Carousel;
import de.amr.games.pacman.ui.lib.Flyer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.input.Keyboard.naked;

/**
 * Got the flyer images from <a href="https://flyers.arcade-museum.com/">The Arcade Flyer Archive</a>.
 *
 * @author Armin Reichert
 */
public class StartPage extends StackPane implements GameActionProvider {

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            bindGameActions();
        }
    };

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
    private final Carousel carousel;

    public StartPage(GameContext context) {
        this.context = checkNotNull(context);
        setBackground(context.assets().get("wallpaper.pacman"));

        carousel = new Carousel(context.assets());
        carousel.selectButtonTextProperty().set(context.locText("play_button"));
        carousel.setOnPrevSlideSelected(() -> {
            var variant = (GameVariant) carousel.currentSlide().getUserData();
            context.selectGameVariant(variant);
        });
        carousel.setOnNextSlideSelected(() -> {
            var variant = (GameVariant) carousel.currentSlide().getUserData();
            context.selectGameVariant(variant);
        });
        carousel.setOnSelect(context::selectGamePage);

        getChildren().add(carousel);
        bindGameActions();
    }

    public Carousel carousel() {
        return carousel;
    }

    private Flyer currentFlyer() {
        return (Flyer) carousel.currentSlide();
    }

    @Override
    public void bindGameActions() {
        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            var joypadKeys = context.joypadKeys();
            bind(context -> currentFlyer().prevFlyerPage(),  joypadKeys.key(NES.JoypadButton.BTN_UP));
            bind(context -> currentFlyer().nextFlyerPage(),  joypadKeys.key(NES.JoypadButton.BTN_DOWN));
            bind(context -> carousel.prevSlide(),            joypadKeys.key(NES.JoypadButton.BTN_LEFT));
            bind(context -> carousel.nextSlide(),            joypadKeys.key(NES.JoypadButton.BTN_RIGHT));
            bind(actionSelectGamePage,                       joypadKeys.key(NES.JoypadButton.BTN_START));
        } else {
            bind(context -> currentFlyer().prevFlyerPage(),  context.arcadeKeys().key(Arcade.Button.UP));
            bind(context -> currentFlyer().nextFlyerPage(),  context.arcadeKeys().key(Arcade.Button.DOWN));
            bind(context -> carousel.prevSlide(),            context.arcadeKeys().key(Arcade.Button.LEFT));
            bind(context -> carousel.nextSlide(),            context.arcadeKeys().key(Arcade.Button.RIGHT));
            // START key is "1" which might be unclear on start page, so add ENTER
            bind(actionSelectGamePage,                       context.arcadeKeys().key(Arcade.Button.START), naked(KeyCode.ENTER));
        }
        bind(GameActions2D.TOGGLE_PAUSED, KeyCode.P);
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }
}