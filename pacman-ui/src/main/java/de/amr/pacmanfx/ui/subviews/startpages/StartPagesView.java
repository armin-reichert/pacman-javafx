/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.startpages;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.ui.action.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.action.GameActionBindingsMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameGlobals;
import de.amr.pacmanfx.ui.subviews.SubView;
import de.amr.pacmanfx.uilib.widgets.Carousel;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
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
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui.input.Keyboard.bare;
import static java.util.Objects.requireNonNull;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesView extends Carousel implements SubView {

    public static final int NAV_BUTTON_SIZE = 48;

    //TODO start pages should define their preferred duration
    public static final int PAGE_CHANGE_SECONDS = 90;

    private final GameAction actionShowPrevPage = new GameAction("show_prev_page") {
        @Override
        public void doAction(Game game) {
            showPreviousItem();
        }
    };

    private final GameAction actionShowNextPage = new GameAction("show_next_page") {
        @Override
        public void doAction(Game game) {
            showNextItem();
        }
    };

    private final List<StartPage> pages = new ArrayList<>();
    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for Start View");

    private final Game game;

    public StartPagesView(Game game) {
        super(Duration.seconds(PAGE_CHANGE_SECONDS));
        this.game = requireNonNull(game);
        selectedIndexProperty().addListener((_, ov, nv) -> {
            Logger.debug("Carousel selection changed from {} to {}", ov, nv);
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                pages.get(oldIndex).onExitStartPage(game);
            }
            if (newIndex != -1) {
                StartPage startPage = pages.get(newIndex);
                startPage.onEnterStartPage(game);
                startPage.rootPane().requestFocus();
            }
        });
        setBackground(GameGlobals.BACKGROUND_PAC_MAN_WALLPAPER);
    }

    @Override
    public void onEnter() {
        actionBindings.bindActionToKeyCombination(actionShowPrevPage, bare(KeyCode.LEFT));
        actionBindings.bindActionToKeyCombination(actionShowNextPage, bare(KeyCode.RIGHT));
        actionBindings.bindActionToKeyCombination(CommonActions.ACTION_BOOT_SHOW_PLAY_VIEW, bare(KeyCode.ENTER));
        Logger.info(actionBindings);
        restartProgressTimer();
        currentStartPage().ifPresent(page -> {
            page.rootPane().requestFocus();
            Logger.info("Request focus for root pane of start page {}", page);
        });
    }

    @Override
    public void onExit() {
        pauseProgressTimer();
        actionBindings.dispose();
        currentStartPage().ifPresent(startPage -> startPage.onExitStartPage(game));
    }

    @Override
    protected Node createNavigationButton(Direction dir) {
        final Color iconColor = Color.gray(0.69);
        final FontAwesomeIcon icon = switch (dir) {
            case LEFT  -> new FontAwesomeIcon(FontAwesomeIcon.Symbol.CHEVRON_CIRCLE_LEFT, NAV_BUTTON_SIZE);
            case RIGHT -> new FontAwesomeIcon(FontAwesomeIcon.Symbol.CHEVRON_CIRCLE_RIGHT, NAV_BUTTON_SIZE);
            default -> throw new IllegalArgumentException("Illegal navigation direction: %s".formatted(dir));
        };
        icon.fillProperty().set(iconColor);
        icon.node().setOpacity(0.2);
        icon.node().setOnMouseEntered(_ -> icon.node().setOpacity(0.8));
        icon.node().setOnMouseExited(_ -> icon.node().setOpacity(0.2));

        final var button = new HBox(icon.node());
        button.setMaxHeight(NAV_BUTTON_SIZE);
        button.setMaxWidth(NAV_BUTTON_SIZE);
        button.setPadding(new Insets(5));
        StackPane.setAlignment(button, dir == Direction.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return button;
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

    public Stream<StartPage> startPages() {
        return pages.stream();
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