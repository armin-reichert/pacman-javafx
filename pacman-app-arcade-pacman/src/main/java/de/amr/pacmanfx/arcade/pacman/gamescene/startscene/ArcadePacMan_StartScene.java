/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.gamescene.startscene;

import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
import de.amr.pacmanfx.uilib.ArcadeColor;

import java.util.List;
import java.util.stream.Stream;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends AbstractGameScene {

    private final List<TextView> texts = List.of(
        createText("PUSH START BUTTON",       ArcadeColor.ORANGE.color(), 8,  6, 17),
        createText("1 PLAYER ONLY",           ArcadeColor.CYAN.color(),   8,  8, 21),
        createText("BONUS PAC-MAN FOR 10000", ArcadeColor.ROSE.color(),   8,  1, 25),
        createText("PTS",                     ArcadeColor.ROSE.color(),   6, 25, 25),
        createText("© 1980 MIDWAY MFG.CO.",   ArcadeColor.PINK.color(),   8,  4, 29)
    );

    public ArcadePacMan_StartScene() {
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
    }

    @Override
    public Stream<Renderable> renderables() {
        return texts.stream().map(GameEntityViewBuilder::propView);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().variantManager().currentRuntime()
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
}