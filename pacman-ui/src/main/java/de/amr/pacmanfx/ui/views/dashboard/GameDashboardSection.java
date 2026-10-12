/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineUIContext;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static de.amr.pacmanfx.engine.action.ActionExecutor.runAction;

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

    public void init(EngineUIContext context) {}

    public void update(EngineUIContext context) {
        dynamicInfoTexts.forEach(DynamicInfoText::update);
    }

    protected Supplier<String> fnGameSceneInfo(EngineUIContext context, Function<GameScene, String> fnInfo) {
        return () -> context.gameSceneManager().optCurrentGameScene().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<?> fnLevelInfo(EngineUIContext context, Function<GameLevel, Object> fnInfo) {
        return () -> context.optCurrentGame()
            .flatMap(game -> game.session().optLevel())
            .map(fnInfo)
            .orElse(NO_INFO);
    }

    protected Supplier<String> fnRulesInfo(EngineUIContext context, Function<GameRules, String> fnInfo) {
        return () -> fnInfo.apply(context.optCurrentGame().orElseThrow().playConfig().gameRules());
    }

    protected void addDynamicInfo(String label, Supplier<?> infoSupplier) {
        var dynamicInfoText = new DynamicInfoText(infoSupplier);
        dynamicInfoTexts.add(dynamicInfoText);
        addRow(label, dynamicInfoText);
    }

    protected void setGameAction(EngineUIContext engineContext, Button button, GameAction<EngineUIContext> action) {
        button.setOnAction(_ -> runAction(action, engineContext));
    }
}