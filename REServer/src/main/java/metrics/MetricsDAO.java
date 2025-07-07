package metrics;

import credentials.Credentials;

public class MetricsDAO {
    private static final String JDBC_URL = "jdbc:postgresql://"
            + Credentials.get("POSTGRES_IP")
            + ":5432/"
            + Credentials.get(
                    "POSTGRES_DB");
    private static final String JDBC_USER = Credentials.get("POSTGRES_USER");
    private static final String JDBC_PASSWORD = Credentials.get("POSTGRES_PASSWORD");
}
