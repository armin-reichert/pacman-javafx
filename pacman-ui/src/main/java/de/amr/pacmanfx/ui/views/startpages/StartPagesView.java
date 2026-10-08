/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.startpages;

import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
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

    private PacManGamesEngine engine;

    private final Carousel carousel;

    public StartPagesView() {
        carousel = new Carousel();
        carousel.setId("start-pages-carousel");
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

        carousel.backButtonTooltipProperty().bind(carousel.selectedIndexProperty()
            .map(Number::intValue)
            .map(i -> i == -1 ? null : pages.get(prevIndex(i)).title()
        ));

        carousel.forwardButtonTooltipProperty().bind(carousel.selectedIndexProperty()
            .map(Number::intValue)
            .map(i -> i == -1 ? null : pages.get(nextIndex(i)).title()
        ));
    }

    @Override
    public void setEngine(PacManGamesEngine engine) {
        this.engine = requireNonNull(engine);
    }

    @Override
    public void onEnter() {
        carousel.startProgress();
        currentStartPage().ifPresent(StartPage::onEnter);
    }

    @Override
    public void onExit() {
        carousel.pauseProgress();
        actionBindings().dispose();
        currentStartPage().ifPresent(StartPage::onExit);
    }

    @Override
    public void onInput(PacManGamesEngine app) {
        currentStartPage().ifPresent(StartPage::onInput);
    }

    @Override
    public void onQuit() {}

    @Override
    public ActionBindingsRegistry actionBindings() {
        return ActionBindingsRegistry.NO_BINDINGS;
    }

    @Override
    public Carousel rootPane() { return carousel; }

    @Override
    public Optional<Supplier<String>> optTitleSupplier() {
        return Optional.of(this::composeTitle);
    }

    public void addStartPage(PacManGamesEngine appContext, StartPage startPage) {
        requireNonNull(startPage);
        if (pages.contains(startPage)) {
            Logger.warn("Start page already exists in list");
            return;
        }
        pages.add(startPage);
        carousel.getItems().add(startPage.rootPane());
        startPage.setEngine(appContext);
    }

    // Private area

    private int nextIndex(int index) {
        return (index + 1) % pages.size();
    }

    private int prevIndex(int index){
        return (index + pages.size() - 1) % pages.size();
    }

    private Optional<StartPage> currentStartPage() {
        final int selectedIndex = carousel.getSelectedIndex();
        return selectedIndex >= 0 ? Optional.of(pages.get(selectedIndex)) : Optional.empty();
    }

    private String composeTitle() {
        final String nameOfTheGame = currentStartPage().map(StartPage::title).orElse("Unknown game");
        return engine != null ? engine.translationManager().translate("startpage.title.template", nameOfTheGame) : nameOfTheGame;
    }
}