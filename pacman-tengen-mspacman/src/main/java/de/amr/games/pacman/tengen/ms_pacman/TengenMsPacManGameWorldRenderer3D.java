/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.ui3d.level.WorldRenderer3D;
import javafx.scene.Group;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.vec_2f;

public class TengenMsPacManGameWorldRenderer3D extends WorldRenderer3D {

    @Override
    public void addObstacle3D(Group parent, Obstacle obstacle, double thickness) {
        switch (obstacle.encoding()) {
            // Tengen BIG map #1, upside T at top, center
            case "dcfbdcgbfcebgce" -> render_BigMap1_UpsideT(parent, obstacle);

            // Tengen BIG map #2, large desk-like obstacle on the bottom
            case "dgbecgbfebgcdbecfbdgbfcdbfeb" -> render_BigMap2_DeskLike(parent, obstacle);

            // Tengen BIG map #3, large double-T obstacle on the top
            case "dcgbecgfcdbecgfcdbfceb" -> render_BigMap3_DoubleTOnTop(parent, obstacle);

            // Tengen BIG map #5, bowl-like obstacle on the top
            case "dcgbecgbfcdbfcebdcfbgceb" -> render_BigMap5_Bowl(parent, obstacle);

            // Tengen BIG map #5, double-F on left side
            case "dcgfcdbfebgcdbfebgcdbfeb" -> render_BigMap5_DoubleFLeft(parent, obstacle);

            // Tengen BIG map #5, double-F on right side
            case "dgbecfbdgbecfbdgbecgfceb" -> render_BigMap5_DoubleFRight(parent, obstacle);

            // Tengen BIG map #5, plane-like obstacle middle bottom
            case "dcfbdgbecfbdgbfebgcdbfebgce" -> render_BigMap5_PlaneLike(parent, obstacle);

            // Tengen BIG map #6, obstacle left-top
            case "dcgfcdbfcebgce" -> render_BigMap6_LeftTopObstacle(parent, obstacle);

            // Tengen BIG map #6, obstacle right-top
            case "dcfbdcgbecgfce" -> render_BigMap6_RightTopObstacle(parent, obstacle);

            // Tengen BIG map #8, huge 62-segment obstacle on top
            case "dcgbecgbecgfcdbfcdbecgbecgfcdbfcdbfcebdcgbecfbgcebdcfbgcdbfceb" -> render_BigMap8_62SegmentObstacle(parent, obstacle);

            // Tengen BIG map #8, big-bowl obstacle middle bottom
            case "dcgbecgbfcdbfcedcfbdcfbgcebgce" -> render_BigMap8_BigBowl(parent, obstacle);

            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbfebgcdbfceb" -> render_BigMap8_SeaHorseLeft(parent, obstacle);

            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbecfbdgbfceb" -> render_BigMap8_SeaHorseRight(parent, obstacle);

            // Tengen BIG map #9, inward turned legs (left+right)
            case "dcgbfebgcdbecfbdgbfceb" -> render_BigMap9_InwardLegs(parent, obstacle);

            // Tengen BIG map #10, table upside-down, at top of maze
            case "dcfbdgbfebgcedcfbgce" -> render_BigMap10_TableUpsideDown(parent, obstacle);

            // Tengen BIG map #11, Tour Eiffel-like
            case "dcfbdcgfcdbecgfcebgce" -> render_BigMap11_TourEiffel(parent, obstacle);

            // Tengen STRANGE map #1, leg-like obstacle at left side at bottom of maze
            case "dcfbdgbfcdbfebgce" -> render_StrangeMap1_Leg_Left(parent, obstacle);

            // Tengen STRANGE map #1, leg-like obstacle at right side at bottom of maze
            case "dcfbdgbecgbfebgce" -> render_StrangeMap1_Leg_Right(parent, obstacle);

            // Tengen STRANGE map #1, Y-shaped obstacle at center at bottom of maze
            case "dgbecgbecgfcdbfcdbfebdcfbgceb" -> render_StrangeMap1_YShape(parent, obstacle);

            // Tengen STRANGE map #2, bowl-like obstacle at center at top of maze
            case "dgbecgbfcdbfebdcfbgceb" -> render_StrangeMap2_Bowl(parent, obstacle);

            // Tengen STRANGE map #2, gallow-like obstacle at left/top
            case "dcgbecgfcebgcdbfeb" -> render_StrangeMap2_Gallows_Left(parent, obstacle);

            // Tengen STRANGE map #2, gallow-like obstacle at right/top
            case "dgbecfbdcgfcdbfceb" -> render_StrangeMap2_Gallows_Right(parent, obstacle);

            // Tengen STRANGE map #3, large hat-like onstacle center/top
            case "dfbdcfbdcgfdbfcdbecgbegfcebgcebgeb" -> render_StrangeMap3_Hat(parent, obstacle);

            // Tengen STRANGE map #3, mushroom center-bottom
            case "dcgbecgbfcdbfceb" -> render_StrangeMap3_Mushroom(parent, obstacle);

            // Tengen STRANGE map #3 and #7, U-shape with stronger "legs"
            case "dcgbfcebdcfbgceb" -> render_StrangeMap3_7_StrongLeggedUShape(parent, obstacle);

            // Tengen STRANGE map #4, huge obstacle
            case "dgbecfbdcgbfcdbecgbfcebgcdbfebdgbecfbgcebgcedcfbdcfbgcdbfeb" -> render_StrangeMap4_Huge(parent, obstacle);

            // Tengen STRANGE map #7: top-left strange obstacle
            case "dcfbdgbecgbecfbdgbfcdbfeb" -> render_Strange7_TopLeft(parent, obstacle);

            // Tengen STRANGE map #7: top-right strange obstacle
            case "dgbecgbfebgcdbfcdbfebgceb" -> render_Strange7_TopRight(parent, obstacle);

            // Tengen STRANGE map #7: boots pointing left
            case "dcfbdgbfceb" -> render_Boot_PointingLeft(parent, obstacle);

            // Tengen STRANGE map #7: boots pointing left
            case "dcgbfebgceb" -> render_Boot_PointingRight(parent, obstacle);

            // STRANGE map #8: rectangle with two arms at top-right corner, left maze side
            case "dcfbdcgbfcdbfebgce" -> render_RectangleWithTwoArmsAtTopRightCorner(parent, obstacle);

            // STRANGE map #8: rectangle with two arms at top-left corner, right maze side
            case "dcfbdgbecgbfcebgce" -> render_RectangleWithTwoArmsAtTopLeftCorner(parent, obstacle);

            // STRANGE map #8: rectangle with one arm at top-right corner, left maze side
            case "dcgbfcdbfeb" -> render_RectangleWithArmAtTopRightCorner(parent, obstacle);

            // STRANGE map #8: rectangle with one arm at top-left corner, right maze side
            case "dgbecgbfceb" -> render_RectangleWithArmAtTopLeftCorner(parent, obstacle);

            // STRANGE map #9: large obstacle left maze side
            case "dcfbdgbecfbdgbecfbdgbfebgcdbfebgcdbfeb" -> render_Strange9_LargeObstacleLeft(parent, obstacle);

            // STRANGE map #9: large obstacle right maze side
            case "dgbecfbdgbecfbdgbfebgcdbfebgcdbfebgceb" -> render_Strange9_LargeObstacleRight(parent, obstacle);

            // STRANGE map #10: spaceship top, center
            case "dcfbdcgbecgfcdbfcebgceb" -> render_Strange10_SpaceShipTopCenter(parent, obstacle);

            // STRANGE map #10: island left of spaceship
            case "dcgegegfcegceb" -> render_Strange10_IslandLeftOfSpaceship(parent, obstacle);

            // STRANGE map #10: island right of spaceship
            case "dcfdcgfdfdfceb" -> render_Strange10_IslandRightOfSpaceship(parent, obstacle);

            // STRANGE map #10: left of house
            case "dcfbdgbecgfcdfceb" -> render_Strange10_RectangleWithTwoLegsAtLowerLeftEdge(parent, obstacle);

            // STRANGE map #10: right of house
            case "dcgecgfcdbfebgceb" -> render_Strange10_RectangleWithTwoLegsAtLowerRightEdge(parent, obstacle);

            default -> addGenericObstacle3D(parent, obstacle, thickness);
        }
    }

