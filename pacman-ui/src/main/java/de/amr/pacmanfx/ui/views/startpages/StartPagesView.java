/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.startpages;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.engine.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.ui.EngineContext;
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

    private final TranslationManager translationManager;
    private final Carousel carousel;

    public StartPagesView(TranslationManager translationManager) {
        this.translationManager = requireNonNull(translationManager);

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
    public void handleGameEvent(GameEvent gameEvent) {}

    public void addPage(EngineContext engine, StartPage startPage) {
        requireNonNull(engine);
        requireNonNull(startPage);

        if (pages.contains(startPage)) {
            Logger.warn("Start page already added to view");
            return;
        }
        pages.add(startPage);
        carousel.getItems().add(startPage.rootPane());
        startPage.setEngine(engine);
    }

    public List<StartPage> pages() {
        return pages;
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
    public void onInput(Input input) {
        currentStartPage().ifPresent(startPage -> startPage.onInput(input));
    }

    @Override
    public void onQuit() {}

    @Override
    public ActionBindingsRegistry<EngineContext> actionBindings() {
        return ActionBindingsRegistry.empty();
    }

    @Override
    public Carousel rootPane() { return carousel; }

    @Override
    public Optional<Supplier<String>> optTitleSupplier() {
        return Optional.of(this::composeTitle);
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
        return translationManager.translate("startpage.title.template", nameOfTheGame);
    }
}