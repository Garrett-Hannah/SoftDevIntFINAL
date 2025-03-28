package ModelTest;

import chGameUtil.Game;
import chModel.Checkers.Board;
import chModel.Checkers.Position;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PositionTest {

    @BeforeAll
    static void start()
    {
        Game.initialize(8);
    }

    @Test
    void initGoodPosition()
    {
        Position goodPosition = new Position(4, 4);

        Assertions.assertTrue(goodPosition != null, " Error with position declaration!");
    }



    @AfterAll
    static void breakDown()
    {
        Game.getInstance().closeBoard();
    }
}
