/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_HUD_Renderer;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Style;
import de.amr.pacmanfx.ui.gamescene.d3.GameLevel3D;
import de.amr.pacmanfx.ui.gamescene.d3.Maze3D;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.allOptionsHaveDefaultValue;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D(GameAppContext app) {
        super(app);
    }

    @Override
    protected void addAdditional3DLevelElements(GameLevel3D level3D) {
        final GameSession session = game().session();

        session.optLevel().ifPresent(_ -> {
            if (!allOptionsHaveDefaultValue(session)) {
                final ImageView levelInfo = createLevelInfoView(level3D,
                    app().currentGameVariantUIConfig().renderConfig().hudStyle());
                level3D.root().getChildren().add(levelInfo);
            }
        });
    }

    private ImageView createLevelInfoView(GameLevel3D level3D, HUD_Style hudStyle) {
        final GameSession session = game().session();
        final GameLevel level = session.level();

        final ImageView levelInfo = new ImageView();
        final double infoWidth = tilesPx(level.worldMap().numCols());
        final double infoHeight = tilesPx(2);
        levelInfo.setFitWidth(infoWidth);
        levelInfo.setFitHeight(infoHeight);
        levelInfo.imageProperty().bind(app().ui().viewModel().maze3DSettings().floorColorProperty().map(
            color -> createLevelInfoImage(hudStyle, level.number(), session, infoWidth, infoHeight, color))
        );

        // Display the level info at front side of floor just over the surface
        final Maze3D maze3D = level3D.maze3D();
        levelInfo.setTranslateY(maze3D.floor3D().getHeight() - levelInfo.getFitHeight());
        levelInfo.setTranslateZ(-maze3D.floor3D().getDepth());

        return levelInfo;
    }

    private Image createLevelInfoImage(
        HUD_Style style,
        int levelNumber,
        GameSession session,
        double width,
        double height,
        Color backgroundColor)
    {
        final double quality = 6;
        final var canvas = new Canvas(quality * width, quality * height);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important for crisp image!

        final var hudRenderer = new TengenMsPacMan_HUD_Renderer(style, canvas);
        hudRenderer.setScaling(quality);
        hudRenderer.fillCanvas(backgroundColor);
        hudRenderer.drawLevelNumberBox(levelNumber, 0, 0);
        hudRenderer.drawLevelNumberBox(levelNumber, width - 2 * TS, 0);
        hudRenderer.drawGameOptions(session, 0.5 * width, tilesPx(1.5f));

        return canvas.snapshot(null, null);
    }

    @Override
    public void replaceActionBindings(GameSession session, GameLevel level) {
        final var bindingsMap = actionBindingsSupport().bindingsMap();
        bindingsMap.dispose();

        final var actions = app().currentGameVariantUIConfig().extensionValue(
            TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);

        if (session.isAttractMode()) {
            // In demo level, allow going back to options screen
            bindingsMap.selectAnyMatchingBinding(actions.actionQuitDemoLevel(), actions.localBindings());
        } else {
            bindingsMap.registerAllBindings(actions.steeringBindings());
            bindingsMap.selectAnyMatchingBinding(actions.actionTogglePacBooster(), actions.localBindings());
            bindingsMap.registerAllBindings(app().commonActions().cheatActions().bindings());
        }
        bindActions();

        Logger.info(actionBindingsSupport());
    }

    @Override
    public void updateHUD3D(GameContext game) {
        optScoresView().ifPresent(scores3D -> {
            final GameSession session = game.session();
            final Score score = session.hud().gameScore(), highScore = session.hud().highScore();
            if (score.data().isEnabled()) {
                scores3D.showScore(score.data().points(), score.data().levelNumber());
            } else {
                scores3D.showTextForScore(app().ui().translations().translate("score.game_over"),
                    Color.valueOf(NES_Palette.rgb(0x16)));
            }
            // Always show high score
            scores3D.showHighScore(highScore.data().points(), highScore.data().levelNumber());
        });
    }
}