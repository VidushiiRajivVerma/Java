
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

public class ServerViewTest {

    @Test
    void testServerViewInit() throws NoSuchFieldException, IllegalAccessException {
        ServerView serverView = new ServerView();

        Field frameField = ServerView.class.getDeclaredField("frame");
        frameField.setAccessible(true);
        assertNotNull(frameField.get(serverView), "Frame should be initialized and not null.");

        Field serverMessageBoardField = ServerView.class.getDeclaredField("serverMessageBoard");
        serverMessageBoardField.setAccessible(true);
        assertNotNull(serverMessageBoardField.get(serverView), "Server message board should be initialized and not null.");
    }
}
