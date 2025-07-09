package sqlobjs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import credentials.Credentials;

public class ADAO {

    private static final String JDBC_URL = "jdbc:postgresql://"
            + Credentials.get("POSTGRES_IP")
            + ":5432/"
            + Credentials.get(
                    "POSTGRES_DB");
    private static final String JDBC_USER = Credentials.get("POSTGRES_USER");
    private static final String JDBC_PASSWORD = Credentials.get("POSTGRES_PASSWORD");

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}
