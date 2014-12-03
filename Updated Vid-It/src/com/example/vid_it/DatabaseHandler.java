package com.example.vid_it;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler {
	
	/* Static Variables */
	
	//Database class variables
	private SQLiteDatabase database;
	private DatabaseOpenHelper databaseOpenHelper;
	
	//Database Version
	private static final int Database_Version = 1;
	
	//Database name
	private static final String Database_Name = "Vid-It_Database";
	
	//Database table name
	private static final String Table_Database = "Vid-It_Table";
	
	//Database Table Column Names
	private static final String Key_ID = "id";
	private static final String Key_Name = "name";
	private static final String Key_Email = "email";
	private static final String Key_Password = "password";
	private static final String Key_Cloud_Username = "cloudUsername";
	private static final String Key_Cloud_Password = "cloudPassword";
	
	public static final String hashMethod = SecurityHandler.getAppropriateHash();
	
	public DatabaseHandler(Context context) {
		databaseOpenHelper = new DatabaseOpenHelper(context, Database_Name, null, Database_Version);
	}
	
	public void open() throws SQLException {
		database = databaseOpenHelper.getWritableDatabase();
	}
	
	public void close() {
		if(database != null)
			database.close();
	}
	
	// getAuthetication
	public boolean getAuthentication(String _email, String _password) {
		//will come back to this
		boolean valid = true;
		Log.d("getAuthentication()", "some statement");
		
		String hashedpassword = SecurityHandler.getHashedPassword( hashMethod, _password );		
		
		Cursor validity = database.query(Table_Database, null, 
										Key_Email+"='"+_email+"' AND "+Key_Password+"='"+hashedpassword+"'",
										null, null, null,null);
		if(validity == null)
			valid = false;
		
		validity.close();
		
		return valid;
	}
	
	// getPreferences
	public List<String> getPreferences(String _name, String _email) {
		List<String> preferences = new ArrayList<String>();
		
		Cursor cursor = database.query(Table_Database, null, Key_Name+"='"+_name+"' AND "+Key_Email+"='"+_email+"'", null, null, null, null);
		
		Log.d("getPreferences()", "trying to get preferences");
		//create a cursor to traverse the rows
		if(cursor == null) {
			Log.d("getPreferences()", "cursor returned null; cursor was empty");
			return preferences;
		}
		else {
			cursor.moveToFirst();

			// retrieve the name, cloud user name, and cloud password from the query
			String namepref = cursor.getString(1);
			String cloudName = cursor.getString(5);
			String cloudPass = cursor.getString(6);

			//will add the name, cloud user name, and cloud password
			preferences.add(namepref);
			preferences.add(cloudName);
			preferences.add(cloudPass);

			cursor.close();
			return preferences; 
		}
	}
	
	// setPreferences
	public void setPreferences(String _name, String _email, String _cloudName, String _cloudPassword) {
		String setPref = "UPDATE " + Table_Database
							+ " SET " + Key_Cloud_Username + " = '" + _cloudName + "', " + Key_Cloud_Password + " = '" + _cloudPassword 
							+ "' WHERE " + Key_Name + " = '" + _name + "' AND " + Key_Email + " = '" + _email + "';";
		Log.d("setPreferences()", setPref);
		database.execSQL(setPref);
		database.close();
	}
	
	// addNewUserInfo
	public void addNewUser(String name, String email, String password) {
		String addUser = "INSERT INTO " + Table_Database 
							+ " (" + Key_Name + ", " + Key_Email + ", " + Key_Password + ")"
							+ "VALUES ('" + name + "', '" + email + "', '" + password + "')";
		Log.d("addNewUser()", addUser);
		database.execSQL(addUser);
		database.close();	
	}
	
	// deleteUserInfo
	public void deleteUser(String _name, String _email) {
		open();
		database.delete(Table_Database, Key_Name + " = '" + _name + "' AND " + Key_Email + " = '" + _email, null);
		close();
	}
	
	//DatabaseOpenHelper class
	private class DatabaseOpenHelper extends SQLiteOpenHelper {
		public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			String Create_Table = "CREATE TABLE IF NOT EXISTS " + Table_Database + " (" 
											+ Key_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
											+ Key_Name + " TEXT, "
											+ Key_Email + " TEXT, "
											+ Key_Password + " TEXT, "
											+ Key_Cloud_Username + " TEXT, "
											+ Key_Cloud_Password + " TEXT)";
			db.execSQL(Create_Table);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Do nothing here; keep same version; version = 1
		}
	}
}
