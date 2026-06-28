/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.uilib.widgets.DashboardSection;
import de.amr.pacmanfx.uilib.widgets.DashboardSectionCreator;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GameDashboardSection extends DashboardSection implements DashboardSectionCreator<GameDashboardSection> {

    public static final String NO_INFO = "n/a";

    protected final List<DynamicInfoText> dynamicInfoTexts = new ArrayList<>();

    public GameDashboardSection() {}

    @Override
    public GameDashboardSection section() {
        return this;
    }

    public void connect(Game game) {}

    public void update(Game game) {
        dynamicInfoTexts.forEach(DynamicInfoText::update);
    }

    protected Supplier<String> gameSceneInfo(Game game, Function<AbstractGameScene, String> fnInfo) {
        return () -> game.ui().gameScenes().optCurrentGameScene().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> gameLevelInfo(Game game, Function<GameLevel, String> fnInfo) {
        return () -> game.currentGameContext().optCurrentLevel().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> gameRulesInfo(Game game, Function<GameRules, String> fnInfo) {
        return () -> fnInfo.apply(game.currentGameContext().rules());
    }

    protected void dynamicInfo(String label, Supplier<?> infoSupplier) {
        var dynamicInfoText = new DynamicInfoText(infoSupplier);
        dynamicInfoTexts.add(dynamicInfoText);
        addRow(label, dynamicInfoText);
    }

    protected void setGameAction(Button button, GameAction gameAction) {
        button.setOnAction(_ -> gameAction.execute());
        //TODO add boolean property for enabled-state to game action and bind against it
    }
}