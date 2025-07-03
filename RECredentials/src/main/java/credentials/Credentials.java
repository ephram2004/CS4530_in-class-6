package credentials;

import io.github.cdimascio.dotenv.Dotenv;

public class Credentials {
    public static Dotenv dotenv;

    static {
        dotenv = Dotenv.configure()
                .directory("REDatabase")
                .filename(".env")
                .load();
    }

    public static String get(String key) {
        return dotenv.get(key);
    }
}