/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_StartPage;
import de.amr.pacmanfx.ui.api.GameUI_View;
import de.amr.pacmanfx.uilib.widgets.Carousel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_BOOT_SHOW_PLAY_VIEW;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_TOGGLE_PAUSED;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;
import static java.util.Objects.requireNonNull;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesCarousel extends Carousel implements GameUI_View {

    //TODO start pages should define their preferred duration
    public static final int PAGE_CHANGE_SECONDS = 90;

    private final GameAction actionShowPrevPage = new GameAction("SHOW_PREV_PAGE") {
        @Override
        public void execute(GameUI ui) {
            showPreviousItem();
        }
    };

    private final GameAction actionShowNextPage = new GameAction("SHOW_NEXT_PAGE") {
        @Override
        public void execute(GameUI ui) {
            showNextItem();
        }
    };

    private final List<GameUI_StartPage> pages = new ArrayList<>();
    private final ActionBindingsManager actionBindings = new DefaultActionBindingsManager();

    public StartPagesCarousel() {
        super(Duration.seconds(PAGE_CHANGE_SECONDS));
    }

    @Override
    public void onEnter() {
        actionBindings.useKeyCombination(actionShowPrevPage,         bare(KeyCode.LEFT));
        actionBindings.useKeyCombination(actionShowNextPage,         bare(KeyCode.RIGHT));
        actionBindings.useKeyCombination(ACTION_BOOT_SHOW_PLAY_VIEW, bare(KeyCode.ENTER));
        actionBindings.useKeyCombination(ACTION_TOGGLE_PAUSED,       bare(KeyCode.P));
        actionBindings.attach(GameUI.KEYBOARD);
        restartTimer();
    }

    @Override
    public void onExit() {
        pauseTimer();
        actionBindings.release(GameUI.KEYBOARD);
    }

    public void setUI(GameUI ui) {
        selectedIndexProperty().addListener((py,ov,nv) -> {
            Logger.info("Carousel selection changed from {} to {}", ov, nv);
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                pages.get(oldIndex).onExit(ui);
            }
            if (newIndex != -1) {
                GameUI_StartPage startPage = pages.get(newIndex);
                startPage.onEnter(ui);
                startPage.layoutRoot().requestFocus();
            }
        });
        setBackground(GameUI.BACKGROUND_PAC_MAN_WALLPAPER);
    }

    @Override
    protected Node createNavigationButton(Direction dir) {
        final int iconSize = 48;
        final Color iconColor = Color.gray(0.69);
        final FontIcon icon = switch (dir) {
            case LEFT  -> FontIcon.of(FontAwesomeSolid.CHEVRON_CIRCLE_LEFT, iconSize, iconColor);
            case RIGHT -> FontIcon.of(FontAwesomeSolid.CHEVRON_CIRCLE_RIGHT, iconSize, iconColor);
            default -> throw new IllegalArgumentException("Illegal navigation direction: %s".formatted(dir));
        };
        icon.setOpacity(0.2);
        icon.setOnMouseEntered(e -> icon.setOpacity(0.8));
        icon.setOnMouseExited(e -> icon.setOpacity(0.2));

        final var button = new HBox(icon);
        button.setMaxHeight(iconSize);
        button.setMaxWidth(iconSize);
        button.setPadding(new Insets(5));
        StackPane.setAlignment(button, dir == Direction.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return button;
    }

    @Override
    public ActionBindingsManager actionBindingsManager() {
        return actionBindings;
    }

    @Override
    public Region root() { return this; }

    @Override
    public Optional<Supplier<String>> titleSupplier() {
        return Optional.of(this::supplyTitle);
    }

    private String supplyTitle() {
        final String pattern = "JavaFX Pac-Man Games: %s";
        final String nameOfTheGame = currentStartPage().map(GameUI_StartPage::title).orElse("Unknown game");
        return pattern.formatted(nameOfTheGame);
    }

    public Optional<GameUI_StartPage> currentStartPage() {
        final int selectedIndex = selectedIndex();
        return selectedIndex >= 0 ? Optional.of(pages.get(selectedIndex)) : Optional.empty();
    }

    public void addStartPage(GameUI_StartPage startPage) {
        requireNonNull(startPage);
        pages.add(startPage);
        addItem(startPage.layoutRoot());
        setNavigationButtonsVisible(numItems() >= 2);
        Logger.info("Start page '{}' added", startPage.getClass().getSimpleName());
    }
}