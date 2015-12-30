/*
Author: Mahen Samaranayake
*/
package com.mypool.ctrl;

import com.mypool.init.MyPoolInit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

public class MyPool {

    private final Vector pool;
    private static MyPool myPool;

    private MyPool() {
        pool = new Vector();
        initialize();
    }

    public static synchronized MyPool getPool() {
        if (myPool == null) {
            myPool = new MyPool();
        }
        return myPool;
    }

    private void initialize() {
        while (!isFull()) {
            pool.addElement(createNewConnection());
        }
    }

    private synchronized boolean isFull() {
        return pool.size() >= MyPoolInit.MAX_POOL_SIZE;
    }

    private Connection createNewConnection() {
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(MyPoolInit.URL, MyPoolInit.USERNAME, MyPoolInit.PASSWORD);
        } catch (SQLException ex) {
            System.err.println(ex);
            return null;
        } catch (ClassNotFoundException ex) {
            System.err.println(ex);
            return null;
        }
        return connection;
    }

    public Connection getConnection() {
        Connection connection = null;
        boolean b = false;
        synchronized (this) {
            if (pool.size() > 0) {
                connection = (Connection) pool.firstElement();
                try {
                    if (connection.isClosed()) {
                        connection = createNewConnection();
                    }
                } catch (SQLException ex) {
                }
                pool.removeElementAt(0);
            } else {
                b = true;
            }
        }
        if (b) {
            try {
                Thread.sleep(MyPoolInit.WAIT_TIME);
            } catch (Exception e) {
            }
            connection = getConnection();
        }
        return connection;
    }

    public synchronized void close(Connection connection) {
        pool.addElement(connection);
    }
}
