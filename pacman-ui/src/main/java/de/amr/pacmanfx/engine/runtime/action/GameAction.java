/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.pacmanfx.core.Validations;

/**
 * Common base class for game actions.
 */
public abstract class GameAction {

    protected final String id;

    protected GameAction(String id) {
        this.id = Validations.requireValidIdentifier(id);
    }

    public final String id() {
        return id;
    }

    public final String resourceBundleKey() { return "action." + id; }

    @Override
    public String toString() {
        return "GameAction{" + "id='" + id + '\'' + '}';
    }

    public abstract void execute(GameActionExecutionContext context);

    public boolean isEnabled(GameActionExecutionContext context) { return true; }
}