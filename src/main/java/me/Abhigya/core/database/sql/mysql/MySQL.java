package me.Abhigya.core.database.sql.mysql;

import me.Abhigya.core.database.DatabaseType;
import me.Abhigya.core.database.sql.SQLDatabase;
import me.Abhigya.core.util.StringUtils;
import org.apache.commons.lang.Validate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/** Class for interacting with a MySQL database. */
public class MySQL extends SQLDatabase {

    /** Connection URL format. */
    private static final String URL_FORMAT =
            "jdbc:mysql://"
                    + "%s" // host
                    + ":"
                    + "%d" // port
                    + "/"
                    + "%s" // database
                    + "?autoReconnect="
                    + "%s" // auto reconnect
                    + "&"
                    + "useSSL="
                    + "%s" // use ssl
            ;

    /** The JDBC driver class. */
    private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean reconnect;
    private final boolean ssl;

    private Connection connection;
    private int lost_connections;

    /**
     * Constructs the MySQL database.
     *
     * <p>
     *
     * @param host Host name
     * @param port Port number
     * @param database Database name
     * @param username User name
     * @param password User password
     * @param reconnect <strong>{@code true}</strong> to auto reconnect
     * @param ssl <strong>{@code true}</strong> to use SSL
     */
    public MySQL(
            String host,
            int port,
            String database,
            String username,
            String password,
            boolean reconnect,
            boolean ssl) {
        super(DatabaseType.MYSQL);

        Validate.isTrue(!StringUtils.isBlank(host), "The host cannot be null or empty!");
        Validate.isTrue(!StringUtils.isBlank(database), "The database cannot be null or empty!");
        Validate.notNull(username, "The username cannot be null!");
        Validate.notNull(password, "The password cannot be null!");

        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.reconnect = reconnect;
        this.ssl = ssl;
    }

    /**
     * Constructs the MySQL database.
     *
     * <p>
     *
     * @param host Host name
     * @param port Port number
     * @param database Database name
     * @param username User name
     * @param password User password
     * @param reconnect <strong>{@code true}</strong> to auto reconnect
     */
    public MySQL(
            String host,
            int port,
            String database,
            String username,
            String password,
            boolean reconnect) {
        this(host, port, database, username, password, reconnect, true);
    }

    /**
     * Gets whether connected to MySQL.
     *
     * <p>
     *
     * @return true if connected.
     */
    @Override
    public boolean isConnected() {
        try {
            return this.connection != null && !this.connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     *
     *
     * <h1>Returns:</h1>
     *
     * <ul>
     *   <li>The current connection if connected to MySQL:
     *   <li>The new connection if and only if:
     *       <ul>
     *         <li>It wasn't connected.
     *         <li>The auto-reconnection is enabled.
     *         <li>The attempt to get connection was successfully.
     *       </ul>
     *   <li><strong>{@code null}</strong> if:
     *       <ul>
     *         <li>It wasn't connected and the auto-reconnection is disabled.
     *         <li>The auto-reconnection is enabled but the attempt to get connection was
     *             unsuccessfully.
     *       </ul>
     * </ul>
     *
     * <p>
     *
     * @return Connection or null if not connected
     * @throws SQLTimeoutException when the driver has determined that the timeout value has been
     *     exceeded and has at least tried to cancel the current database connection attempt.
     * @throws IllegalStateException if the JDBC drivers is unavailable
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public Connection getConnection()
            throws SQLTimeoutException, IllegalStateException, SQLException {
        if (!isConnected() && reconnect) {
            this.lost_connections++;
            this.connect();
        }
        return this.isConnected() ? this.connection : null;
    }

    /**
     * The times the connection was lost.
     *
     * <p>
     *
     * @return Times the connection was lost, or <strong>{@code -1}</strong> if the
     *     auto-reconnection is disabled.
     */
    @Override
    public int getLostConnections() {
        return reconnect ? lost_connections : -1;
    }

    /**
     * Starts the connection with MySQL.
     *
     * <p>
     *
     * @throws IllegalStateException if the JDBC drivers is unavailable.
     * @throws SQLException if a database access error occurs.
     * @throws SQLTimeoutException when the driver has determined that the timeout has been exceeded
     *     and has at least tried to cancel the current database connection attempt.
     */
    @Override
    public synchronized void connect()
            throws IllegalStateException, SQLException, SQLTimeoutException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "Could not connect to MySQL! The JDBC driver is unavailable!");
        }

        this.connection =
                DriverManager.getConnection(
                        String.format(
                                URL_FORMAT,
                                host,
                                port,
                                database,
                                String.valueOf(reconnect),
                                String.valueOf(ssl)),
                        username,
                        password);
    }

    /**
     * Closes the connection with MySQL.
     *
     * <p>
     *
     * @throws IllegalStateException if currently not connected, the connection should be checked
     *     before calling this: {@link #isConnected()}.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public void disconnect() throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected!");
        }

        this.connection.close();
        this.connection = null;
    }
}
