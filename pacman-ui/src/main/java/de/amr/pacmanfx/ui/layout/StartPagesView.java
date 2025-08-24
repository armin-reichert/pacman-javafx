/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.ui.AbstractGameAction;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.widgets.Carousel;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_BOOT_SHOW_PLAY_VIEW;
import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_TOGGLE_PAUSED;
import static de.amr.pacmanfx.ui.input.Keyboard.nude;
import static java.util.Objects.requireNonNull;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesView implements GameUI_View {

    private static final Supplier<String> TITLE_SUPPLIER = () -> "JavaFX Pac-Man Games";

    public static FancyButton createStartButton(GlobalAssets assets, Pos alignment) {
        var button = new FancyButton(
                assets.translated("play_button"),
                assets.arcadeFont(30),
                Color.rgb(0, 155, 252, 0.7),
                Color.WHITE);
        StackPane.setAlignment(button, alignment);
        return button;
    }

    private static class StartPagesCarousel extends Carousel {
        @Override
        protected Node createCarouselButton(Direction dir) {
            int size = 48;
            FontIcon icon = switch (dir) {
                case LEFT -> FontIcon.of(FontAwesomeSolid.CHEVRON_CIRCLE_LEFT, size, Color.gray(0.69));
                case RIGHT -> FontIcon.of(FontAwesomeSolid.CHEVRON_CIRCLE_RIGHT, size, Color.gray(0.69));
                default -> throw new IllegalArgumentException("Illegal carousel button direction: %s".formatted(dir));
            };
            icon.setOpacity(0.2);
            icon.setOnMouseEntered(e -> icon.setOpacity(0.8));
            icon.setOnMouseExited(e -> icon.setOpacity(0.2));
            var box = new BorderPane(icon);
            box.setMaxHeight(size);
            box.setMaxWidth(size);
            box.setPadding(new Insets(5));
            StackPane.setAlignment(box, dir == Direction.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
            return box;
        }
    }

    private final List<StartPage> startPageList = new ArrayList<>();
    private final ActionBindingsManager actionBindings;
    private final Carousel carousel;

    public StartPagesView(GameUI ui) {
        requireNonNull(ui);
        this.actionBindings = new DefaultActionBindingsManager();
        carousel = new StartPagesCarousel();
        carousel.selectedIndexProperty().addListener((py,ov,nv) -> {
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

        AbstractGameAction actionPrevSlide = new AbstractGameAction("SHOW_PREV_SLIDE") {
            @Override
            public void execute(GameUI ui) {
                carousel.showPreviousSlide();
            }
        };

        AbstractGameAction actionNextSlide = new AbstractGameAction("SHOW_NEXT_SLIDE") {
            @Override
            public void execute(GameUI ui) {
                carousel.showNextSlide();
            }
        };

        actionBindings.bind(actionPrevSlide,            nude(KeyCode.LEFT));
        actionBindings.bind(actionNextSlide,            nude(KeyCode.RIGHT));
        actionBindings.bind(ACTION_BOOT_SHOW_PLAY_VIEW, nude(KeyCode.ENTER));
        actionBindings.bind(ACTION_TOGGLE_PAUSED,       nude(KeyCode.P));
    }

    @Override
    public ActionBindingsManager actionBindingsManager() {
        return actionBindings;
    }

    @Override
    public Region root() { return carousel; }

    @Override
    public Optional<Supplier<String>> titleSupplier() {
        return Optional.of(TITLE_SUPPLIER);
    }

    public Optional<StartPage> currentStartPage() {
        int selectedIndex = carousel.selectedIndex();
        return selectedIndex >= 0 ? Optional.of(startPageList.get(selectedIndex)) : Optional.empty();
    }

    public void addStartPage(StartPage startPage) {
        requireNonNull(startPage);
        startPageList.add(startPage);
        carousel.addSlide(startPage.layoutRoot());
        carousel.setNavigationVisible(carousel.numSlides() >= 2);
        Logger.info("Start page {} added", startPage.getClass().getSimpleName());
    }

    public boolean containsPage(StartPage page) {
        return startPageList.contains(page);
    }

    public void selectStartPage(int index) {
        carousel.setSelectedIndex(index);
    }

    public void setBackground(Background background) {
        carousel.setBackground(background);
    }
}