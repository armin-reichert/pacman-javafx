/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.clapperboard;

import de.amr.basics.ecs.GameEntityComp;

public record ClapperboardInscriptionComp(String number, String text) implements GameEntityComp { }
