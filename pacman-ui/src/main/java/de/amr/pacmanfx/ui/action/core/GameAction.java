/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

import de.amr.pacmanfx.core.Validations;

/**
 * Common base class for game actions.
 */
public abstract class GameAction {

    protected final String id;

    protected GameAction(String id) {
        this.id = Validations.requireValidIdentifier(id);
    }

    @Override
    public String toString() {
        return "GameAction{" + "id='" + id + '\'' + '}';
    }

    /**
     * This method has to be implemented by subclasses.
     *
     * @param app the application context
     */
    public abstract void doAction(GameAppContext app);

    /**
     * This method may be implemented by subclasses to define when this action is enabled.
     *
     * @param app application context
     * @return {@code true} if this action can be executed
     */
    public boolean isEnabled(GameAppContext app) { return true; }

    public final String id() {
        return id;
    }

    public final String resourceBundleKey() { return "action." + id; }
}