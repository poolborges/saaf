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
import de.rub.syssec.saaf.db.dao.interfaces.NuHResultDAO;
import de.rub.syssec.saaf.model.analysis.HResultInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MySQLHResultDAO implements NuHResultDAO {

    private Connection connection;
    private static final String DB_COLUMN_ANALYSIS = "id_analyses";
    private static final String DB_COLUMN_PATTERN = "id_heuristic_pattern";
    private static final String DB_COLUMN_CLASS = "id_class";
    private static final String DB_COLUMN_METHOD = "id_method";
    private static final String DB_COLUMN_LINENR = "in_line";
    private static final String DB_COLUMN_LINETXT = "line";
    private static final String DB_COLUMN_ID = "id";
    private static final String DB_COLUMN_INADFRAMEWORK = "in_ad_framework";

    private static final String DB_QUERY_CREATE = "INSERT INTO heuristic_results("
            + DB_COLUMN_ANALYSIS + ","
            + DB_COLUMN_PATTERN + ","
            + DB_COLUMN_CLASS + ","
            + DB_COLUMN_METHOD + ","
            + DB_COLUMN_LINENR + ","
            + DB_COLUMN_LINETXT + ","
            + DB_COLUMN_INADFRAMEWORK + ")VALUES(?,?,?,?,?,?,?)";

//		private static final String DB_QUERY_READ = "SELECT * FROM heuristic_results WHERE "+DB_COLUMN_ID+"=?";
    private static final String DB_QUERY_UPDATE = "UPDATE heuristic_results SET "
            + DB_COLUMN_ANALYSIS + "=?,"
            + DB_COLUMN_PATTERN + "=?,"
            + DB_COLUMN_CLASS + "=?,"
            + DB_COLUMN_METHOD + "=?,"
            + DB_COLUMN_LINENR + "=?,"
            + DB_COLUMN_LINETXT + "=?,"
            + DB_COLUMN_INADFRAMEWORK + "=? WHERE "
            + DB_COLUMN_ID + "=?";

    private static final String DB_QUERY_DELETE = "DELETE FROM heuristic_results WHERE " + DB_COLUMN_ID + "=?";
    private static final String DB_QUERY_DELETE_ALL = "DELETE FROM heuristic_results";

    /**
     * Creates a MySQLHResultDAO that uses the supplied Connection.
     *
     * @param connection
     */
    public MySQLHResultDAO(Connection connection) {
        super();
        this.connection = connection;
    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
     */
    @Override
    public int create(HResultInterface entity) throws DAOException, DuplicateEntityException {
        PreparedStatement statement;
        int index = 0;
        int id;
        try {
            statement = connection.prepareStatement(DB_QUERY_CREATE, Statement.RETURN_GENERATED_KEYS);
            if (entity.getAnalysis() != null) {
                statement.setInt(++index, entity.getAnalysis().getId());
            } else {
                statement.setNull(++index, Types.INTEGER);
            }

            if (entity.getPattern() != null) {
                statement.setInt(++index, entity.getPattern().getId());
            } else {
                statement.setNull(++index, Types.INTEGER);
            }

            if (entity.getFile() != null) {
                statement.setInt(++index, entity.getFile().getId());
            } else {
                statement.setNull(++index, Types.INTEGER);
            }

            if (entity.getCodeline() != null && entity.getCodeline().getMethod() != null) {
                statement.setInt(++index, entity.getCodeline().getMethod().getId());
            } else {
                statement.setNull(++index, Types.INTEGER);
            }

            if (entity.getCodeline() != null) {
                statement.setInt(++index, entity.getCodeline().getLineNr());
            } else {
                statement.setNull(++index, Types.INTEGER);
            }

            if (entity.getCodeline() != null && entity.getCodeline().getLine() != null) {
                statement.setString(++index, new String(entity.getCodeline().getLine()));
            } else {
                statement.setNull(++index, Types.VARCHAR);
            }

            if (entity.getCodeline() != null && entity.getCodeline().getSmaliClass() != null) {
                statement.setBoolean(++index, entity.getCodeline().getSmaliClass().isInAdFrameworkPackage());
            } else {
                statement.setNull(++index, Types.VARCHAR);
            }

            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                throw new DAOException(
                        "Autogenerated keys could not be retrieved!");
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
    public HResultInterface read(int id) throws DAOException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
     */
    @Override
    public boolean update(HResultInterface entity) throws DAOException, NoSuchEntityException {
        boolean success = false;
        int recordsUpdated;
        int index = 0;
        PreparedStatement updateStmt;

        try {
            updateStmt = connection.prepareStatement(DB_QUERY_UPDATE);
            if (entity.getAnalysis() != null) {
                updateStmt.setInt(++index, entity.getAnalysis().getId());
            } else {
                updateStmt.setNull(++index, Types.INTEGER);
            }

            if (entity.getPattern() != null) {
                updateStmt.setInt(++index, entity.getPattern().getId());
            } else {
                updateStmt.setNull(++index, Types.INTEGER);
            }

            if (entity.getFile() != null) {
                updateStmt.setInt(++index, entity.getFile().getId());
            }

            if (entity.getCodeline() != null && entity.getCodeline().getMethod() != null) {
                updateStmt.setInt(++index, entity.getCodeline().getMethod().getId());
            } else {
                updateStmt.setNull(++index, Types.INTEGER);
            }

            if (entity.getCodeline() != null) {
                updateStmt.setInt(++index, entity.getCodeline().getLineNr());
            } else {
                updateStmt.setNull(++index, Types.INTEGER);
            }

            if (entity.getCodeline() != null && entity.getCodeline().getLine() != null) {
                updateStmt.setString(++index, new String(entity.getCodeline().getLine()));
            } else {
                updateStmt.setNull(++index, Types.VARCHAR);
            }

            if (entity.getCodeline() != null && entity.getCodeline().getSmaliClass() != null) {
                updateStmt.setBoolean(++index, entity.getCodeline().getSmaliClass().isInAdFrameworkPackage());
            } else {
                updateStmt.setNull(++index, Types.VARCHAR);
            }

            updateStmt.setInt(++index, entity.getId());

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
    public boolean delete(HResultInterface entity) throws DAOException, NoSuchEntityException {
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
    public List<HResultInterface> readAll() {
        throw new UnsupportedOperationException();
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
    public int findId(HResultInterface entity) throws DAOException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
