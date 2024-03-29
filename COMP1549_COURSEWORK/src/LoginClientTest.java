
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginClientTest {

    @BeforeEach
    public void setup() {

    }

    
    @Test
    public void testGUIInitialization() throws NoSuchFieldException, IllegalAccessException {
        // Accessing private static fields using reflection
        try {
            Field frameField = LoginClient.class.getDeclaredField("frame");
            frameField.setAccessible(true);
            Object frameObject = frameField.get(null); // Assuming frame is static
            assertNotNull(frameObject, "Frame is null");
        } catch (NullPointerException e) {
            System.out.println("NullPointerException occurred while accessing frame field.");
            e.printStackTrace();
        }

        try {
            Field clientUserNameField = LoginClient.class.getDeclaredField("clientUserName");
            clientUserNameField.setAccessible(true);
            Object clientUserNameObject = clientUserNameField.get(null); // Assuming clientUserName is static
            assertNotNull(clientUserNameObject, "Client user name is null");
        } catch (NullPointerException e) {
            System.out.println("NullPointerException occurred while accessing clientUserName field.");
            e.printStackTrace();
        }
    }

}

