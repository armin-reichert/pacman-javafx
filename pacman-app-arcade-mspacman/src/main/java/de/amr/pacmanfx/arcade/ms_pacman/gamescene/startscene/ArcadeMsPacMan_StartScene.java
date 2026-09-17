/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.startscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.uilib.entities.ImageDisplay;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.assets.AssetMap;

import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class ArcadeMsPacMan_StartScene extends AbstractGameScene {

    private final StartSceneText sceneText;
    private final ImageDisplay copyrightImage;

    public ArcadeMsPacMan_StartScene(GameApp app) {
        super(app);

        // Add 2D rendering support
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());

        final AssetMap assets = app.variantManager().currentRuntime().uiConfig().assets();

        sceneText = new StartSceneText(6, 16);

        copyrightImage = new ImageDisplay();
        copyrightImage.show();
        copyrightImage.pos().set(tilesPx(6), tilesPx(28));
        copyrightImage.image().setImage(assets.image("logo.midway"));
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(sceneText, copyrightImage);
    }

    @Override
    public void onActivate() {
        // Bind "insert coin" + "start game" actions
        final Arcade_Actions actions = app.variantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        actionBindings().registry().registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
        actionBindings().registry().dispose();
    }

    @Override
    public void onTick(GameContext game) {}
}