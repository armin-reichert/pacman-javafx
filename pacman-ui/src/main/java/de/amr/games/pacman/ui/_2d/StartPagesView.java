/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.uilib.Action;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.View;
import de.amr.games.pacman.uilib.Carousel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * Carousel containing the start pages for the different game variants (XXL game variants share common start page).
 */
public class StartPagesView implements View {

    private final Action actionSelectGamePage = new Action() {
        @Override
        public void execute() {
            THE_UI.showGameView();
        }

        @Override
        public boolean isEnabled() {
            return !THE_UI.clock().isPaused();
        }
    };

    private final List<StartPage> startPageList = new ArrayList<>();
    private final Map<KeyCodeCombination, Action> actionBindings = new HashMap<>();
    private final Carousel carousel;
    private StringExpression titleExpression;

    public StartPagesView() {
        carousel = new Carousel();
        carousel.selectedIndexProperty().addListener((py,ov,nv) -> {
            int oldIndex = ov.intValue(), newIndex = nv.intValue();
            if (oldIndex != -1) {
                StartPage startPage = startPageList.get(oldIndex);
                startPage.onExit();
            }
            if (newIndex != -1) {
                StartPage startPage = startPageList.get(newIndex);
                THE_UI.selectGameVariant(startPage.currentGameVariant());
                startPage.requestFocus();
                startPage.onEnter();
            }
        });
        titleExpression = Bindings.createStringBinding(() -> "JavaFX Pac-Man Games");
        bindGameActions();
    }

    @Override
    public Region layoutRoot() {
        return carousel;
    }

    @Override
    public void update() {}

    @Override
    public StringExpression title() {
        return titleExpression;
    }

    @Override
    public Map<KeyCodeCombination, Action> actionBindings() {
        return actionBindings;
    }

    @Override
    public void bindGameActions() {
        bind(carousel::showPreviousSlide, KeyCode.LEFT);
        bind(carousel::showNextSlide,     KeyCode.RIGHT);
        bind(actionSelectGamePage,        KeyCode.ENTER);
        bind(GameAction.TOGGLE_PAUSED, KeyCode.P);
    }

    public void setTitleExpression(StringExpression stringExpression) {
        titleExpression = assertNotNull(stringExpression);
    }

    public Optional<StartPage> currentStartPage() {
        int selectedIndex = carousel.selectedIndex();
        return selectedIndex >= 0 ? Optional.of(startPageList.get(selectedIndex)) : Optional.empty();
    }

    public void addStartPage(StartPage startPage) {
        assertNotNull(startPage);
        startPageList.add(startPage);
        carousel.addSlide(startPage.root());
        carousel.setNavigationVisible(carousel.numSlides() >= 2);
        Logger.info("Start page {} added", startPage.getClass().getSimpleName());
    }

    public void selectStartPage(int index) {
        carousel.setSelectedIndex(index);
    }

    public void setBackground(Background background) {
        carousel.setBackground(background);
    }
}