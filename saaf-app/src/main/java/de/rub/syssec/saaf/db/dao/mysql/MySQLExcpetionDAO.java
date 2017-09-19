/* SAAF: A static analyzer for APK files.
 * Copyright (C) 2013  syssec.rub.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rub.syssec.saaf.db.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuExceptionDAO;
import de.rub.syssec.saaf.model.SAAFException;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MySQLExcpetionDAO implements NuExceptionDAO {

    private static final String DB_COLUMN_ANALYSIS = "id_analyses";
    private static final String DB_COLUMN_MESSAGE = "error_message";
    private static final String DB_COLUMN_ID = "id";
    private static final String DB_QUERY_CREATE = "INSERT INTO error_messages(" + DB_COLUMN_ANALYSIS + ","
            + DB_COLUMN_MESSAGE + ")VALUES(?,?)";
    private static final String DB_QUERY_UPDATE = "UPDATE error_messages SET " + DB_COLUMN_ANALYSIS + "=?,"
            + DB_COLUMN_MESSAGE + "=? WHERE "
            + DB_COLUMN_ID + "=?";
    private static final String DB_QUERY_DELETE = "DELETE FROM error_messages WHERE " + DB_COLUMN_ID + "=?";
    private static final String DB_QUERY_DELETE_ALL = "DELETE FROM error_messages";

    private Connection connection;

    /**
     * Creates a MySQLExcpetionDAO that uses the supplied Connection.
     *
     * @param connection
     */
    public MySQLExcpetionDAO(Connection connection) {
        super();
        this.connection = connection;
    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
     */
    @Override
    public int create(SAAFException entity) throws DAOException, DuplicateEntityException {
        PreparedStatement statement;
        int id;
        int index = 0;
        try {
            statement = connection.prepareStatement(DB_QUERY_CREATE, Statement.RETURN_GENERATED_KEYS);
            if (entity.getAnalysis() != null) {
                statement.setInt(++index, entity.getAnalysis().getId());
            } else {
                statement.setNull(++index, Types.INTEGER);
            }

            if (entity.getMessage() != null) {
                statement.setString(++index, entity.getMessage());
            } else {
                statement.setNull(++index, Types.VARCHAR);
            }

            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                throw new DAOException(
                        "Autogenerated keys coudl not be retrieved!");
            }
        } catch (SQLException e) {
            //use the SQL Error Code to throw specific exception for duplicate entries
            if (e.getSQLState().equalsIgnoreCase(SQL_ERROR_DUPLICATE) && e.getMessage().toLowerCase().contains("duplicate")) {
                throw new DuplicateEntityException("An entity with the same key attributes already exists", e);
            } else {
                throw new DAOException(e);
            }
        }
        //otherwise continue persisting the primitive types and 1-1 associations
        return id;
    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#read(int)
     */
    @Override
    public SAAFException read(int id) throws DAOException {
        throw new UnsupportedOperationException("Reading exceptions is not implemented yet");
    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
     */
    @Override
    public boolean update(SAAFException entity) throws DAOException, NoSuchEntityException {
        boolean success = false;
        int recordsUpdated;
        PreparedStatement updateStmt;

        try {
            updateStmt = connection.prepareStatement(DB_QUERY_UPDATE);
            if (entity.getAnalysis() != null) {
                updateStmt.setInt(1, entity.getAnalysis().getId());
            } else {
                updateStmt.setNull(1, Types.INTEGER);
            }

            if (entity.getMessage() != null) {
                updateStmt.setString(2, entity.getMessage());
            } else {
                updateStmt.setNull(2, Types.VARCHAR);
            }
            updateStmt.setInt(3, entity.getId());
            recordsUpdated = updateStmt.executeUpdate();
            // this should affect at most one record
            if (recordsUpdated == 0) {
                throw new NoSuchEntityException();
            } else if (recordsUpdated == 1) {
                success = true;
            } else {
                // the update affected multiple records this should not happen!
                throw new DAOException("Update of one entity affected multiple records. This should not happen!");
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return success;
    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#delete(java.lang.Object)
     */
    @Override
    public boolean delete(SAAFException entity) throws DAOException, NoSuchEntityException {
        boolean success = false;
        int recordsAffected;
        try {
            PreparedStatement deleteStmt = connection
                    .prepareStatement(DB_QUERY_DELETE);
            deleteStmt.setInt(1, entity.getId());
            recordsAffected = deleteStmt.executeUpdate();
            // this should affect at most one record
            if (recordsAffected == 0) {
                throw new NoSuchEntityException();
            } else if (recordsAffected == 1) {
                success = true;
            } else if (recordsAffected > 1) {
                throw new DAOException("Delete of one entity affected multiple records. This should not happen!");
            }

        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return success;
    }

    @Override
    public List<SAAFException> readAll() {
        throw new UnsupportedOperationException("Reading exceptions is not implemented yet");
    }

    @Override
    public int deleteAll() throws DAOException {
        int recordsAffected;
        try {
            PreparedStatement deleteStmt = connection.prepareStatement(DB_QUERY_DELETE_ALL);
            recordsAffected = deleteStmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return recordsAffected;
    }

    @Override
    public int findId(SAAFException candidate) throws DAOException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
