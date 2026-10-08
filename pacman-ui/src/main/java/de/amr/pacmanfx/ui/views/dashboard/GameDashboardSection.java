/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.PacManGameEngineContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GameDashboardSection extends DashboardSection implements DashboardSectionCreator<GameDashboardSection> {

    public static final String NO_INFO = "n/a";

    protected final List<DynamicInfoText> dynamicInfoTexts = new ArrayList<>();

    public GameDashboardSection(Named id) {
        super(id);
    }

    @Override
    public GameDashboardSection section() {
        return this;
    }

    public void setGameApp(PacManGameEngineContext app) {}

    public void update(PacManGameEngineContext app) {
        dynamicInfoTexts.forEach(DynamicInfoText::update);
    }

    protected Supplier<String> fnGameSceneInfo(PacManGameEngineContext app, Function<GameScene, String> fnInfo) {
        return () -> app.gameSceneManager().optCurrentGameScene().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<?> fnLevelInfo(PacManGameEngineContext app, Function<GameLevel, Object> fnInfo) {
        return () -> app.game().session().optLevel().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> fnRulesInfo(PacManGameEngineContext app, Function<GameRules, String> fnInfo) {
        return () -> fnInfo.apply(app.game().playConfig().rules());
    }

    protected void addDynamicInfo(String label, Supplier<?> infoSupplier) {
        var dynamicInfoText = new DynamicInfoText(infoSupplier);
        dynamicInfoTexts.add(dynamicInfoText);
        addRow(label, dynamicInfoText);
    }

    protected void setGameAction(PacManGameEngineContext app, Button button, GameAction gameAction) {
        button.setOnAction(_ -> app.runAction(gameAction));
    }
}