/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes.startscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.entities.Copyright;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;

import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class ArcadeMsPacMan_StartScene extends GameScene {

    private final Copyright copyright;

    public ArcadeMsPacMan_StartScene(GameAppContext app) {
        super(app);
        setComp(SceneCanvasRenderingComp.class, new SceneCanvasRenderingComp());

        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();

        copyright = new Copyright();
        copyright.show();
        copyright.pos().set(tilesPx(6), tilesPx(28));
        copyright.image().setImage(renderConfig.assets().image("logo.midway"));
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(copyright);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().currentGameVariantUIConfig()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindingsSupport().registry();
        // Insert coin + start game actions
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
    }
}