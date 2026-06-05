/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.ui.AppConstants;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariant.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.core.GameVariant.ARCADE_PACMAN_XXL;
import static de.amr.pacmanfx.core.Globals.TS;
import static de.amr.pacmanfx.model.world.WorldMapSelectionMode.*;
import static java.util.Objects.requireNonNull;

public class PacManXXL_OptionMenu extends OptionMenu {

    public static final int NUM_TILES_X = 42;
    public static final int NUM_TILES_Y = 34;

    public static final int TEXT_COLUMN  = 6;
    public static final int VALUE_COLUMN = 20;

    public static final int CHASE_ANIMATION_Y = (NUM_TILES_Y - 12) * TS;

    private final OptionMenuEntry<GameVariant>           entryGameVariant;
    private final OptionMenuEntry<Boolean>               entryPlay3D;
    private final OptionMenuEntry<Boolean> entryCutScenes;
    private final OptionMenuEntry<WorldMapSelectionMode> entryMapOrder;

    private final ChaseAnimation chaseAnimation = new ChaseAnimation(NUM_TILES_X);
    private AppContext context;

    public PacManXXL_OptionMenu() {
        super(NUM_TILES_X, NUM_TILES_Y, TEXT_COLUMN, VALUE_COLUMN);

        setTitle("Pac-Man XXL");

        // Default key code RIGHT is already used to navigate through start pages carousel
        setKeyNextValue(KeyCode.SPACE);

        defineAction(1, KeyCode.E, "OPEN EDITOR");
        defineAction(2, KeyCode.ENTER, "START");

        entryGameVariant = createGameVariantEntry();
        entryPlay3D = createSceneDisplayEntry();
        entryCutScenes = createCutScenesEntry();
        entryMapOrder = createCustomMapsEntry();

        addEntry(entryGameVariant);
        addEntry(entryPlay3D);
        addEntry(entryCutScenes);
        addEntry(entryMapOrder);

        chaseAnimation.setY(CHASE_ANIMATION_Y);
        chaseAnimation.scalingProperty().bind(scalingProperty());

        canvas.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                startDrawLoop();
            }
        });
    }

    @Override
    public void draw() {
        super.draw();
        chaseAnimation.draw();
    }

    public void init(AppContext context) {
        this.context = requireNonNull(context);

        final UIConfig currentConfig = context.currentUIConfig();
        final GameVariant gameVariant = GameVariant.valueOf(context.currentGameVariantName());
        final GameModel game = context.currentGame();

        if (!(game.mapSelector() instanceof PacManXXL_MapSelector mapSelector)) {
            final String errorMsg = "Expected XXL map selector but found %s".formatted(game.mapSelector().getClass().getSimpleName());
            Logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        mapSelector.loadMapPrototypes();

        // Init menu items
        entryGameVariant.setValue(gameVariant);
        entryPlay3D.setValue(AppConstants.PROPERTY_3D_ENABLED.get());
        entryCutScenes.setValue(game.flow().cutScenesEnabled());
        entryMapOrder.setValue(mapSelector.selectionMode());
        entryMapOrder.setEnabled(!mapSelector.customMaps().isEmpty());

        logMenuState();

        soundEnabledProperty().bind(context.ui().sounds().muteProperty().not());
        chaseAnimation.init(currentConfig, canvas, context.ui().sprites().animationSet());

        requestFocus();
    }

    public void startGame() {
        context.ui().subViews().selectGamePlayView();
        context.restartGame();
    }

    public void startDrawLoop() {
        super.startDrawLoop();
        chaseAnimation.start();
    }

    public void stopDrawLoop() {
        super.stopDrawLoop();
        chaseAnimation.stop();
    }

    public OptionMenuEntry<GameVariant> entryGameVariant() {
        return entryGameVariant;
    }

    public OptionMenuEntry<Boolean> entryPlay3D() {
        return entryPlay3D;
    }

    public OptionMenuEntry<Boolean> entryCutScenesEnabled() {
        return entryCutScenes;
    }

    // Private

    private OptionMenuEntry<GameVariant> createGameVariantEntry() {
        final var entry = new OptionMenuEntry<>(
            "GAME VARIANT",
            List.of(ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL),
            ARCADE_PACMAN_XXL)
        {
            @Override
            public void onValueChanged(GameVariant oldVariant, GameVariant newVariant) {
                if (context != null) {
                    final UIConfig uiConfig = context.ui().configurations().getOrCreateUIConfig(newVariant.name());
                    chaseAnimation.init(uiConfig, canvas, context.ui().sprites().animationSet());
                }
            }
        };

        entry.setValueFormatter(variant -> switch (variant) {
            case ARCADE_PACMAN_XXL    -> "PAC-MAN XXL";
            case ARCADE_MS_PACMAN_XXL -> "MS.PAC-MAN XXL";
            default -> "???";
        });

        return entry;
    }

    private OptionMenuEntry<Boolean> createSceneDisplayEntry() {
        final var entry = new OptionMenuEntry<>("SCENE DISPLAY", List.of(true, false), false);
        entry.setValueFormatter(play3D -> play3D ? "3D" : "2D");
        return entry;
    }

    private OptionMenuEntry<Boolean> createCutScenesEntry() {
        final var entry = new OptionMenuEntry<>("CUTSCENES", List.of(true, false), true);
        entry.setValueFormatter(enabled -> enabled ? "ON" : "OFF");
        return entry;
    }

    private OptionMenuEntry<WorldMapSelectionMode> createCustomMapsEntry() {
        final var entry = new OptionMenuEntry<>("MAP ORDER", List.of(CUSTOM_MAPS_FIRST, ALL_RANDOM, NO_CUSTOM_MAPS), CUSTOM_MAPS_FIRST);
        entry.setValueFormatter(order -> {
            if (!entry.enabled()) {
                return "NO CUSTOM MAPS!";
            }
            return switch (order) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        });
        return entry;
    }

    @Override
    public void logMenuState() {
        Logger.info("Option Menu: ({}, {}, {}, {})",
            entryGameVariant.value(),
            entryPlay3D.value() ? "3D" : "2D",
            "Cut Scenes " + (entryCutScenes.value() ? "ON" : "OFF"),
            entryMapOrder.value());
    }

}