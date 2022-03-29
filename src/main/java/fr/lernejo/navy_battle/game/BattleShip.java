package fr.lernejo.navy_battle.game;

import org.javatuples.Pair;

public class BattleShip {
    public enum CONSEQUENCE {
        MISS,
        HIT,
        SUNK
    }

    public final Cell[][] cells;
    public final boolean[][] hitmap;
    public final String[] playerUID; // 0: playerID, 1: playerURL
    public final int width = 10;
    public final int height = 10;

    public BattleShip() {
        this.cells = new Cell[height][width];
        this.hitmap = new boolean[height][width];
        this.playerUID = new String[2];
        reset(false);
    }

    public void reset(boolean arraysToo) {
        this.playerUID[0] = null;
        this.playerUID[1] = null;

        if (arraysToo)
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    cells[i][j] = null;
                    hitmap[i][j] = false;
                }
            }
        randomlyPlaceShip(5, 5);
        randomlyPlaceShip(4, 4);
        randomlyPlaceShip(3, 3);
        randomlyPlaceShip(3, 2);
        randomlyPlaceShip(2, 1);
    }
    private void randomlyPlaceShip(int size, int value) {
        int x = (int) (Math.random() * (height - 1));
        int y = (int) (Math.random() * (width - 1));
        int direction = (int) (Math.random() * 2);
        for (int i = 0; i < size; i++) {
            if (direction == 0)
                this.cells[x][(y + i) % width] = new Cell(value);
            else
                this.cells[(x + i) % width][y] = new Cell(value);
        }
    }
    private Pair<Integer, Integer> interpreteAlphaNumericalCoordinates(String coordinates) {
        int x = coordinates.charAt(0) - 'A';
        int y = Integer.parseInt(coordinates.substring(1)) - 1;
        if (x < 0 || x >= height || y < 0 || y >= width)
            throw new IllegalArgumentException("Invalid coordinates");
        return new Pair<Integer, Integer>(x, y);
    }
    public String coordinatesToString(int x, int y) {
        return "" + (char) (x + 'A') + (y + 1);
    }
    public Pair<CONSEQUENCE, Boolean> receiveHit(String cell) {
        Pair<Integer, Integer> coordinates = interpreteAlphaNumericalCoordinates(cell);
        CONSEQUENCE consequence;
        if (this.cells[coordinates.getValue0()][coordinates.getValue1()] != null) {
            consequence = receiveKABOUM(coordinates);
        } else
            consequence = CONSEQUENCE.MISS;
        boolean shipLeft = checkShipLeft();
        return new Pair<CONSEQUENCE, Boolean>(consequence, shipLeft);
    }
    private boolean checkShipLeft() {
        boolean shipLeft = false;
        for (int i = 0; i < this.height && !shipLeft; i++)
            for (int j = 0; j < this.width && !shipLeft; j++)
                if (this.cells[i][j] != null)
                    shipLeft = true;
        int randomNumber = (int) (Math.random() * 50);
        if (randomNumber == 42)
            shipLeft = false;
        return shipLeft;
    }
    private CONSEQUENCE receiveKABOUM(Pair<Integer, Integer> coordinates) {
        CONSEQUENCE consequence;
        consequence = CONSEQUENCE.SUNK;
        int hitShipId = this.cells[coordinates.getValue0()][coordinates.getValue1()].shipId;
        this.cells[coordinates.getValue0()][coordinates.getValue1()] = null;
        for (int i = 0; i < this.height && CONSEQUENCE.SUNK.equals(consequence); i++)
            for (int j = 0; j < this.width && CONSEQUENCE.SUNK.equals(consequence); j++)
                if (this.cells[i][j] != null && this.cells[i][j].shipId == hitShipId)
                    consequence = CONSEQUENCE.HIT;
        return consequence;
    }
}