    // TODO add these to standard shapes?

    private void render_Boot_PointingLeft(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 7, 9);
        Vector2f h0 = vec_2f(t[0].x(), t[1].y());
        addTowers(parent, t);
        addWall(parent, t[0], h0);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[3], t[0]);
        addWallAtCenter(parent, t[0].midpoint(t[2]), t[0].manhattanDist(t[3]), t[3].manhattanDist(t[2]));
    }

    private void render_Boot_PointingRight(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 9);
        Vector2f h0 = vec_2f(t[3].x(), t[1].y());
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, h0, t[3]);
        addWall(parent, t[3], t[0]);
        addWallAtCenter(parent, t[1].midpoint(t[3]), t[0].manhattanDist(t[3]), t[0].manhattanDist(t[1]));
    }

    private void render_BigMap1_UpsideT(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 6, 8, 10);
        addTowers(parent, t);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[0], t[2].midpoint(t[3]));
        addWallAtCenter(parent, t[1].midpoint(t[3]), t[1].manhattanDist(t[4]), 2 * TS);
    }

    private void render_BigMap2_DeskLike(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topL = c[0], innerBottomL = c[1], innerBottomR = c[2], topR = c[3];
        Vector2f outerBottomL = innerBottomL.minus(4 * TS, 0);
        Vector2f outerBottomR = innerBottomR.plus(4 * TS, 0);
        addTowers(parent, topL, topR, innerBottomL, outerBottomL, innerBottomR, outerBottomR);
        addWall(parent, topL, topR);
        addWall(parent, outerBottomL, innerBottomL);
        addWall(parent, outerBottomR, innerBottomR);
        addWall(parent, outerBottomL, outerBottomL.minus(0, 6 * TS));
        addWall(parent, outerBottomR, outerBottomR.minus(0, 6 * TS));
    }

    private void render_BigMap3_DoubleTOnTop(Group parent, Obstacle obstacle) {
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerNE = cornerNW.plus(12 * TS, 0);
        Vector2f cornerSW = cornerNW.plus(0, TS);
        Vector2f cornerSE = cornerNE.plus(0, TS);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f bottomL = c[0], bottomR = c[1];
        addTowers(parent, cornerNW, cornerNE, cornerSW, cornerSE, bottomL, bottomR);
        addWall(parent, cornerNW, cornerSW);
        addWall(parent, cornerNE, cornerSE);
        addWall(parent, cornerNW, cornerNE);
        addWall(parent, cornerSW, cornerSE);
        addWall(parent, bottomL, bottomL.minus(0, 3 * TS));
        addWall(parent, bottomR, bottomR.minus(0, 3 * TS));
    }

    private void render_BigMap5_Bowl(Group parent, Obstacle obstacle) {
        Vector2f leftCornerNW = obstacle.cornerCenter(0);
        Vector2f leftCornerSW = leftCornerNW.plus(0, TS);
        Vector2f leftCornerNE = leftCornerNW.plus(2 * TS, 0);
        Vector2f rightCornerNW = leftCornerNW.plus(8 * TS, 0);
        Vector2f rightCornerNE = rightCornerNW.plus(2 * TS, 0);
        Vector2f rightCornerSE = rightCornerNE.plus(0, TS);
        Vector2f leftBottom = leftCornerNW.plus(2 * TS, 4 * TS);
        Vector2f rightBottom = leftBottom.plus(6 * TS, 0);
        addTowers(parent, leftCornerNW, leftCornerSW, leftCornerNE, rightCornerNW, rightCornerNE, rightCornerSE, leftBottom, rightBottom);
        addWall(parent, leftCornerNW, leftCornerNE);
        addWall(parent, leftCornerNW, leftCornerSW);
        addWall(parent, leftCornerNE, leftBottom);
        addWall(parent, rightCornerNW, rightCornerNE);
        addWall(parent, rightCornerNE, rightCornerSE);
        addWall(parent, rightCornerNW, rightBottom);
        addWall(parent, leftBottom, rightBottom);
    }

    private void render_BigMap5_DoubleFLeft(Group parent, Obstacle obstacle) {
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerSW = obstacle.cornerCenter(2);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topRight = c[3], middleRight = c[2], bottomRight = c[1];
        addTowers(parent, cornerNW, cornerSW, topRight, middleRight, bottomRight);
        addWall(parent, cornerNW, cornerSW);
        addWall(parent, cornerNW, topRight);
        addWall(parent, middleRight.minus(3 * TS, 0), middleRight);
        addWall(parent, bottomRight.minus(3 * TS, 0), bottomRight);
        addWall(parent, cornerNW, cornerSW);
    }

    private void render_BigMap5_DoubleFRight(Group parent, Obstacle obstacle) {
        Vector2f cornerNE = obstacle.cornerCenter(22);
        Vector2f cornerSE = obstacle.cornerCenter(20);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topLeft = c[0], middleLeft = c[1], bottomLeft = c[2];
        addTowers(parent, cornerNE, cornerSE, topLeft, middleLeft, bottomLeft);
        addWall(parent, cornerNE, cornerSE);
        addWall(parent, cornerNE, topLeft);
        addWall(parent, middleLeft.plus(3 *TS, 0), middleLeft);
        addWall(parent, bottomLeft.plus(3 *TS, 0), bottomLeft);
        addWall(parent, cornerNE, cornerSE);
    }

    private void render_BigMap5_PlaneLike(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f nose = c[4], leftWing = c[0], rightWing = c[3], leftBack = c[1], rightBack = c[2];
        addTowers(parent, nose, leftWing, leftBack, rightWing, rightBack);
        addWall(parent, nose, leftBack.midpoint(rightBack));
        addWall(parent, leftWing, rightWing);
        addWall(parent, leftBack, rightBack);
    }

    private void render_BigMap8_62SegmentObstacle(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 60, 58, 2, 6, 10, 15, 50, 48, 21, 25, 30, 34, 36, 38, 40);
        Vector2f[] h = new Vector2f[8];
        h[0] = vec_2f(t[4].x(),  t[2].y());
        h[1] = vec_2f(t[4].x(),  t[3].y());
        h[2] = vec_2f(t[5].x(),  t[4].y());
        h[3] = vec_2f(t[10].x(), t[9].y());
        h[4] = vec_2f(t[11].x(), t[12].y());
        h[5] = vec_2f(t[11].x(), t[15].y());
        h[6] = vec_2f(t[4].x(),  t[0].y());
        h[7] = vec_2f(t[11].x(), t[13].y());

        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, h[0], h[6]);
        addWall(parent, t[0], t[3]);
        addWall(parent, t[3], h[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], h[0]);
        addWall(parent, h[0], h[1]);
        addWall(parent, h[1], t[4]);
        addWall(parent, t[4], t[6]);
        addWall(parent, h[2], t[5]);
        addWall(parent, t[6], t[7]);
        addWall(parent, t[7], t[8]);
        addWall(parent, t[8], t[9]);
        addWall(parent, t[9], t[11]);
        addWall(parent, h[3], t[10]);
        addWall(parent, t[11], h[4]);
        addWall(parent, h[4], t[12]);
        addWall(parent, h[4], h[5]);
        addWall(parent, h[5], t[15]);
        addWall(parent, h[5], h[7]);
        addWall(parent, t[15], t[14]);
        addWall(parent, t[14], t[13]);
        addWall(parent, t[13], t[12]);
        addWall(parent, t[12], h[4]);
    }

    private void render_BigMap8_BigBowl(Group parent, Obstacle obstacle) {
        Vector2f[] p = new Vector2f[8];
        p[0] = obstacle.uTurnCenters()[1];
        p[1] = p[0].plus(0, 2 * TS);
        p[2] = p[1].plus(3 * TS, 0);
        p[3] = p[2].plus(0, 6 * TS);
        p[4] = p[3].plus(12 * TS, 0);
        p[5] = p[4].minus(0, 6 * TS);
        p[6] = p[5].plus(3 * TS, 0);
        p[7] = p[6].minus(0, 2 * TS);
        addTowers(parent, p);
        for (int i = 0; i < p.length; ++i) {
            if (i + 1 < p.length) addWall(parent, p[i], p[i+1]);
        }
    }

    private void render_BigMap8_SeaHorseLeft(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 11, 13);
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[0], t[4]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[4], t[3]);
        addWall(parent, t[3], t[3].minus(2*TS, 0));
    }

    private void render_BigMap8_SeaHorseRight(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 8, 2, 11, 13);
        addTowers(parent, t);
        addWall(parent, t[0], t[2]);
        addWall(parent, t[0], t[4]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[3], t[1]);
        addWall(parent, t[2], t[2].plus(2*TS, 0));
    }

    private void render_BigMap9_InwardLegs(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        Vector2f toeLeft = utc[0], toeRight = utc[1];
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f heelLeft = obstacle.cornerCenter(2);
        Vector2f heelRight = obstacle.cornerCenter(18);
        Vector2f cornerNE = obstacle.cornerCenter(20);
        addTowers(parent, cornerNW, cornerNE, heelLeft, toeLeft, heelRight, toeRight);
        addWall(parent, cornerNW, cornerNE);
        addWall(parent, cornerNW, heelLeft);
        addWall(parent, heelLeft, toeLeft);
        addWall(parent, cornerNE, heelRight);
        addWall(parent, heelRight, toeRight);
    }

    private void render_BigMap10_TableUpsideDown(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 8, 12);
        Vector2f h0 = vec_2f(t[0].x(), t[1].y()), h1 = vec_2f(t[3].x(), t[1].y());
        addTowers(parent, t);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[0], h0);
        addWall(parent, t[3], h1);
    }

    private void render_BigMap11_TourEiffel(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        Vector2f baseLeft = utc[0], baseRight = utc[1], top = utc[2];
        Vector2f platformLeft = obstacle.cornerCenter(4);
        Vector2f platformRight = obstacle.cornerCenter(16);
        Vector2f topBase = vec_2f(top.x(), platformLeft.y());
        addTowers(parent, top, platformLeft, platformRight, baseLeft, baseRight);
        addWall(parent, top, topBase);
        addWall(parent, platformLeft, platformRight);
        addWall(parent, platformLeft, baseLeft);
        addWall(parent, platformRight, baseRight);
    }

    private void render_StrangeMap1_Leg_Left(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 7, 11);
        Vector2f h = vec_2f(t[0].x(), t[3].y());
        addTowers(parent, t);
        addWall(parent, t[0], t[2]);
        addWall(parent, t[2], t[1]);
        addWall(parent, h, t[3]);
    }

    private void render_StrangeMap1_Leg_Right(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 11);
        Vector2f h = vec_2f(t[0].x(), t[1].y());
        addTowers(parent, t);
        addWall(parent, t[0], t[2]);
        addWall(parent, t[2], t[3]);
        addWall(parent, h, t[1]);
    }

    private void render_StrangeMap1_YShape(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 27, 5, 9, 14, 18, 21);
        Vector2f h = vec_2f(t[3].x(), t[2].y());
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], t[4]);
        addWall(parent, t[4], t[6]);
        addWall(parent, t[6], t[5]);
        addWall(parent, t[3], h);
    }

    private void render_StrangeMap2_Bowl(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 20, 5, 7, 14, 11);
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[4], t[5]);
    }

    private void render_StrangeMap2_Gallows_Right(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 16, 7, 14, 9);
        var h0 = vec_2f(t[1].x(), t[2].y());
        var h1 = vec_2f(t[2].x(), t[3].y());
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[1], t[3]);
        addWall(parent, h0, t[2]);
        addWall(parent, t[2], t[4]);
        addWall(parent, h1, t[3]);
        // fill hole
        addWallAtCenter(parent, h0.midpoint(h1), 2*TS, 3*TS);
    }

    private void render_StrangeMap2_Gallows_Left(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 15, 2, 9, 6);
        var h0 = vec_2f(t[0].x(), t[3].y());
        var h1 = vec_2f(t[3].x(), t[2].y());
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[0], t[2]);
        addWall(parent, h0, t[3]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[2], h1);
        // fill hole
        addWallAtCenter(parent, h0.midpoint(h1), 2*TS, 3*TS);
    }

    private void render_StrangeMap3_Hat(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 32, 3, 29, 7, 25, 9, 13, 19, 22);
        Vector2f[] h = new Vector2f[6];
        h[0] = t[0].plus(0, TS);
        h[1] = t[1].plus(0, TS);
        h[2] = t[2].plus(TS, 0);
        h[3] = t[3].minus(TS, 0);
        h[4] = t[7].minus(TS, 0);
        h[5] = t[8].plus(TS, 0);
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[0], h[0]);
        addWall(parent, t[1], h[1]);
        addWall(parent, h[0], h[1]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[2], h[4]);
        addWall(parent, h[2], t[7]);
        addWall(parent, t[4], t[7]);
        addWall(parent, t[4], t[6]);
        addWall(parent, h[3], t[8]);
        addWall(parent, t[3], h[5]);
        addWall(parent, t[8], t[5]);
        addWall(parent, t[5], t[9]);
    }

    private void render_BigMap6_LeftTopObstacle(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 7, 9);
        Vector2f[] h = { vec_2f(t[1].x(), t[2].y()), vec_2f(t[1].x(), t[3].y()) };
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, h[0], t[2]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[3], h[1]);
    }

    private void render_BigMap6_RightTopObstacle(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 6, 10);
        Vector2f[] h = { vec_2f(t[0].x(), t[1].y()), vec_2f(t[0].x(), t[2].y()) };
        addTowers(parent, t);
        addWall(parent, t[0], t[3]);
        addWall(parent, h[0], t[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], h[1]);
    }

    private void render_StrangeMap3_Mushroom(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 6, 8, 12, 14);
        Vector2f[] h = { vec_2f(t[2].x(), t[1].y()), vec_2f(t[3].x(), t[1].y()) };
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[1], h[0]);
        addWall(parent, h[0], t[2]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[3], h[1]);
        addWall(parent, h[1], t[4]);
        addWall(parent, t[4], t[5]);
        addWall(parent, t[5], t[0]);
        addWallAtCenter(parent, h[0].midpoint(h[1]), 2 * TS, 3 * TS);
    }

    private void render_StrangeMap3_7_StrongLeggedUShape(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 6, 8, 14);
        Vector2f[] h = { vec_2f(t[5].x(), t[1].y()), vec_2f(t[4].x(), t[2].y()) };
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[4], h[1]);
        addWall(parent, t[5], t[0]);
        addWall(parent, t[5], h[0]);
        addWallAtCenter(parent, t[1].midpoint(t[5]), t[0].manhattanDist(t[5]), t[0].manhattanDist(t[1]));
        addWallAtCenter(parent, t[2].midpoint(t[4]), t[3].manhattanDist(t[4]), t[2].manhattanDist(t[3]));
    }

    private void render_StrangeMap4_Huge(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 57, 7, 9, 11, 48, 39, 43, 17, 19, 21, 27, 30);
        Vector2f[] h = {
                t[0].plus(2 * TS, 0),
                vec_2f(t[0].x() + 2 * TS, t[2].y()),
                vec_2f(t[7].x(), t[5].y()),
                t[10].minus(2 * TS, 0),
                t[11].minus(2 * TS, 0)
        };
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, h[0], h[1]);
        addWall(parent, h[1], t[2]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[4], t[5]);
        addWall(parent, t[5], t[6]);
        addWall(parent, h[2], t[7]);
        addWall(parent, t[6], t[8]);
        addWall(parent, t[8], t[9]);
        addWall(parent, t[9], t[10]);
        addWall(parent, t[10], h[3]);
        addWall(parent, h[3], h[4]);
        addWall(parent, t[11], t[12]);
        // fill holes
        addWallAtCenter(parent, h[1].midpoint(t[3]), 3*TS, 3*TS);
        addWallAtCenter(parent, h[3].midpoint(t[9]), 3*TS, 3*TS);
    }

    private void render_Strange7_TopLeft(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 15, 18, 22);
        Vector2f[] h = {
            vec_2f(t[0].x(), t[1].y()),
            vec_2f(t[2].x(), t[1].y()),
            vec_2f(t[0].x(), t[2].y()),
        };
        addTowers(parent, t);
        addWall(parent, t[0], t[4]);
        addWall(parent, t[1], h[0]);
        addWall(parent, t[2], h[2]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[0], t[5]);
        addWallAtCenter(parent, h[1].midpoint(h[2]), 3*TS, 2*TS);
    }

    private void render_Strange7_TopRight(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 5, 7, 14, 18, 23);
        Vector2f[] h = {
                vec_2f(t[1].x(), t[4].y()),
                vec_2f(t[3].x(), t[4].y()),
                vec_2f(t[1].x(), t[3].y()),
        };
        addTowers(parent, t);
        addWall(parent, t[0], t[5]);
        addWall(parent, t[4], h[0]);
        addWall(parent, t[3], h[2]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[1], t[5]);
        addWallAtCenter(parent, h[1].midpoint(h[2]), 3*TS, 2*TS);
    }

    private void render_RectangleWithTwoArmsAtTopRightCorner(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 6, 8, 12);
        addTowers(parent, t);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[1], t[4]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[3], t[0]);
        addWallAtCenter(parent, t[1].midpoint(t[3]), t[2].manhattanDist(t[3]), t[1].manhattanDist(t[2]));
    }

    private void render_RectangleWithTwoArmsAtTopLeftCorner(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 11, 13);
        addTowers(parent, t);
        addWall(parent, t[0], t[2]);
        addWall(parent, t[1], t[4]);
        addWall(parent, t[4], t[3]);
        addWall(parent, t[3], t[2]);
        addWallAtCenter(parent, t[2].midpoint(t[4]), t[2].manhattanDist(t[3]), t[3].manhattanDist(t[4]));
    }

    private void render_RectangleWithArmAtTopRightCorner(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 8);
        Vector2f h = vec_2f(t[2].x(), t[0].y());
        addTowers(parent, t);
        addWall(parent, t[0], t[3]);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], h);
        addWallAtCenter(parent, t[0].midpoint(t[2]), t[1].manhattanDist(t[2]), t[0].manhattanDist(t[1]));
    }

    private void render_RectangleWithArmAtTopLeftCorner(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 5, 7, 9);
        Vector2f h = vec_2f(t[1].x(), t[0].y());
        addTowers(parent, t);
        addWall(parent, t[0], t[3]);
        addWall(parent, h, t[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], t[3]);
        addWallAtCenter(parent, t[1].midpoint(t[3]), t[1].manhattanDist(t[2]), t[2].manhattanDist(t[3]));
    }

    private void render_Strange9_LargeObstacleLeft(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 5, 11, 18, 21, 28, 35);
        Vector2f[] h = {
                vec_2f(t[0].x(), t[1].y()),
                vec_2f(t[0].x(), t[2].y()),
                vec_2f(t[0].x(), t[5].y()),
                vec_2f(t[0].x(), t[3].y()),
        };
        addTowers(parent, t);
        addWall(parent, t[0], t[6]);
        addWall(parent, t[1], h[0]);
        addWall(parent, t[2], h[1]);
        addWall(parent, h[2], t[5]);
        addWall(parent, t[0], t[5]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[0], h[3]);
    }

    private void render_Strange9_LargeObstacleRight(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 7, 14, 17, 24, 31, 36);
        Vector2f[] h = {
                vec_2f(t[6].x(), t[5].y()),
                vec_2f(t[6].x(), t[4].y()),
                vec_2f(t[6].x(), t[1].y()),
                vec_2f(t[6].x(), t[2].y()),
        };
        addTowers(parent, t);
        addWall(parent, t[0], t[6]);
        addWall(parent, h[0], t[5]);
        addWall(parent, h[1], t[4]);
        addWall(parent, t[1], h[2]);
        addWall(parent, t[2], t[3]);
        addWall(parent, t[6], h[3]);
    }

    private void render_Strange10_SpaceShipTopCenter(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 6, 10, 15, 17, 21);
        Vector2f[] h = {
                vec_2f(t[0].x(), t[1].y()),
                vec_2f(t[6].x(), t[1].y()),
                vec_2f(t[3].x(), t[2].y())
        };
        addTowers(parent, t);
        addWall(parent, t[0], t[6]);
        addWall(parent, t[0], h[0]);
        addWall(parent, t[6], h[1]);
        addWall(parent, t[1], t[5]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[3], h[2]);
        addWall(parent, t[4], t[5]);
        addWallAtCenter(parent, t[0].midpoint(h[1]), t[0].manhattanDist(t[6]), t[0].manhattanDist(h[0]));
        addWallAtCenter(parent, t[1].midpoint(t[4]), t[1].manhattanDist(t[5]), t[1].manhattanDist(t[2]) + TS);
    }

    private void render_Strange10_IslandLeftOfSpaceship(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 6, 9, 12);
        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[0], t[5]);
        addWall(parent, t[5], t[2]);
        addWall(parent, t[2], t[4]);
        addWall(parent, t[4], t[3]);
        addWall(parent, t[1], t[1].plus(TS, 0));
    }

    private void render_Strange10_IslandRightOfSpaceship(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 3, 5, 8, 10, 12);
        addTowers(parent, t);
        addWall(parent, t[0], t[3]);
        addWall(parent, t[0], t[5]);
        addWall(parent, t[5], t[4]);
        addWall(parent, t[1], t[3]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[4], t[4].minus(TS, 0));
    }

    private void render_Strange10_RectangleWithTwoLegsAtLowerLeftEdge(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 13, 15);
        addTowers(parent, t);
        addWall(parent, t[0], t[2]);
        addWall(parent, t[1], t[3]);
        addWall(parent, t[3], t[4]);
        addWall(parent, t[0], t[4]);
    }

    private void render_Strange10_RectangleWithTwoLegsAtLowerRightEdge(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 5, 10, 15);
        addTowers(parent, t);
        addWall(parent, t[0], t[4]);
        addWall(parent, t[0], t[1]);
        addWall(parent, t[1], t[3]);
        addWall(parent, t[2], t[4]);
    }


}
