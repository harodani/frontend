/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
package project.cs.netinfservice.netinf.node.resolution;

import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import project.cs.netinfservice.application.MainNetInfApplication;
import project.cs.netinfservice.database.DatabaseException;
import project.cs.netinfservice.database.IODatabase;
import project.cs.netinfservice.database.IODatabaseFactory;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfutilities.UProperties;
import android.util.Log;

import com.google.inject.Inject;

/**
 * A local resolution service that provides access to the local database.
 *
 * @author Kim-Anh Tran
 *
 */
public class LocalResolutionService
extends AbstractResolutionServiceWithoutId {

    /** The debug tag. */
    private static final String TAG = "LocalResolutionService";

    /** The factory creating the database. */
    @Inject
    private IODatabaseFactory mDatabaseFactory;

    /** The local database used for storing information objects. */
    private IODatabase mDatabase;

    /** DataModel Factory. **/
    private DatamodelFactory mDatamodelFactory;

    /**
     * Creates a new local resolution service.
     *
     * @param databaseFactory
     *     	The factory used for creating the database.
     * @param datamodelFactory
     *     	The factory used for creating information objects.
     */
    @Inject
    public LocalResolutionService(IODatabaseFactory databaseFactory,
            DatamodelFactory datamodelFactory) {
        // Database factory
        mDatabaseFactory = databaseFactory;

        // DataModel Factory
        mDatamodelFactory = datamodelFactory;

        // Database
        mDatabase = mDatabaseFactory.create(MainNetInfApplication.getAppContext());
    }

    /**
     * Deletes an identifier from database.
     * 
     * @param identifier
     *      Identifier to be deleted.
     */
    @Override
    public void delete(Identifier identifier) {
        Log.d(TAG, "Deleting IO from database.");

        // Extract hash
        String hash = identifier.getIdentifierLabel(
                SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();

        Log.d(TAG, "IO hash to be deleted: " + hash);

        // Calls deleteIO function
        mDatabase.deleteIO(hash);
    }
    
    /**
     * Tries to retrieve an IO from the database using an identifier.
     * 
     * @param identifier
     *      Identifier with the information about the object to be retrieved.
     * @return
     *      Information Object retrieved from the Database.
     *      <p>null if get procedure failed.
     */
    @Override
    public InformationObject get(Identifier identifier) {
        Log.d(TAG, "Get an IO from the database.");
        
        // Extracts hash from the identifier
        String hash = identifier.getIdentifierLabel(
                SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();

        InformationObject io = null;
        
        // Tries to fetch the IO from the database using the hash
        try {
            io = mDatabase.getIO(hash);
        } catch (DatabaseException e) {
            Log.e(TAG, "Couldn't retrieve the information object associated with the hash = "
                    + hash);
            
            // If it fails, return null
            return null;
        }

        // Returns IO
        return io;
    }

    /**
     * Adds an Information Object to the IO.
     * 
     * @param io
     *      Information Object to be added.
     */
    @Override
    public void put(InformationObject io) {
        Log.d(TAG, "put()");
        
        Log.d(TAG, "Trying to put an IO into the database");
        
        // Tries to add IO using the addIO function from the IODatabase class
        try {
            mDatabase.addIO(io);
        } catch (DatabaseException e) {
            Log.e(TAG, "Failed adding the information object into the database.");
        }
    }

    /**
     * Creates an Identity Object for the Local Resolution Service.
     *
     * @return
     *      The IdentityObject that was created for this Resolution Service.
     */
    @Override
    protected ResolutionServiceIdentityObject createIdentityObject() {
        // Create an Identity Object using the datamodel factory
        ResolutionServiceIdentityObject identity = mDatamodelFactory
                .createDatamodelObject(ResolutionServiceIdentityObject.class);
        
        // Set attributes
        identity.setName(TAG);
        int priority = Integer.parseInt(UProperties.INSTANCE
                .getPropertyWithName("lrs.priority"));
        identity.setDefaultPriority(priority);
        identity.setDescription(describe());
        
        // Return the new Identity Object
        return identity;
    }

    /**
     * Not supported.
     */
    @Override
    public List<Identifier> getAllVersions(Identifier arg0) {
        return null;
    }

    @Override
    public String describe() {
        return "Database Service";
    }
}
