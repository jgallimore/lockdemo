/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.superbiz.lockdemo.application;

import org.superbiz.lockdemo.model.Row;

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Singleton
@Lock(LockType.READ)
/**
 * This class is implemented as a Singleton EJB. EJBs are transactional by default, so this makes
 * removes any work for us as the developer to any heavy lifting around transactions.
 *
 * Because it is a Singleton, this class will only be instantiated once by the EJB container, and all
 * threads invoking its business methods will be using the same instance of the bean. By default,
 * Singleton beans use LockType.WRITE as their lock type, meaning only one thread can run a business
 * method at a time. Where a Singleton has some state (for example, some fields on the singleton itself)
 * this is the safest option, but it does negatively effect concurrency. This bean does not have state,
 * so it has been set to LockType.READ, which will allow multiple threads to execute business methods
 * concurrently.
 *
 * This is quite useful for us, as it demonstrates the effect of synchronizing method calls, and how
 * allowing more concurrency may negatively impact the database if SQL statements lock rows or tables
 * for a long time.
 *
 * Try running making two calls to the updateAndProcess() method concurrently and take a threaddump.
 * Then switch the LockType above from READ to WRITE, and repeat the test.
 *
 * The thread dumps should show the differences quite clearly.
 */
public class DatabaseService {

    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());

    @Resource
    private DataSource ds;

    /**
     * This method simply selects all the rows in the database table, and returns the data as a collection
     * of {@link Row} objects.
     *
     * @return All the rows in the database table as a collection of {@link Row} objects
     */
    public List<Row> selectAll() {
        LOGGER.info(String.format("[%d] Starting selectAll", Thread.currentThread().getId()));

        try (final Connection c = ds.getConnection();
             final PreparedStatement ps = c.prepareStatement("select a,b from t");
             final ResultSet rs = ps.executeQuery()) {

            int count = 0;

            final List<Row> results = new ArrayList<>();

            while (rs.next()) {
                final int a = rs.getInt(1);
                final int b = rs.getInt(2);

                results.add(new Row(a, b));
                count++;
            }

            LOGGER.info(String.format("[%d] Fetched %d rows in selectAll", Thread.currentThread().getId(), count));

            return results;
        } catch (Exception e) {
            LOGGER.severe(String.format("[%d] Error while running selectAll", Thread.currentThread().getId()));
            throw new RuntimeException(e);
        } finally {
            LOGGER.info(String.format("[%d] Completed selectAll", Thread.currentThread().getId()));
        }
    }

    /**
     * This method will run a SQL update in a transaction, and then do some "processing" (in this
     * case, we'll just sleep). The transaction will run for as long as the bean method invocation takes.
     *
     * All of the rows affected by the update will be locked in MySQL for the duration of the transaction.
     * Another thread that also attempts to update or read the records will not be able to do so until this method
     * completes, and the transaction either commits, or rolls back.
     */
    public int updateAndProcess() {
        LOGGER.info(String.format("[%d] Starting updateAndProcess", Thread.currentThread().getId()));

        try (final Connection c = ds.getConnection();
             final PreparedStatement ps = c.prepareStatement("update t set b = ?")) {

            ps.setInt(1, new Random().nextInt(10));
            final int rowsUpdated = ps.executeUpdate();

            LOGGER.info(String.format("[%d] Updated %d rows in updateAndProcess, now sleeping for 60 seconds", Thread.currentThread().getId(), rowsUpdated));

            // This is our "processing" - we're just going to sleep for 1 minute to keep the transaction running
            Thread.sleep(60000);

            LOGGER.info(String.format("[%d] 60 s Sleep finished in updateAndProcess", Thread.currentThread().getId()));

            return rowsUpdated;

        } catch (Exception e) {
            LOGGER.severe(String.format("[%d] Error while running updateAndProcess", Thread.currentThread().getId()));
            throw new RuntimeException(e);
        } finally {
            LOGGER.info(String.format("[%d] Completed updateAndProcess", Thread.currentThread().getId()));
        }
    }

}
