package fr.lernejo.navy_battle.game;

public class PrintBoard {
    public void printBoard(BattleShip battleShip) {
        System.out.println(battleShip.playerUID[0] + "\t" + battleShip.playerUID[1]);
        System.out.println("My map");
        printMyMap(battleShip);

        System.out.println("Ennemy hitmap");
        printHitmap(battleShip);

    }

    private void printHitmap(BattleShip battleShip) {
        for (int i = 0; i < battleShip.height; i++) {
            for (int j = 0; j < battleShip.width; j++) {
                if (battleShip.hitmap[i][j])
                    System.out.print("X ");
                else
                    System.out.print("- ");

            }
            System.out.println();
        }
    }

    private void printMyMap(BattleShip battleShip) {
        for (int i = 0; i < battleShip.height; i++) {
            for (int j = 0; j < battleShip.width; j++) {
                if (battleShip.cells[i][j] != null) {
                    System.out.print(battleShip.cells[i][j].shipId + " ");
                } else {
                    System.out.print("- ");
                }
            }
            System.out.println();
        }
    }
}
