/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.Difficulty;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.ui._3d.GameLevel3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_TOGGLE_PAC_BOOSTER;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_FLOOR_COLOR;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D(GameUI ui) {
        super(ui);
    }

    @Override
    protected GameLevel3D createGameLevel3D() {
        final GameLevel3D gameLevel3D = super.createGameLevel3D();

        final TengenMsPacMan_GameModel game = context().game();
        if (!game.optionsAreInitial()) {
            final ImageView infoView = createGameInfoView(game);
            final Box floor3D = gameLevel3D.floor3D();
            infoView.setTranslateX(floor3D.getTranslateX());
            infoView.setTranslateY(floor3D.getHeight() - TS(2));
            infoView.setTranslateZ(-floor3D.getDepth());
            gameLevel3D.getChildren().add(infoView);
        }
        return gameLevel3D;
    }

    // shows info about category, difficulty, booster etc
    private ImageView createGameInfoView(TengenMsPacMan_GameModel game) {
        final float width = TS(game.gameLevel().worldMap().numCols());
        final float height = TS(2);
        final ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.imageProperty().bind(PROPERTY_3D_FLOOR_COLOR.map(floorColor -> createInfoViewImage(
            game.mapCategory(), game.difficulty(), game.pacBooster(), game.gameLevel().number(), width, floorColor)));
        return imageView;
    }

    private Image createInfoViewImage(MapCategory mapCategory, Difficulty difficulty, PacBooster pacBooster, int levelNumber, double width, Color backgroundColor) {
        final double scaling = 6;
        final var canvas = new Canvas(scaling * width, scaling * 2 * TS);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important for crisp image!

        final var hudRenderer = (TengenMsPacMan_HUDRenderer) ui.currentConfig().createHUDRenderer(canvas);
        hudRenderer.scalingProperty().set(scaling);
        hudRenderer.fillCanvas(backgroundColor);
        hudRenderer.drawLevelNumberBox(levelNumber, 0, 0);
        hudRenderer.drawLevelNumberBox(levelNumber, width - 2 * TS, 0);
        hudRenderer.drawGameOptions(mapCategory, difficulty, pacBooster, TS(14), TS + HTS);

        return canvas.snapshot(null, null);
    }


    @Override
    protected void setActionBindings() {
        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        // if demo level, allow going back to options screen
        if (context().optGameLevel().isPresent() && context().gameLevel().isDemoLevel()) {
            actionBindings.useBindings(ACTION_QUIT_DEMO_LEVEL, tengenActionBindings);
        } else {
            setPlayerSteeringActionBindings();
            actionBindings.useBindings(ACTION_CHEAT_ADD_LIVES, ui.actionBindings());
            actionBindings.useBindings(ACTION_CHEAT_EAT_ALL_PELLETS, ui.actionBindings());
            actionBindings.useBindings(ACTION_CHEAT_ENTER_NEXT_LEVEL, ui.actionBindings());
            actionBindings.useBindings(ACTION_CHEAT_KILL_GHOSTS, ui.actionBindings());
            actionBindings.useBindings(ACTION_TOGGLE_PAC_BOOSTER, tengenActionBindings);
        }
        actionBindings.useBindings(ACTION_PERSPECTIVE_PREVIOUS, ui.actionBindings());
        actionBindings.useBindings(ACTION_PERSPECTIVE_NEXT, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_DRAW_MODE, ui.actionBindings());

        actionBindings.assignBindingsToKeyboard(ui.keyboard());
    }

    @Override
    protected void setPlayerSteeringActionBindings() {
        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        actionBindings.useBindings(ACTION_STEER_UP,    tengenActionBindings);
        actionBindings.useBindings(ACTION_STEER_DOWN,  tengenActionBindings);
        actionBindings.useBindings(ACTION_STEER_LEFT,  tengenActionBindings);
        actionBindings.useBindings(ACTION_STEER_RIGHT, tengenActionBindings);
    }

    @Override
    protected void updateHUD() {
        ScoreManager scoreManager = context().game().scoreManager();
        final Score score = scoreManager.score(), highScore = scoreManager.highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else {
            scores3D.showTextForScore(ui.assets().translated("score.game_over"), Color.web(NES_Palette.color(0x16)));
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }
}