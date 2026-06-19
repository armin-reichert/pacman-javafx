/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.startpages;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.uilib.widgets.Carousel;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeSymbol;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static java.util.Objects.requireNonNull;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesView extends Carousel implements GameView {

    //TODO start pages should define their preferred duration
    public static final int PAGE_CHANGE_SECONDS = 90;

    private final List<StartPage> pages = new ArrayList<>();
    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Start View Action Bindings");

    private Game game;
    private GameAction actionShowPrevPage;
    private GameAction actionShowNextPage;

    public StartPagesView() {
        super(PAGE_CHANGE_SECONDS);

        setId("start-pages-carousel");

        setBackground(GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER);

        selectedIndexProperty().addListener((_, ov, nv) -> {
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                pages.get(oldIndex).onExit();
            }
            if (newIndex != -1) {
                final StartPage startPage = pages.get(newIndex);
                startPage.connect(game);
                startPage.onEnter();
                startPage.rootPane().requestFocus();
            }
        });
    }

    @Override
    public void connect(Game game) {
        this.game = requireNonNull(game);

        actionShowPrevPage = new GameAction(game, "show_prev_page") {
            @Override
            public void doAction() {
                showPreviousItem();
            }
        };

        actionShowNextPage = new GameAction(game, "show_next_page") {
            @Override
            public void doAction() {
                showNextItem();
            }
        };
    }

    @Override
    public void onEnter() {
        actionBindings.bindActionToKeyCombination(actionShowPrevPage, bareKey(KeyCode.LEFT));
        actionBindings.bindActionToKeyCombination(actionShowNextPage, bareKey(KeyCode.RIGHT));
        Logger.info(actionBindings);

        restartProgressTimer();
        currentStartPage().ifPresent(page -> {
            page.startButton().ifPresentOrElse(
                startButton -> {
                    Logger.info("Request focus for start button of start page {}", page);
                    Platform.runLater(startButton::requestFocus);
                },
                () -> {
                    Logger.info("Request focus for root pane of start page {}", page);
                    Platform.runLater(() -> page.rootPane().requestFocus());
                }
            );
        });
    }

    @Override
    public void onExit() {
        pauseProgressTimer();
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
    public Region rootPane() { return this; }

    @Override
    public Optional<Supplier<String>> optTitleSupplier() {
        return Optional.of(this::composeTitle);
    }

    public Optional<StartPage> currentStartPage() {
        final int selectedIndex = selectedIndex();
        return selectedIndex >= 0 ? Optional.of(pages.get(selectedIndex)) : Optional.empty();
    }

    public void addStartPage(StartPage startPage) {
        requireNonNull(startPage);
        pages.add(startPage);
        addItem(startPage.rootPane());
        setNavigationButtonsVisible(numItems() >= 2);
        Logger.debug("Start page '{}' added", startPage.getClass().getSimpleName());
    }

    private String composeTitle() {
        final String nameOfTheGame = currentStartPage().map(StartPage::title).orElse("Unknown game");
        return game != null ? game.ui().translations().translate("startpage.title.template", nameOfTheGame) : nameOfTheGame;
    }
}