/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author Armin Reichert
 */
public class TileMapPath implements Iterable<Direction> {

    private final Vector2i startTile;
    private final List<Direction> directions = new ArrayList<>();

    public TileMapPath(Vector2i startTile) {
        this.startTile = Objects.requireNonNull(startTile);
    }

    public Vector2i startTile() {
        return startTile;
    }

    public int size() {
        return directions.size();
    }

    public void add(Direction dir) {
        Objects.requireNonNull(dir);
        directions.add(dir);
    }

    public Direction dir(int i) {
        return directions.get(i);
    }

    @Override
    public Iterator<Direction> iterator() {
        return directions.iterator();
    }
}