package ModelTest;

import chGameUtil.BoardHelperSingleton;
import chModel.Checkers.Position;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PositionTest {

    @BeforeAll
    static void start()
    {
        BoardHelperSingleton.initialize(8);
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
        BoardHelperSingleton.getInstance().closeBoard();
    }
}
