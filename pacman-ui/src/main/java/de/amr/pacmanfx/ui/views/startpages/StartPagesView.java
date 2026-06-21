/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.startpages;

import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.uilib.controls.Carousel;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesView implements GameView {

    //TODO start pages should define their preferred duration
    public static final int PAGE_CHANGE_SECONDS = 90;

    private final List<StartPage> pages = new ArrayList<>();
    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Start View Action Bindings");

    private Game game;

    private final Carousel carousel;

    public StartPagesView() {
        carousel = new Carousel();
        carousel.setId("start-pages-carousel");
        carousel.setBackground(GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER);
        carousel.setChangeDuration(PAGE_CHANGE_SECONDS);

        carousel.selectedIndexProperty().addListener((_, ov, nv) -> {
            final int oldSelection = ov.intValue();
            final int newSelection = nv.intValue();
            if (oldSelection != -1) {
                pages.get(oldSelection).onExit();
            }
            if (newSelection != -1) {
                pages.get(newSelection).onEnter();
            }
        });
    }

    @Override
    public void connect(Game game) {
        this.game = requireNonNull(game);
    }

    @Override
    public void onEnter() {
        carousel.startProgress();
    }

    @Override
    public void onExit() {
        carousel.pauseProgress();
        actionBindings.dispose();
        currentStartPage().ifPresent(StartPage::onExit);
    }

    @Override
    public void handleQuit(Game game) {}

    @Override
    public void onInput(Input input) {
        actionBindings.findActionMatchingPressedKeys(input.keyboard()).ifPresent(GameAction::execute);
    }

    @Override
    public ActionBindingsRegistry actionBindings() {
        return actionBindings;
    }

    @Override
    public Carousel rootPane() { return carousel; }

    @Override
    public Optional<Supplier<String>> optTitleSupplier() {
        return Optional.of(this::composeTitle);
    }

    public void addStartPage(Game game, StartPage startPage) {
        requireNonNull(startPage);
        if (pages.contains(startPage)) {
            Logger.warn("Start page already exists in list");
            return;
        }
        pages.add(startPage);
        carousel.getItems().add(startPage.rootPane());
        //carousel.setNavigationButtonsVisible(carousel.numItems() >= 2);
        startPage.connect(game);
    }

    private Optional<StartPage> currentStartPage() {
        final int selectedIndex = carousel.getSelectedIndex();
        return selectedIndex >= 0 ? Optional.of(pages.get(selectedIndex)) : Optional.empty();
    }

    private String composeTitle() {
        final String nameOfTheGame = currentStartPage().map(StartPage::title).orElse("Unknown game");
        return game != null ? game.ui().translations().translate("startpage.title.template", nameOfTheGame) : nameOfTheGame;
    }
}