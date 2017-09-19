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
import de.rub.syssec.saaf.db.dao.interfaces.NuClassDAO;
import de.rub.syssec.saaf.model.application.ClassInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MySQLClassDAO implements NuClassDAO {

    private static final String DB_COLUMN_ID = "id";
    private static final String DB_COLUMN_CODELINES = "codelines";
    private static final String DB_COLUMN_HASH_FUZZY = "hash_fuzzy";
    private static final String DB_COLUMN_NAME = "name";
    private static final String DB_COLUMN_SOURCE = "source";
    private static final String DB_COLUMN_EXTENDS = "extends";
    private static final String DB_COLUMN_IMPLEMENTS = "implements";
    private static final String DB_COLUMN_PACKAGES = "id_packages";
    private static final String DB_COLUMN_CM_ENTROPY = "cm_entropy";
    private static final String DB_COLUMN_CMF_ENTROPY = "cmf_entropy";
    private static final String DB_COLUMN_AVG_ENTROPY = "avg_entropy";

    private static final String DB_QUERY_INSERT = "INSERT INTO classes "
            + "("
            + DB_COLUMN_PACKAGES + ", "
            + DB_COLUMN_CODELINES + ", "
            + DB_COLUMN_HASH_FUZZY + ", "
            + DB_COLUMN_NAME + ", "
            + DB_COLUMN_SOURCE + ", "
            + DB_COLUMN_EXTENDS + ", "
            + DB_COLUMN_IMPLEMENTS + ", "
            + DB_COLUMN_CM_ENTROPY + ","
            + DB_COLUMN_CMF_ENTROPY + ","
            + DB_COLUMN_AVG_ENTROPY
            + ")VALUES(?,?,?,?,?,?,?,?,?,?)";

    private static final String DB_QUERY_UPDATE = "UPDATE classes SET "
            + DB_COLUMN_PACKAGES + "=?, "
            + DB_COLUMN_CODELINES + "=?, "
            + DB_COLUMN_HASH_FUZZY + "=?, "
            + DB_COLUMN_NAME + "=?, "
            + DB_COLUMN_SOURCE + "=?, "
            + DB_COLUMN_EXTENDS + "=?, "
            + DB_COLUMN_IMPLEMENTS + "=?, "
            + DB_COLUMN_CM_ENTROPY + "=?,"
            + DB_COLUMN_CMF_ENTROPY + "=?,"
            + DB_COLUMN_AVG_ENTROPY + "=? WHERE "
            + DB_COLUMN_ID + "=?";

    private static final String DB_QUERY_DELETE = "DELETE FROM classes WHERE " + DB_COLUMN_ID + "=?";
    private static final String DB_QUERY_DELETE_ALL = "DELETE FROM classes";
    private static final String DB_QUERY_FIND_EXISITING = "SELECT * FROM classes WHERE" + DB_COLUMN_PACKAGES + "=? AND "
            + DB_COLUMN_HASH_FUZZY + "=? AND "
            + DB_COLUMN_NAME + "=?";

    private Connection connection;

    /**
     * Creates a MySQLClassDAO that uses the supplied Connection.
     *
     * @param connection
     */
    public MySQLClassDAO(Connection connection) {
        super();
        this.connection = connection;
    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
     */
    @Override
    public int create(ClassInterface entity) throws DAOException, DuplicateEntityException {
        int id;
        int index = 0;
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(DB_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(++index, entity.getPackageId());
            statement.setInt(++index, entity.getLinesOfCode());

            if (entity.getSsdeepHash() != null) {
                statement.setString(++index, entity.getSsdeepHash());
            } else {
                statement.setNull(++index, Types.VARCHAR);
            }

            if (entity.getClassName() != null) {
                statement.setString(++index, entity.getClassName());
            } else {
                statement.setNull(++index, Types.VARCHAR);
            }

            if (entity.getSourceFile() != null) {
                statement.setString(++index, entity.getSourceFile());
            } else {
                statement.setNull(++index, Types.VARCHAR);
            }

            if (entity.getSuperClass() != null) {
                statement.setString(++index, entity.getSuperClass());
            } else {
                statement.setNull(++index, Types.VARCHAR);
            }

            if (entity.getImplementedInterfaces() != null) {
                statement.setString(++index, entity.getImplementedInterfaces().toString());
            } else {
                statement.setNull(++index, Types.VARCHAR);
            }

            if (entity.getEntropy() != null) {
                statement.setDouble(++index, entity.getEntropy().CMEntropy);
                statement.setDouble(++index, entity.getEntropy().CMFEntropy);
                statement.setDouble(++index, entity.getEntropy().AverageEntropy);
            } else {
                statement.setNull(++index, Types.DOUBLE);
                statement.setNull(++index, Types.DOUBLE);
                statement.setNull(++index, Types.DOUBLE);
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
        return id;

    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#read(int)
     */
    @Override
    public ClassInterface read(int id) throws DAOException {
        throw new UnsupportedOperationException("Reading Classes from Database is currently not supported");
    }

    /* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
     */
    @Override
    public boolean update(ClassInterface entity) throws DAOException, NoSuchEntityException {
        boolean success = false;
        int index = 0;
        int recordsUpdated = 0;
        try {
            PreparedStatement update = connection.prepareStatement(DB_QUERY_UPDATE);
            update.setInt(++index, entity.getPackageId());
            update.setInt(++index, entity.getLinesOfCode());
            if (entity.getSsdeepHash() != null) {
                update.setString(++index, entity.getSsdeepHash());
            } else {
                update.setNull(++index, Types.VARCHAR);
            }
            if (entity.getClassName() != null) {
                update.setString(++index, entity.getClassName());
            } else {
                update.setNull(++index, Types.VARCHAR);
            }
            if (entity.getSourceFile() != null) {
                update.setString(++index, entity.getSourceFile());
            } else {
                update.setNull(++index, Types.VARCHAR);
            }
            if (entity.getSuperClass() != null) {
                update.setString(++index, entity.getSuperClass());
            } else {
                update.setNull(++index, Types.VARCHAR);
            }
            if (entity.getImplementedInterfaces() != null) {
                update.setString(++index, entity.getImplementedInterfaces().toString());
            } else {
                update.setNull(++index, Types.VARCHAR);
            }

            if (entity.getEntropy() != null) {
                update.setDouble(++index, entity.getEntropy().CMEntropy);
                update.setDouble(++index, entity.getEntropy().CMFEntropy);
                update.setDouble(++index, entity.getEntropy().AverageEntropy);
            } else {
                update.setNull(++index, Types.DOUBLE);
                update.setNull(++index, Types.DOUBLE);
                update.setNull(++index, Types.DOUBLE);
            }
            update.setInt(++index, entity.getId());
            recordsUpdated = update.executeUpdate();
            // this should affect at most one record
            if (recordsUpdated == 0) {
                throw new NoSuchEntityException();
            } else if (recordsUpdated == 1) {
                success = true;
            } else {
                // the update affected multiple records this should not happen!
                throw new DAOException("Update affected multiple records. This should not happen!");
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
    public boolean delete(ClassInterface entity) throws DAOException, NoSuchEntityException {
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
                throw new DAOException("Delete affected multiple records. This should not happen!");
            }

        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return success;
    }

    @Override
    public List<ClassInterface> readAll() {
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
    public int findId(ClassInterface candidate) throws DAOException {
        int id = 0;
        PreparedStatement selectStmt;
        try {
            selectStmt = connection.prepareStatement(DB_QUERY_FIND_EXISITING);
            selectStmt.setInt(1, candidate.getPackageId());
            selectStmt.setString(2, candidate.getSsdeepHash());
            selectStmt.setString(3, candidate.getClassName());
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
