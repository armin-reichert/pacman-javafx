/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.gamescene.startscene;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.TextDisplay;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.GlobalFonts;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends AbstractGameScene {

    private final List<Renderable> texts = List.of(
        createText("PUSH START BUTTON",       ARCADE_ORANGE, 8,  6, 17),
        createText("1 PLAYER ONLY",           ARCADE_CYAN,   8,  8, 21),
        createText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   8,  1, 25),
        createText("PTS",                     ARCADE_ROSE,   6, 25, 25),
        createText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   8,  4, 29)
    );

    public ArcadePacMan_StartScene(GameApp app) {
        super(app);
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
    }

    @Override
    public Stream<Renderable> renderables() {
        return texts.stream();
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app.variantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
    }

    @Override
    public void onTick(GameContext game) {}

    private static TextDisplay createText(String text, Color color, int fontSize, float tileX, float tileY) {
        final var textDisplay = new TextDisplay();
        textDisplay.data().setFillColor(color);
        textDisplay.data().setFont(GlobalFonts.ARCADE.font(fontSize));
        textDisplay.data().setText(text);
        textDisplay.pos().set(tilesPx(tileX), tilesPx(tileY));
        textDisplay.show();
        return textDisplay;
    }
}