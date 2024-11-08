/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Carousel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.input.Keyboard.naked;

/**
 * Got the flyer images from <a href="https://flyers.arcade-museum.com/">The Arcade Flyer Archive</a>.
 *
 * @author Armin Reichert
 */
public class StartPage extends StackPane implements Page {

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            bindGameActions();
        }
    };

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private final GameContext context;
    private final Carousel carousel;

    public StartPage(GameContext context) {
        this.context = checkNotNull(context);

        var pacManFlyer = new Flyer(
            context.assets().image("pacman.startpage.image1"),
            context.assets().image("pacman.startpage.image2"),
            context.assets().image("pacman.startpage.image3")
        );
        pacManFlyer.setUserData(GameVariant.PACMAN);
        pacManFlyer.selectFlyerPage(0);

        var msPacManFlyer = new Flyer(
            context.assets().image("ms_pacman.startpage.image1"),
            context.assets().image("ms_pacman.startpage.image2")
        );
        msPacManFlyer.setUserData(GameVariant.MS_PACMAN);
        msPacManFlyer.selectFlyerPage(0);

        var pacManXXLFlyer = new Flyer(
            context.assets().image("pacman_xxl.startpage.source")
        );
        pacManXXLFlyer.setLayoutMode(0, Flyer.LayoutMode.FILL);
        pacManXXLFlyer.setUserData(GameVariant.PACMAN_XXL);
        pacManXXLFlyer.selectFlyerPage(0);

        var msPacManTengenFlyer = new Flyer(
            context.assets().image("tengen.startpage.image1"),
            context.assets().image("tengen.startpage.image2")
        );
        msPacManTengenFlyer.setUserData(GameVariant.MS_PACMAN_TENGEN);
        msPacManTengenFlyer.selectFlyerPage(0);

        setBackground(context.assets().get("wallpaper.pacman"));

        carousel = new Carousel(context.assets());
        carousel.selectButtonTextProperty().set(context.locText("play_button"));
        carousel.addSlide(pacManFlyer);
        carousel.addSlide(msPacManFlyer);
        carousel.addSlide(pacManXXLFlyer);
        carousel.addSlide(msPacManTengenFlyer);

        carousel.setOnPrevSlideSelected(() -> {
            var variant = (GameVariant) carousel.currentSlide().getUserData();
            context.selectGameVariant(variant);
        });

        carousel.setOnNextSlideSelected(() -> {
            var variant = (GameVariant) carousel.currentSlide().getUserData();
            context.selectGameVariant(variant);
        });
        carousel.setOnSelect(context::selectGamePage);

        carousel.selectedIndexProperty().set(0);

        getChildren().add(carousel);
        bindGameActions();
    }

    private Flyer currentFlyer() {
        return (Flyer) carousel.currentSlide();
    }

    @Override
    public void bindGameActions() {
        if (context.currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            var joypad = context.joypad();
            bind(context -> currentFlyer().prevFlyerPage(),  joypad.mapControToKey(NES.Joypad.UP));
            bind(context -> currentFlyer().nextFlyerPage(),  joypad.mapControToKey(NES.Joypad.DOWN));
            bind(context -> carousel.prevSlide(),            joypad.mapControToKey(NES.Joypad.LEFT));
            bind(context -> carousel.nextSlide(),            joypad.mapControToKey(NES.Joypad.RIGHT));
            bind(GameContext::selectGamePage,                joypad.mapControToKey(NES.Joypad.START));
        } else {
            bind(context -> currentFlyer().prevFlyerPage(),  context.arcade().mapControlToKey(Arcade.Controls.UP));
            bind(context -> currentFlyer().nextFlyerPage(),  context.arcade().mapControlToKey(Arcade.Controls.DOWN));
            bind(context -> carousel.prevSlide(),            context.arcade().mapControlToKey(Arcade.Controls.LEFT));
            bind(context -> carousel.nextSlide(),            context.arcade().mapControlToKey(Arcade.Controls.RIGHT));
            // START key is "1" which might be unclear on start page, so add ENTER
            bind(GameContext::selectGamePage,                context.arcade().mapControlToKey(Arcade.Controls.START), naked(KeyCode.ENTER));
        }
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    @Override
    public Pane rootPane() {
        return this;
    }

    @Override
    public void onPageSelected() {
        if (context.gameClock().isRunning()) {
            context.gameClock().stop();
        }
    }

    @Override
    public void setSize(double width, double height) {
    }
}