/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
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

    public GameDashboardSection(Identifier id) {
        super(id);
    }

    @Override
    public GameDashboardSection section() {
        return this;
    }

    public void setGameAppContext(GameAppContext appContext) {}

    public void update(GameAppContext appContext) {
        dynamicInfoTexts.forEach(DynamicInfoText::update);
    }

    protected Supplier<String> fnGameSceneInfo(GameAppContext appContext, Function<GameScene, String> fnInfo) {
        return () -> appContext.ui().gameScenes().optCurrentGameScene().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> fnGameLevelInfo(GameAppContext appContext, Function<GameLevel, String> fnInfo) {
        return () -> appContext.currentGameContext().model().optLevel().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> fnGameRulesInfo(GameAppContext appContext, Function<GameRules, String> fnInfo) {
        return () -> fnInfo.apply(appContext.currentGameContext().model().rules());
    }

    protected void addDynamicInfo(String label, Supplier<?> infoSupplier) {
        var dynamicInfoText = new DynamicInfoText(infoSupplier);
        dynamicInfoTexts.add(dynamicInfoText);
        addRow(label, dynamicInfoText);
    }

    protected void setGameAction(Button button, GameAction gameAction) {
        button.setOnAction(_ -> gameAction.execute());
    }
}