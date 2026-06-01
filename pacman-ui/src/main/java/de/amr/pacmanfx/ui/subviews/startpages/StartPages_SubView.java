/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.startpages;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.subviews.GameUI_SubView;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
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
//import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
//import org.kordamp.ikonli.javafx.FontIcon;
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
public class StartPages_SubView extends Carousel implements GameUI_SubView {

    public static final int NAV_BUTTON_SIZE = 48;

    //TODO start pages should define their preferred duration
    public static final int PAGE_CHANGE_SECONDS = 90;

    private final GameAction actionShowPrevPage = new GameAction("show_prev_page") {
        @Override
        public void doAction(AppContext context) {
            showPreviousItem();
        }
    };

    private final GameAction actionShowNextPage = new GameAction("show_next_page") {
        @Override
        public void doAction(AppContext context) {
            showNextItem();
        }
    };

    private final List<StartPage> pages = new ArrayList<>();
    private final ActionBindingsSet actionBindings = new GameActionBindingsSet("Action Bindings for Start View");

    private final AppContext context;

    public StartPages_SubView(AppContext context) {
        super(Duration.seconds(PAGE_CHANGE_SECONDS));
        this.context = requireNonNull(context);
        selectedIndexProperty().addListener((_, ov, nv) -> {
            Logger.debug("Carousel selection changed from {} to {}", ov, nv);
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                pages.get(oldIndex).onExitStartPage(context);
            }
            if (newIndex != -1) {
                StartPage startPage = pages.get(newIndex);
                startPage.onEnterStartPage(context);
                startPage.layoutRoot().requestFocus();
            }
        });
        setBackground(GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER);
    }

    @Override
    public void onEnter() {
        actionBindings.setKeyCombination(actionShowPrevPage, bare(KeyCode.LEFT));
        actionBindings.setKeyCombination(actionShowNextPage, bare(KeyCode.RIGHT));
        actionBindings.setKeyCombination(CommonActions.ACTION_BOOT_SHOW_PLAY_VIEW, bare(KeyCode.ENTER));
        Logger.info(actionBindings);
        restartProgressTimer();
        currentStartPage().ifPresent(page -> page.layoutRoot().requestFocus());
    }

    @Override
    public void onExit() {
        pauseProgressTimer();
        actionBindings.dispose();
        currentStartPage().ifPresent(startPage -> startPage.onExitStartPage(context));
    }

    @Override
    protected Node createNavigationButton(Direction dir) {
        final Color iconColor = Color.gray(0.69);
        final FontAwesomeIcon icon = switch (dir) {
            case LEFT  -> FontAwesomeIcon.of(FontAwesomeIcon.Symbol.CHEVRON_CIRCLE_LEFT, NAV_BUTTON_SIZE, iconColor);
            case RIGHT -> FontAwesomeIcon.of(FontAwesomeIcon.Symbol.CHEVRON_CIRCLE_RIGHT, NAV_BUTTON_SIZE, iconColor);
            default -> throw new IllegalArgumentException("Illegal navigation direction: %s".formatted(dir));
        };
        icon.setOpacity(0.2);
        icon.setOnMouseEntered(_ -> icon.setOpacity(0.8));
        icon.setOnMouseExited(_ -> icon.setOpacity(0.2));

        final var button = new HBox(icon);
        button.setMaxHeight(NAV_BUTTON_SIZE);
        button.setMaxWidth(NAV_BUTTON_SIZE);
        button.setPadding(new Insets(5));
        StackPane.setAlignment(button, dir == Direction.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return button;
    }

    @Override
    public ActionBindingsSet actionBindings() {
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
        addItem(startPage.layoutRoot());
        setNavigationButtonsVisible(numItems() >= 2);
        Logger.debug("Start page '{}' added", startPage.getClass().getSimpleName());
    }

    private String composeTitle() {
        final String nameOfTheGame = currentStartPage().map(StartPage::title).orElse("Unknown game");
        return context != null ? context.ui().translations().translate("startpage.title.template", nameOfTheGame) : nameOfTheGame;
    }
}