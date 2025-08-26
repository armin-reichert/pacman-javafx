package de.amr.pacmanfx.tilemap.editor;

public interface EditorActions {
    Action_AddBorderWall ADD_BORDER_WALL = new Action_AddBorderWall();
    Action_FloodWithPellets FLOOD_WITH_PELLETS = new Action_FloodWithPellets();
    Action_IdentifyObstacle IDENTIFY_OBSTACLE = new Action_IdentifyObstacle();
    Action_PlaceArcadeHouse PLACE_ARCADE_HOUSE = new Action_PlaceArcadeHouse();
}
