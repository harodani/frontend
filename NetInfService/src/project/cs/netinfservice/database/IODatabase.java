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
package project.cs.netinfservice.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import project.cs.netinfservice.application.MainNetInfActivity;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfservice.netinf.node.search.SearchResult;
import project.cs.netinfservice.netinf.node.search.SearchResultImpl;
import project.cs.netinfservice.util.IOBuilder;
import project.cs.netinfutilities.UProperties;
import project.cs.netinfutilities.metadata.Metadata;
import project.cs.netinfutilities.metadata.MetadataParser;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * The database that contains the data corresponding to an information object
 * that is stored in the device.
 * 
 * @author Harold Martinez
 * @author Kim-Anh Tran
 *
 */
public class IODatabase 
		extends SQLiteOpenHelper
		implements IODatabaseFactory {

	/** The current database version. */
	public static final int DATABASE_VERSION = 1;
	
	/** Debug Tag. */
	private static final String TAG = "IODatabase";
	
	/** The name of the database. */
	private static final String DATABASE_NAME = "IODatabase"; 
	
	/** The name of the table containing our Information Object information. */
	private static final String TABLE_IO = "IO";
	
	/** The name of the table containing the url values corresponding to each hash value. */
	private static final String TABLE_URL = "IO_url";
	
	/** The hash value corresponding to the IO. This is the primary key. */
	private static final String KEY_HASH = "hash";
	
	/** The hash algorithm used to create the hash value. */
	private static final String KEY_HASH_ALGORITHM = "hash_algorithm";
	
	/** The Filepath that determines the location of the file on the device. */
	private static final String KEY_FILEPATH = "filepath";
	
	/** The content type of the file associated with the IO. */
	private static final String KEY_CONTENT_TYPE = "content_type";
	
	/** The URL associated with the file. */
	private static final String KEY_URL = "url";
	
	/** The file size of the file associated with the IO. */
	private static final String KEY_FILE_SIZE = "file_size";
	
    /** Local File system (database) transmission used to transfer a resource. */
    public static final String LOCAL_TRANSMISSION = "project.cs.netinfservice.LOCAL_TRANSMISSION";

    /** Meta-data label for the filepath. */
	private final String mFilepathLabel;
	
	/** Meta-data label for the file size. */
	private final String mFilesizeLabel;
	
	/** Meta-data label for the url. */
	private final String mUrlLabel;
	
	/** The datamodel factory used for constructing the IO. */
	private DatamodelFactory mDatamodelFactory;

	/**
	 * Creates a new Database for storing IO information.
	 * 
	 * @param context
	 * 		The application context
	 * @param datamodelFactory
	 *     	The factory that is used in order to create information objects.
	 */
	@Inject
	public IODatabase(DatamodelFactory datamodelFactory, @Assisted Context context) {
		// We skip the cursor object factory, since we don't need it
		super(context, DATABASE_NAME, null, 1); 

		// Fetch properties
		UProperties instance = UProperties.INSTANCE;
		mFilepathLabel = instance.getPropertyWithName("metadata.filepath");
		mFilesizeLabel = instance.getPropertyWithName("metadata.filesize");
		mUrlLabel = instance.getPropertyWithName("metadata.url");
		
		// Get data model
		mDatamodelFactory = datamodelFactory;
	}
	
	/**
	 * Called when the database is created for the first time. This is where the creation of
	 * tables and the initial population of the tables should happen.
	 * 
	 * @param db
	 *     The SQLite database.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createIoTable = "CREATE TABLE " + TABLE_IO + "(" 
							+ KEY_HASH + " TEXT PRIMARY KEY," 
							+ KEY_HASH_ALGORITHM + " TEXT NOT NULL, "
							+ KEY_CONTENT_TYPE + " TEXT NOT NULL, "
							+ KEY_FILEPATH + " TEXT NOT NULL, "
							+ KEY_FILE_SIZE + " REAL NOT NULL CHECK(" + KEY_FILE_SIZE + " > 0.0))";
		
		String createUrlTable = "CREATE TABLE " + TABLE_URL + "(" 
							+ KEY_HASH + " TEXT NOT NULL, "
							+ KEY_URL + " TEXT NOT NULL, " 
							+ "CONSTRAINT primarykey PRIMARY KEY " 
							+ "( " + KEY_HASH + ", " + KEY_URL + "), "
							+ "FOREIGN KEY (" + KEY_HASH + ") " 
							+ "REFERENCES " + TABLE_IO + " ( " + KEY_HASH + ") "
							+ "ON DELETE CASCADE )";
		
		db.execSQL(createIoTable);
		db.execSQL(createUrlTable);
	}

	/**
	 * Called when the database has been opened. The implementation should check isReadOnly() 
	 * before updating the database. This method is called after the database connection has 
	 * been configured and after the database schema has been created, upgraded or downgraded 
	 * as necessary. If the database connection must be configured in some way before the schema 
	 * is created, upgraded, or downgraded, do it in onConfigure(SQLiteDatabase) instead.
	 */
	@Override
	public void onOpen(SQLiteDatabase db) {
	    super.onOpen(db);
	    
	    // Checks if the database is read-only or not
	    if (!db.isReadOnly()) {
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}

	/**
	 * Called when the database needs to be upgraded. The implementation should use this method
	 * to drop tables, add tables, or do anything else it needs to upgrade to the new schema
	 * version.
	 * 
	 * @param db
	 *     The SQLite database.
	 * @param oldVersion
	 *     Old database version number.
	 * @param newVersion
	 *     New database version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Upgrading database to version " + newVersion);

		// Drop tables to re-build them later
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IO);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_URL);

		// Re-run the creation of the database
		onCreate(db);
	}

	/**
	 * Creates a new IODatabase object.
	 * 
	 * @param context
	 *     The context where the database will be created for.
	 * @return
	 *     The new IO database object.
	 */
	@Override
	public IODatabase create(Context context) {
		return new IODatabase(mDatamodelFactory, context);
	}
	
	/**
	 * Inserts the specified information object into the database.
	 * 
	 * @param io					The information object to insert.
	 * @throws DatabaseException 	thrown if insert operation fails
	 */
	@SuppressWarnings("unchecked")
	public void addIO(InformationObject io) throws DatabaseException  {
		Log.d(TAG, "Received an add information object call.");

		// Extract the field values for inserting them into the database tables
		// Get the Identifier from the Information Object 
		Identifier identifier = io.getIdentifier();
		
		// Get hash, hashAlgorithm and content-type
		String hash = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
		
		String hashAlgorithm = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
		
		String contentType = identifier.getIdentifierLabel(
				SailDefinedLabelName.CONTENT_TYPE.getLabelName()).getLabelValue();
		
		// Extract meta data 
		String metadata = identifier.getIdentifierLabel(
						SailDefinedLabelName.META_DATA.getLabelName()).getLabelValue();
		
		// Extract the metadata to a map
		Map<String, Object> metadataMap = extractMetaData(metadata);
		
		// TODO: Remove this line?
		System.out.println("Metadata: adding " + metadata);
		
		String filePath = (String) metadataMap.get(mFilepathLabel);
		String fileSize = (String) metadataMap.get(mFilesizeLabel);
		
		// Create list of URLs
		Object urlJsonObject = metadataMap.get(mUrlLabel);
		
		// Populate urlList with one or several URLs
		List<String> urlList;
		
		// TODO: Ask Kim for input on what this does.
		if (urlJsonObject instanceof ArrayList) {
			urlList  = (ArrayList<String>) urlJsonObject;
		} else {
			String url = (String) urlJsonObject;
			urlList = new ArrayList<String>();
			urlList.add(url);
		}

		// If the objects hash is not in the database, insert it
		if (!containsIO(hash)) {
			Log.d(TAG, "New information object will be inserted into database.");
			// Insert the IO
			ContentValues ioEntry = 
					createIOEntry(hash, hashAlgorithm, contentType, filePath, fileSize);
			// TODO: Better to use Log.d?
			System.out.println("New information object: " + urlList.toString());
			insert(TABLE_IO, ioEntry);
		} else {
			Log.d(TAG, "Information object already exists in database.");
			// Check if the URLs that we want to insert already exist
			List<String> storedUrls = getURLs(hash);
			urlList.removeAll(storedUrls);	
		}
				
		Log.d(TAG, "Inserting the following URLs:");
		
		// Insert the URLs
		for (String url : urlList) {
		    // Log the URL to be inserted
			Log.d(TAG, url);
			// Create a ContentValues object, which is recognized by ContentResolver
			ContentValues urlEntry = createUrlEntry(hash, url);
			insert(TABLE_URL, urlEntry);
		}
	}
	
	/**
	 * Inserts a value in the database.
	 * 
	 * @param table
     *      The table where the values will be inserted
	 * @param values
	 *      The values that will be inserted
	 */
	private void insert(String table, ContentValues values) {
	    // TODO: Maybe this should raise an exception? Writing might not be available.
		// Get database
	    SQLiteDatabase db = this.getWritableDatabase();
		
		// Insert value into table
		db.insert(table, null, values);	
		
		// Close connection
		db.close();
	}

	/**
	 * Returns the list of URL associated with a hash.
	 * 
	 * @param hash
	 *     A hash from an information object
	 * @return
     *     The list of URL in the IO_url table corresponding to the given hash
	 */
	public List<String> getURLs(String hash) {
		List<String> urlList = new ArrayList<String>();

		Cursor cursor = null;
		
		// Make database query
		try {
			cursor = query(TABLE_URL, KEY_HASH, hash);
		} catch (DatabaseException e) {
			return urlList;
		}
		
		// add all results to list
		do {
			urlList.add(cursor.getString(1));
		} while (cursor.moveToNext());
		
		return urlList;
	}
	
	/**
	 * Returns the information object specified by the hash value, if existent.
	 * 
	 * @param hash
	 *     The hash value identifying the information object
	 * @return
	 *     The information object
	 * @throws DatabaseException
	 *     Thrown when the query does not return any value
	 */
	public InformationObject getIO(String hash) throws DatabaseException {
		Log.d(TAG, "Searching for information object.");
		
		// Query. If it fails, it will throw a DatabaseException
		Cursor cursor = query(TABLE_IO, KEY_HASH, hash);
		
		Log.d(TAG, "Found information object.");
		
        Log.d(TAG, "Sending Intent " + LOCAL_TRANSMISSION);
        
        // Create and send an intent for local transmission
        if (MainNetInfActivity.getActivity() != null) {
        	Intent intent = new Intent(LOCAL_TRANSMISSION);        
        	MainNetInfActivity.getActivity().sendBroadcast(intent);
        }

        // Build a new IO to host the result
		IOBuilder builder = new IOBuilder(mDatamodelFactory);
		
		// Populate the fields of the IO
		builder.setHash(cursor.getString(0))
			.setHashAlgorithm(cursor.getString(1))
			.setContentType(cursor.getString(2))
			.addFilePathLocator(cursor.getString(3))
			.addMetaData(mFilepathLabel, cursor.getString(3))
			.addMetaData(mFilesizeLabel, cursor.getString(4));

		// Get URLs related to the hash
		cursor = query(TABLE_URL, KEY_HASH, hash);
		
		// Add URLs to metadata
		do {
			builder.addMetaData(mUrlLabel, cursor.getString(1));
		} while (cursor.moveToNext());
		
		// Return the Information Object built
		return builder.build();
	}
	
	/**
	 * Returns the information object corresponding to the url,
	 * if existent.
	 * 
	 * @param url
     *      The url that identifies the information object
	 * @return
	 * 		The information object
	 * @throws DatabaseException
	 *     	Is thrown if the url doesn't belong to any stored information object 
	 */
	@SuppressWarnings("unchecked") // Because of urlArray.add(cursor..)
    public SearchResult searchIO(String url) throws DatabaseException {
	    // Metadata holder. Uses our Metadata class
		Metadata metadata = new Metadata();
		
		// Find the hash identification of the corresponding object
		Cursor cursor = query(TABLE_URL, KEY_URL, url);
		String hash = cursor.getString(0);
		
		// Add all url fields
		JSONArray urlArray = new JSONArray();
		cursor = query(TABLE_URL, KEY_HASH, hash);
		do {
			urlArray.add(cursor.getString(1));
		} while (cursor.moveToNext());
		metadata.insert(KEY_URL, urlArray);
		
		// Build the metadata corresponding to the hash
		cursor = query(TABLE_IO, KEY_HASH, hash);
		
		metadata.insert(mFilepathLabel, cursor.getString(3));
		metadata.insert(mFilesizeLabel, cursor.getString(4));
		
		// Get the hash algorithm used to create the object's hash
		String hashAlg = cursor.getString(1);

		// Return a SearchResult object, accessible through the SearchResult interface
		return new SearchResultImpl(hash, hashAlg, metadata);
	}

	/**
	 * Deletes the information object corresponding to the specified hash value from the database. 
	 * 
	 * @param hash
	 *      The hash value identifying the information object.
	 */
	public void deleteIO(String hash) {
	    // TODO: Check what to do when it fails, as getWritableDatabase throws SQLiteException. 
		Log.d(TAG, "Deleting io corresponding to the following hash: " + hash);
		SQLiteDatabase db = getWritableDatabase();
		
		// Finds and deletes object
		db.delete(TABLE_IO, KEY_HASH + " = ?", new String[] {hash});
		
		db.close();
	}
	
	/**
	 * Deletes the information object that is specified from the database.
	 * 
	 * @param io
	 *      The information object to delete.
	 */
	public void deleteIO(InformationObject io) {
		Log.d(TAG, "Deleting an information object from the database.");
		
		// Get the Identifier from the InformationObject
		Identifier identifier = io.getIdentifier();
		
		// Extract the hash
		String hash = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
		
		// Delete the object by calling deleteIO(hash)
		deleteIO(hash);
	}
	
	/**
	 * Returns the map corresponding to the meta data contained in the specified Metadata String.
	 * 
	 * @param metadata
	 *      The complete meta-data String
	 * @return
	 * 		A map containing the meta data key value pairs
	 * @throws DatabaseException
	 *     	Thrown, if a failure occurred during extracting
	 */
	private Map<String, Object> extractMetaData(String metadata) throws DatabaseException {
	    // Parse metadata to create an JSON Object.
		Object jsonObject = JSONValue.parse(metadata);
		
		// If we could not get a JSONObject from the metadata, throw DatabaseException
		if (!(jsonObject instanceof JSONObject)) {
			Log.e(TAG, "Invalid metadata.");
			
			// Throw exception
			throw new DatabaseException(
					"Metadata in the information object was an invalid JSONObject.");
		}
		
		// Create mapping for metadata. Object may be JSON Object or JSON Array
		Map<String, Object> metadataMap = null;
		
		
		// Parse metadata again, this time adding objects to the map to reflect "String" : "Value" 
		try {
			metadataMap = MetadataParser.toMap((JSONObject) jsonObject);
		} catch (ParseException e) {
			Log.e(TAG, "Error extracting metadata");
			throw new DatabaseException("The IO cannot be inserted into the database. "
					+ "Because the meta-data could not be extracted.", e);
		}
		
		// Return metadata Map (String, Object)
		return metadataMap;
	}
	
	/**
	 * Returns a content value object representing an entry in the IO table.
	 * 
	 * @param hash
	 * 		The hash value of the IO
	 * @param hashAlgorithm
	 *      The hash algorithm
	 * @param contentType
	 *     	The content type
	 * @param filePath
	 *     	The file path
	 * @param fileSize
	 *     	The file size
	 * @return
	 * 		The corresponding content value
	 */
	private ContentValues createIOEntry(
			String hash, String hashAlgorithm, String contentType, 
			String filePath, String fileSize) {
		// Create a new ContentValues object, readable by ContentResolver
		ContentValues ioEntry = new ContentValues();
		
		// Add attributes
		ioEntry.put(KEY_HASH, hash);
		ioEntry.put(KEY_HASH_ALGORITHM, hashAlgorithm);
		ioEntry.put(KEY_CONTENT_TYPE, contentType);
		ioEntry.put(KEY_FILEPATH, filePath);
		ioEntry.put(KEY_FILE_SIZE, fileSize);
		
		// Return the ContentValues with attributes
		return ioEntry;
	}
	
	/**
	 * Returns a content value object representing an entry in the IO_url table.
	 * 
	 * @param hash
	 *     	The hash value of the IO
	 * @param url
	 *     	The url where it can be found
	 * @return
	 * 		The corresponding content value
	 */
	private ContentValues createUrlEntry(String hash, String url) {
	    // Create new ContentValues object, which is processed by ContentResolver
		ContentValues urlEntry = new ContentValues();	

		// Add attributes
		urlEntry.put(KEY_HASH, hash);
		urlEntry.put(KEY_URL, url);
		
		// Return ContentValues with attributes
		return urlEntry;
	}
	
	/**
	 * Queries the database and returns a cursor.
	 * 
	 * @param table
	 * 		The table in which we want to query
	 * @param key
	 * 		The key
	 * @param value
	 * 		The corresponding value
	 * @return
	 * 		A cursor pointing to the first row of results
	 * @throws DatabaseException
	 *     	Thrown, if no entry was found for the specified key value pair
	 */
	private Cursor query(String table, String key, String value) throws DatabaseException {
	    // TODO: This might throw a SQLiteException, which we must handle!
		SQLiteDatabase db = this.getReadableDatabase();

		// Makes the query for (key,value)
		Cursor cursor = db.query(table, null, key + "=?", 
				new String[]{value}, null, null, null);
		
		// If there was any result, move the cursor to the first result
		if (cursor != null && cursor.getCount() != 0) {
			cursor.moveToFirst();
		} else {
		    // Fails if it does not find anything
		    db.close();
			throw new DatabaseException("The given key does not correspond to any IO : " + key);
		}
		
		db.close();
		
		// Return first object
		return cursor;
	}
	
	/**
	 * Query the database to see if a hash already exists.
	 * 
	 * @param hash
	 *     	The hash of an information object
	 * @return
	 * 		false if the hash does not exist,
	 *		true  if it does
	 */
	private boolean containsIO(String hash) {
		try {
			// Check if the IO we want to insert already exists
			query(TABLE_IO, KEY_HASH, hash);
		} catch (DatabaseException e) {
			// This exception is thrown if the query is empty, in that case we say that 
		    // the IO is not stored
			return false;
		}

		return true;
	}
}
