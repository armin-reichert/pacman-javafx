/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.ui._3d.GameLevel3D;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_FLOOR_COLOR;
import static de.amr.pacmanfx.uilib.rendering.GameRenderer.fillCanvas;

public class TengenMsPacMan_GameLevel3D extends GameLevel3D {

    public TengenMsPacMan_GameLevel3D(GameUI ui) {
        super(ui);
        TengenMsPacMan_GameModel game = ui.gameContext().game();
        if (!game.optionsAreInitial()) {
            addGameInfoView();
        }
    }

    // shows info about category, difficulty, booster etc
    private void addGameInfoView() {
        TengenMsPacMan_GameModel game = ui.gameContext().game();
        int width = gameLevel.worldMap().numCols() * TS;
        int height = 2 * TS;
        int scaling = 5; // for better snapshot resolution

        var canvas = new Canvas(scaling * width, scaling * height);
        fillCanvas(canvas, PROPERTY_3D_FLOOR_COLOR.get());

        var renderer = (TengenMsPacMan_GameRenderer) ui.currentConfig().createGameRenderer(canvas);
        renderer.ctx().setImageSmoothing(false); // important for sharp image!
        renderer.setScaling(scaling);
        renderer.drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(), 0.5 * width, TS + HTS);
        renderer.drawLevelNumberBox(gameLevel.number(), 0, 0);
        renderer.drawLevelNumberBox(gameLevel.number(), width - 2 * TS, 0);

        ImageView imageView = new ImageView(canvas.snapshot(null, null));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setTranslateX(0);
        imageView.setTranslateY((gameLevel.worldMap().numRows() - 2) * TS);
        imageView.setTranslateZ(-floor3D.getDepth());

        getChildren().add(imageView);
    }
}
