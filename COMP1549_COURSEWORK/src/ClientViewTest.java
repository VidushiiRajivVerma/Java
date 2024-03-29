
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class ClientViewTest {

    @Test
    public void testGenerateUniqueID_IsNumber() {
        try {
            ClientView clientView = new ClientView();
            Method method = ClientView.class.getDeclaredMethod("generateUniqueID");
            method.setAccessible(true); // Make the method accessible
            String uniqueID = (String) method.invoke(clientView);

            // Now we can test the output
            assertNotNull(uniqueID);
            assertEquals(6, uniqueID.length());
            Integer.parseInt(uniqueID); // This line throws if not a number
        } catch (NumberFormatException e) {
            fail("Generated Unique ID is not a number");
        } catch (Exception e) {
            fail("Exception thrown during test execution: " + e.getMessage());
        }
    }
}
