package ModelTest;

import chkMVC.chModel.Checkers.BoardModel;
import chkMVC.chModel.Checkers.Position;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PositionTest {

    private static BoardModel gameBoardModel;

    @BeforeAll
    static void start()
    {
        gameBoardModel = new BoardModel(8);
    }

    @Test
    void initGoodPosition()
    {
        Position goodPosition = gameBoardModel.createPosition(4, 4);

        Assertions.assertTrue(goodPosition != null, " Error with position declaration!");
    }

    @Test
    void testLinPositionFromInt()
    {
        Position pA1 = gameBoardModel.positionFromInt(0);
        Assertions.assertEquals(gameBoardModel.createPosition(1, 1), pA1, "Position was not expected.");

        Position pH8 = gameBoardModel.positionFromInt(63);
        Assertions.assertEquals(gameBoardModel.createPosition(8, 8), pH8, "Position was not expected.");
    }

    @Test
    void testInvalidPositionFromInt()
    {
        // Expecting IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            gameBoardModel.positionFromInt(65);
        }, "Piece should throw an exception for illegal moves.");

        assertThrows(IllegalArgumentException.class, () -> {
            gameBoardModel.positionFromInt(-1);
        }, "Piece should throw an exception for illegal moves.");
    }

    @AfterAll
    static void breakDown()
    {

    }
}
