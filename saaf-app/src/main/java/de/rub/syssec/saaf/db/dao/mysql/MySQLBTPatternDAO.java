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
import java.util.ArrayList;
import java.util.List;

import de.rub.syssec.saaf.analysis.steps.slicing.BTPattern;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuBTPatternDAO;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MySQLBTPatternDAO implements NuBTPatternDAO {

    private static final String DB_COLUMN_ID = "id";
    private static final String DB_COLUMN_CLASS = "qualified_class";
    private static final String DB_COLUMN_METHOD = "method_name";
    private static final String DB_COLUMN_PARAMS = "parameter_types";
    private static final String DB_COLUMN_POI = "param_of_interest";
    private static final String DB_COLUMN_DESC = "description";
    private static final String DB_COLUMN_ACTIVE = "active";

    private static final String QUERY_UPDATE = "UPDATE backtrack_pattern SET "
            + DB_COLUMN_CLASS + "=?, "
            + DB_COLUMN_METHOD + "=?, "
            + DB_COLUMN_PARAMS + "=?,"
            + DB_COLUMN_POI + "=?,"
            + DB_COLUMN_DESC + "=?,"
            + DB_COLUMN_ACTIVE + "=? WHERE "
            + DB_COLUMN_ID + "=?";
    private static final String QUERY_INSERT = "INSERT INTO backtrack_pattern("
            + DB_COLUMN_CLASS + ", "
            + DB_COLUMN_METHOD + ", "
            + DB_COLUMN_PARAMS + ", "
            + DB_COLUMN_POI + ", "
            + DB_COLUMN_DESC + ","
            + DB_COLUMN_ACTIVE
            + ") VALUES(?,?,?,?,?,?)";
    private static final String QUERY_READ = "SELECT * FROM backtrack_pattern WHERE "
            + DB_COLUMN_ID + "=?";
    private static final String QUERY_DELETE = "DELETE FROM backtrack_pattern WHERE "
            + DB_COLUMN_ID + "=?";
    private static final String QUERY_READ_ALL = "SELECT * FROM backtrack_pattern";
    private static final String DB_QUERY_DELETE_ALL = "DELETE FROM backtrack_pattern";
    private static final String DB_QUERY_FIND_EXISITING = "SELECT * FROM backtrack_pattern WHERE " + DB_COLUMN_CLASS + "=? AND "
            + DB_COLUMN_METHOD + "=? AND "
            + DB_COLUMN_PARAMS + "=? AND "
            + DB_COLUMN_POI + "=? AND "
            + DB_COLUMN_DESC + "=?";
    private Connection connection;

    /**
     * Creates a MySQLBTPatternDAO that uses the supplied Connection.
     *
     * @param connection
     */
    public MySQLBTPatternDAO(Connection connection) {
        super();
        this.connection = connection;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
     */
    @Override
    public int create(BTPatternInterface entity) throws DAOException, DuplicateEntityException {
        int id;
        int index = 0;
        try {
            PreparedStatement insert = connection.prepareStatement(
                    QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
            //test parameters for null and set accordingly
            if (entity.getQualifiedClassName() != null) {
                insert.setString(++index, entity.getQualifiedClassName());
            } else {
                insert.setNull(++index, Types.VARCHAR);
            }
            if (entity.getMethodName() != null) {
                insert.setString(++index, entity.getMethodName());
            } else {
                insert.setNull(++index, Types.VARCHAR);
            }
            if (entity.getArgumentsTypes() != null) {
                insert.setString(++index, new String(entity.getArgumentsTypes()));
            } else {
                insert.setNull(++index, Types.VARCHAR);
            }
            insert.setInt(++index, entity.getParameterOfInterest());

            if (entity.getDescription() != null) {
                insert.setString(++index, entity.getDescription());
            } else {
                insert.setNull(++index, Types.VARCHAR);
            }
            insert.setBoolean(++index, entity.isActive());

            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
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
        return id;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#read(int)
     */
    @Override
    public BTPatternInterface read(int id) throws DAOException {
        BTPatternInterface pattern = null;
        PreparedStatement selectStmt;
        try {
            selectStmt = connection.prepareStatement(QUERY_READ);
            selectStmt.setInt(1, id);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                pattern = new BTPattern(rs.getString(DB_COLUMN_CLASS),
                        rs.getString(DB_COLUMN_METHOD),
                        rs.getString(DB_COLUMN_PARAMS),
                        rs.getInt(DB_COLUMN_POI), rs.getString(DB_COLUMN_DESC));
                pattern.setActive(rs.getBoolean(DB_COLUMN_ACTIVE));
                pattern.setId(rs.getInt(DB_COLUMN_ID));
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return pattern;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
     */
    @Override
    public boolean update(BTPatternInterface entity) throws DAOException, NoSuchEntityException {
        boolean success = false;
        int index = 0;
        int recordsUpdated;
        PreparedStatement updateStmt;

        try {
            updateStmt = connection.prepareStatement(QUERY_UPDATE);
            updateStmt.setString(++index, entity.getQualifiedClassName());
            updateStmt.setString(++index, entity.getMethodName());
            updateStmt.setString(++index, new String(entity.getArgumentsTypes()));
            updateStmt.setInt(++index, entity.getParameterOfInterest());
            updateStmt.setString(++index, entity.getDescription());
            updateStmt.setBoolean(++index, entity.isActive());
            updateStmt.setInt(++index, entity.getId());
            recordsUpdated = updateStmt.executeUpdate();
            // this should affect at most one record
            if (recordsUpdated == 0) {
                throw new NoSuchEntityException();
            } else if (recordsUpdated == 1) {
                success = true;
            } else {
                // the update affected multiple records this should not happen!
                throw new DAOException("Update of one BTPattern affected multiple records. This should not happen!");
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return success;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#delete(java.lang.Object)
     */
    @Override
    public boolean delete(BTPatternInterface entity) throws DAOException, NoSuchEntityException {
        boolean success = false;
        int recordsAffected;
        try {
            PreparedStatement deleteStmt = connection
                    .prepareStatement(QUERY_DELETE);
            deleteStmt.setInt(1, entity.getId());
            recordsAffected = deleteStmt.executeUpdate();
            // this should affect at most one record
            if (recordsAffected == 0) {
                throw new NoSuchEntityException();
            } else if (recordsAffected == 1) {
                success = true;
            } else if (recordsAffected > 1) {
                throw new DAOException("Delete of one BTPattern affected multiple records. This should not happen!");
            }

        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return success;
    }

    @Override
    public List<BTPatternInterface> readAll() throws DAOException {
        ArrayList<BTPatternInterface> allPatterns = new ArrayList<BTPatternInterface>();
        PreparedStatement selectStmt;
        BTPatternInterface pattern;
        try {
            selectStmt = connection.prepareStatement(QUERY_READ_ALL);
            ResultSet rs = selectStmt.executeQuery();
            while (rs.next()) {
                pattern = new BTPattern(rs.getString(DB_COLUMN_CLASS),
                        rs.getString(DB_COLUMN_METHOD),
                        rs.getString(DB_COLUMN_PARAMS),
                        rs.getInt(DB_COLUMN_POI), rs.getString(DB_COLUMN_DESC));
                pattern.setActive(rs.getBoolean(DB_COLUMN_ACTIVE));
                pattern.setId(rs.getInt(DB_COLUMN_ID));
                allPatterns.add(pattern);
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return allPatterns;
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
    public int findId(BTPatternInterface candidate) throws DAOException {
        int id = 0;
        PreparedStatement selectStmt;
        try {
            selectStmt = connection.prepareStatement(DB_QUERY_FIND_EXISITING);
            if (candidate.getQualifiedClassName() != null) {
                selectStmt.setString(1, candidate.getQualifiedClassName());
            } else {
                selectStmt.setNull(1, Types.VARCHAR);
            }
            if (candidate.getMethodName() != null) {
                selectStmt.setString(2, candidate.getMethodName());
            } else {
                selectStmt.setNull(2, Types.VARCHAR);
            }
            if (candidate.getArgumentsTypes() != null) {
                selectStmt.setString(3, new String(candidate.getArgumentsTypes()));
            } else {
                selectStmt.setNull(3, Types.VARCHAR);
            }
            selectStmt.setInt(4, candidate.getParameterOfInterest());
            if (candidate.getDescription() != null) {
                selectStmt.setString(5, candidate.getDescription());
            } else {
                selectStmt.setNull(5, Types.VARCHAR);
            }

            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt(DB_COLUMN_ID);
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return id;
    }

}
