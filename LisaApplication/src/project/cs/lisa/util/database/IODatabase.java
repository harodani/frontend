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
package project.cs.lisa.util.database;

import java.util.List;
import java.util.Map;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;

import org.json.JSONException;
import org.json.JSONObject;

import project.cs.lisa.exceptions.DatabaseException;
import project.cs.lisa.metadata.MetadataParser;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The database that contains the data corresponding to an information object
 * that is stored in the device.
 * 
 * @author Harold Martinez
 * @author Kim-Anh Tran
 *
 */
public class IODatabase extends SQLiteOpenHelper {
	
	/** Debug Tag. */
	private static final String TAG = "IODatabase";
	
	/** The current database version. */
	private static final int DATABASE_VERSION = 1;
	
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
	
	/** The JSON String representing the meta-data as a whole. */
	private static final String KEY_METADATA = "meta_data";

	/**
	 * Creates a new Database for storing IO information.
	 * 
	 * @param context	The application context
	 */
	public IODatabase(Context context) {
		
		// We skip the curser object factory, since we don't need it
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createIoTable = "CREATE TABLE " + TABLE_IO + "(" 
							+ KEY_HASH + " TEXT PRIMARY KEY," 
							+ KEY_HASH_ALGORITHM + " TEXT NOT NULL, "
							+ KEY_FILEPATH + " TEXT NOT NULL, "
							+ KEY_CONTENT_TYPE + " TEXT NOT NULL, "
							+ KEY_URL + " TEXT NOT NULL, "
							+ KEY_FILE_SIZE + " REAL NOT NULL CHECK(" + KEY_FILE_SIZE + " > 0.0), "
							+ KEY_METADATA + " TEXT NOT NULL)";
		
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


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IO + " " + TABLE_URL);
		
		onCreate(db);
	}
	
	/**
	 * Inserts the specified information object into the database.
	 * 
	 * @param io					The information object to insert.
	 * @throws DatabaseException 	thrown if insert operation fails
	 */
	public void addIO(InformationObject io) throws DatabaseException  {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Extract the field values for inserting them into the database tables
		Identifier identifier = io.getIdentifier();
		String hash = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
		String hashAlgorithm = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
		String contentType = identifier.getIdentifierLabel(
				SailDefinedLabelName.CONTENT_TYPE.getLabelName()).getLabelValue();
		
		// Extract meta data 
		String metadata = identifier.getIdentifierLabel(
						SailDefinedLabelName.META_DATA.getLabelName()).getLabelValue();
		Map<String, Object> metadataMap = null;	
		try {
			metadataMap = MetadataParser.extractMetaData(new JSONObject(metadata));
		} catch (JSONException e) {
			Log.e(TAG, "Error extracting metadata");
			throw new DatabaseException("The IO cannot be inserted into the database. "
					+ "Because the meta-data could not be extracted.", e);
		}
				
		String filePath = (String) metadataMap.get("filepath");
		String fileSize = (String) metadataMap.get("filesize");
		// Will always be a list of Strings
		@SuppressWarnings("unchecked")
		List<String> urlList = (List<String>) metadataMap.get("url");
		
		ContentValues ioEntry = createIOEntry(hash, hashAlgorithm, contentType, filePath, fileSize);
		db.insert(TABLE_IO, null, ioEntry);
			
		for (String url : urlList) {
			ContentValues urlEntry = createUrlEntry(hash, url);
			db.insert(TABLE_URL, null, urlEntry);
		}
		
		db.close();		
	}

	/**
	 * Deletes the information object corresponding to 
	 * the specified hash value.
	 * 
	 * @param hash	The hash value identifying the information object.
	 */
	public void deleteIO(String hash) {
		
	}
	
	/**
	 * Returns the information object specified by the hash value, if existent.
	 * 
	 * @param hash	The hash value identifying the information object.
	 * @return		The information object.
	 */
	public InformationObject getIO(String hash) {
		return null;
	}
	
	/**
	 * Returns the list of all information objects that are stored in the
	 * database. 
	 * 
	 * @return	Returns a list of all information objects.
	 */
	public List<InformationObject> getAllIO() {
		return null;
	}
	
	/**
	 * Returns a content value object representing an entry in the IO table.
	 * 
	 * @param hash			The hash value of the IO
	 * @param hashAlgorithm The hash algorithm
	 * @param contentType	The content type
	 * @param filePath		The file path
	 * @param fileSize		The file size
	 * @return				The corresponding content value
	 */
	private ContentValues createIOEntry(
			String hash, String hashAlgorithm, String contentType, 
			String filePath, String fileSize) {
		
		ContentValues ioEntry = new ContentValues();
		
		ioEntry.put(KEY_HASH, hash);
		ioEntry.put(KEY_HASH_ALGORITHM, hashAlgorithm);
		ioEntry.put(KEY_CONTENT_TYPE, contentType);
		
		ioEntry.put(KEY_FILEPATH, filePath);
		ioEntry.put(KEY_FILE_SIZE, fileSize);
		
		return ioEntry;
	}
	
	/**
	 * Returns a content value object representing an entry in the IO_url table.
	 * 
	 * @param hash	The hash value of the IO
	 * @param url	The url where it can be found
	 * @return		The corresponding content value
	 */
	private ContentValues createUrlEntry(String hash, String url) {
		ContentValues urlEntry = new ContentValues();	
		urlEntry.put(KEY_HASH, hash);
		urlEntry.put(KEY_URL, url);
		return urlEntry;
	}
}
