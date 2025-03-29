package ModelTest;

import chGameUtil.BoardHelperSingleton;
import MVC.chModel.Checkers.Position;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void testLinPositionFromInt()
    {
        Position pA1 = Position.positionFromInt(0);
        Assertions.assertEquals(new Position(1, 1), pA1, "Position was not expected.");

        Position pH8 = Position.positionFromInt(63);
        Assertions.assertEquals(new Position(8, 8), pH8, "Position was not expected.");
    }

    @Test
    void testInvalidPositionFromInt()
    {
        // Expecting IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            Position.positionFromInt(65);
        }, "Piece should throw an exception for illegal moves.");

        assertThrows(IllegalArgumentException.class, () -> {
            Position.positionFromInt(-1);
        }, "Piece should throw an exception for illegal moves.");
    }


    @AfterAll
    static void breakDown()
    {
        BoardHelperSingleton.getInstance().closeBoard();
    }
}
