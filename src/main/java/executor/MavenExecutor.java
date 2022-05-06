package executor;

import java.io.IOException;

public class MavenExecutor {
    public static void execute(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
