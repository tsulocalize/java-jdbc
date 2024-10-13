package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService service;

    public TxUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.service = new AppUserService(userDao, userHistoryDao);
    }

    @Override
    public User findById(long id) {
        Connection connection = null;
        User user = null;
        try {
            connection = DataSourceConfig.getInstance().getConnection();
            connection.setAutoCommit(false);
            user = service.findById(id);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            rollback(e, connection);
        } finally {
            closeConnection(connection);
        }
        return user;
    }

    @Override
    public void insert(User user) {
        Connection connection = null;
        try {
            connection = DataSourceConfig.getInstance().getConnection();
            connection.setAutoCommit(false);
            service.insert(user);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            rollback(e, connection);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = null;
        try {
            connection = DataSourceConfig.getInstance().getConnection();
            connection.setAutoCommit(false);
            service.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            rollback(e, connection);
        } finally {
            closeConnection(connection);
        }
    }

    private static void rollback(Exception e, Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException rollbackE) {
            throw new DataAccessException(rollbackE);
        }
        throw new DataAccessException(e);
    }

    private static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException closeE) {
            throw new DataAccessException(closeE);
        }
    }
}
