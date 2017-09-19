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
package de.rub.syssec.saaf.db.persistence.nodb;

import java.util.List;

import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.HResultEntityManagerInterface;
import de.rub.syssec.saaf.model.analysis.HResultInterface;

public class NoDBHResultManager implements HResultEntityManagerInterface {

    @Override
    public boolean save(HResultInterface entity) throws InvalidEntityException,
            PersistenceException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean delete(HResultInterface entity)
            throws InvalidEntityException, PersistenceException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean validate(HResultInterface entity)
            throws InvalidEntityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<?> readAll(Class<?> entitClass) throws PersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<HResultInterface> readAll() throws PersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean saveAll(List<HResultInterface> entities)
            throws PersistenceException, InvalidEntityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void shutdown() throws PersistenceException {
        // TODO Auto-generated method stub

    }

}
