/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.ui.GameAssets;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_View;
import de.amr.pacmanfx.ui.api.StartPage;
import de.amr.pacmanfx.uilib.widgets.Carousel;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
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

    public static final Color START_BUTTON_COLOR = Color.rgb(0, 155, 252, 0.7);
    public static final int PAGE_CHANGE_SECONDS = 20;

    public static Node createDefaultStartButton(GameUI ui, Runnable action) {
        final GameAssets assets = ui.assets();
        var button = new FancyButton(
            assets.translated("play_button"),
            assets.arcadeFont(30),
            START_BUTTON_COLOR,
            Color.WHITE);
        button.setAction(action);
        StackPane.setAlignment(button, Pos.BOTTOM_CENTER);
        return button;
    }

    private final GameAction actionToggleAutoPlay = new GameAction("TOGGLE_PLAY") {
        @Override
        public void execute(GameUI ui) {
            if (isPlaying()) {
                pauseTimer();
            } else {
                startTimer();
            }
            Logger.info("Carousel is {}", isPlaying() ? "playing" : "stopped");
        }
    };

    private final List<StartPage> startPageList = new ArrayList<>();
    private final ActionBindingsManager actionBindings = new DefaultActionBindingsManager();

    public StartPagesCarousel() {
        super(Duration.seconds(PAGE_CHANGE_SECONDS));
    }

    @Override
    public void onEnter() {
        GameUI_View.super.onEnter();
        restartTimer();
    }

    @Override
    public void onExit() {
        GameUI_View.super.onExit();
        pauseTimer();
    }

    public void setUI(GameUI ui) {
        requireNonNull(ui);
        selectedIndexProperty().addListener((py,ov,nv) -> {
            Logger.info("Carousel selection changed from {} to {}", ov, nv);
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                startPageList.get(oldIndex).onExit(ui);
            }
            if (newIndex != -1) {
                StartPage startPage = startPageList.get(newIndex);
                startPage.onEnter(ui);
                startPage.layoutRoot().requestFocus();
            }
        });
        setBackground(ui.assets().background("background.scene"));
        createActions();
        setOnMouseClicked(e -> {
            Logger.info("Mouse click {}", e);
            actionToggleAutoPlay.executeIfEnabled(ui);
        });
    }

    private void createActions() {
        final var actionPrevSlide = new GameAction("SHOW_PREV_SLIDE") {
            @Override
            public void execute(GameUI ui) {
                showPreviousItem();
            }
        };
        final var actionNextSlide = new GameAction("SHOW_NEXT_SLIDE") {
            @Override
            public void execute(GameUI ui) {
                showNextItem();
            }
        };
        actionBindings.setKeyCombination(actionToggleAutoPlay,       bare(KeyCode.C));
        actionBindings.setKeyCombination(actionPrevSlide,            bare(KeyCode.LEFT));
        actionBindings.setKeyCombination(actionNextSlide,            bare(KeyCode.RIGHT));
        actionBindings.setKeyCombination(ACTION_BOOT_SHOW_PLAY_VIEW, bare(KeyCode.ENTER));
        actionBindings.setKeyCombination(ACTION_TOGGLE_PAUSED,       bare(KeyCode.P));
    }

    @Override
    protected Node createNavigationButton(Direction dir) {
        final int iconSize = 48;
        final Color iconColor = Color.gray(0.69);
        final FontIcon icon = switch (dir) {
            case LEFT  -> FontIcon.of(FontAwesomeSolid.CHEVRON_CIRCLE_LEFT, iconSize, iconColor);
            case RIGHT -> FontIcon.of(FontAwesomeSolid.CHEVRON_CIRCLE_RIGHT, iconSize, iconColor);
            default -> throw new IllegalArgumentException("Illegal carousel button direction: %s".formatted(dir));
        };
        icon.setOpacity(0.2);
        icon.setOnMouseEntered(e -> icon.setOpacity(0.8));
        icon.setOnMouseExited(e -> icon.setOpacity(0.2));

        final var buttonPane = new BorderPane(icon);
        buttonPane.setMaxHeight(iconSize);
        buttonPane.setMaxWidth(iconSize);
        buttonPane.setPadding(new Insets(5));
        StackPane.setAlignment(buttonPane, dir == Direction.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return buttonPane;
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
        final String nameOfTheGame = currentStartPage().map(StartPage::title).orElse("Unknown game");
        return pattern.formatted(nameOfTheGame);
    }

    public Optional<StartPage> currentStartPage() {
        final int selectedIndex = selectedIndex();
        return selectedIndex >= 0 ? Optional.of(startPageList.get(selectedIndex)) : Optional.empty();
    }

    public void addStartPage(StartPage startPage) {
        requireNonNull(startPage);
        startPageList.add(startPage);
        addItem(startPage.layoutRoot());
        setNavigationButtonsVisible(numItems() >= 2);
        Logger.info("Start page '{}' added", startPage.getClass().getSimpleName());
    }
}