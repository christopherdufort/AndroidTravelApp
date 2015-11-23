package com.android.bonvoyagetravelapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "itinerary.db";
	private static final int DATABASE_VERSION = 1;

	// Table names
	private static final String TABLE_TRIPS = "trips";
	private static final String TABLE_LOCATIONS = "locations";
	private static final String TABLE_BUDGETED_EXPENSE = "budgeted_expense";
	private static final String TABLE_ACTUAL_EXPENSE = "actual_expense";

	// Database field names
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_TRIP_ID = "trip_id";
	private static final String COLUMN_CREATE_DATE = "create_date";
	private static final String COLUMN_UPDATE_DATE = "update_date";
	private static final String COLUMN_CLOSE_DATE = "close_date";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_DESCRIPTION = "description";
	private static final String COLUMN_CITY = "city";
	private static final String COLUMN_COUNTRY_CODE = "country_code";
	private static final String COLUMN_LOCATION_ID = "location_id";
	private static final String COLUMN_PLANNED_ARRIVAL_DATE = "planned_arrival_date";
	private static final String COLUMN_PLANNED_DEPARTURE_DATE = "planned_departure_date";
	private static final String COLUMN_AMOUNT = "amount";
	private static final String COLUMN_CATEGORY = "category";
	private static final String COLUMN_ARRIVAL_DATE = "arrival_date";
	private static final String COLUMN_DEPARTURE_DATE = "departure_date";
	private static final String COLUMN_NAME_OF_SUPPLIER = "name_of_supplier";
	private static final String COLUMN_ADDRESS = "address";

	// Database creation raw SQL statement
	// TODO: Choose a data type for the dates
	private static final String CREATE_TRIPS_TABLE = "create table " + TABLE_TRIPS + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TRIP_ID + " integer not null, " + COLUMN_CREATE_DATE
			+ " text not null, " + COLUMN_UPDATE_DATE + " text, " + COLUMN_CLOSE_DATE + " text, " + COLUMN_NAME
			+ " text not null, " + COLUMN_DESCRIPTION + " text not null);";

	private static final String CREATE_LOCATIONS_TABLE = "create table " + TABLE_LOCATIONS + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TRIP_ID + " integer not null, " + COLUMN_NAME
			+ " text not null, " + COLUMN_DESCRIPTION + " text not null, " + COLUMN_CITY + " text not null, "
			+ COLUMN_COUNTRY_CODE + " text not null, " + "foreign key (" + COLUMN_TRIP_ID + ") references "
			+ TABLE_TRIPS + "(" + COLUMN_ID + ") on delete cascade);";

	private static final String CREATE_BUDGETED_EXPENSE_TABLE = "create table " + TABLE_BUDGETED_EXPENSE + "( "
			+ COLUMN_ID + " integer primary key autoincrement, " + COLUMN_LOCATION_ID + " integer not null, "
			+ COLUMN_PLANNED_ARRIVAL_DATE + " text not null, " + COLUMN_PLANNED_DEPARTURE_DATE + " text not null, "
			+ COLUMN_AMOUNT + " real not null, " + COLUMN_DESCRIPTION + " text not null, " + COLUMN_CATEGORY
			+ " text not null, " + "foreign key (" + COLUMN_LOCATION_ID + ") references " + TABLE_LOCATIONS + "("
			+ COLUMN_ID + ") on delete cascade);";

	private static final String CREATE_ACTUAL_EXPENSE_TABLE = "create table " + TABLE_ACTUAL_EXPENSE + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_LOCATION_ID + " integer not null, " + COLUMN_ARRIVAL_DATE
			+ " text not null, " + COLUMN_DEPARTURE_DATE + " text not null, " + COLUMN_AMOUNT + " real not null, "
			+ COLUMN_DESCRIPTION + " text not null, " + COLUMN_CATEGORY + " text not null, " + COLUMN_NAME_OF_SUPPLIER
			+ " text not null, " + COLUMN_ADDRESS + " text not null, " + "foreign key (" + COLUMN_LOCATION_ID
			+ ") references " + TABLE_LOCATIONS + "(" + COLUMN_ID + " cascade));";

	// Static instance to share DBHelper
	private static DBHelper dbh = null;

	/**
	 * Constructor
	 * 
	 * super.SQLiteOpenHelper: public SQLiteOpenHelper (Context context, String
	 * name, SQLiteDatabase.CursorFactory factory, int version)
	 * 
	 * "Create a helper object to create, open, and/or manage a database."
	 * Remember the database is not actually created or opened until one of
	 * getWritableDatabase() or getReadableDatabase() is called.
	 * 
	 * constructor is private to prevent direct instantiation only getDBHelper()
	 * can be called and it will create or return existing
	 */
	private DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * getDBHelper(Context)
	 * 
	 * The static factory method getDBHelper make's sure that only one database
	 * helper exists across the app's lifecycle
	 * 
	 * A factory is an object for creating other objects. It is an abstraction
	 * of a constructor.
	 */
	public static DBHelper getDBHelper(Context context) {
		/*
		 * Use the application context, which will ensure that you don't
		 * accidentally leak an Activity's context. See this article for more
		 * information: http://bit.ly/6LRzfx
		 */
		if (dbh == null) {
			dbh = new DBHelper(context.getApplicationContext());
		}
		return dbh;
	} // getDBHelper()

	/**
	 * onCreate()
	 * 
	 * SQLiteOpenHelper lifecycle method used when we first create the database,
	 * in this case we create an empty database. You may want to populate your
	 * database with data when you create it, this is app dependent.
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 *      .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {
		/*
		 * this is one of the few places where it is not an horrific idea to use
		 * raw SQL.
		 */
		createPopulateDB(database);

	}

	/**
	 * onUpgrade()
	 * 
	 * SQLiteOpenHelper lifecycle method used when the database is upgraded to a
	 * different version, this one is simple, it drops then recreates the
	 * database, you loose all of the data This is not necessarily what you will
	 * want to do :p
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 *      .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		try {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETED_EXPENSE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTUAL_EXPENSE);
		} catch (SQLException e) {
			Log.e(DBHelper.class.getName(), "DROP exception" + Log.getStackTraceString(e));
			throw e;
		}
		createPopulateDB(db);
	}

	/**
	 * createPopulateDB()
	 * 
	 * method creates database table called in onCreate() and in onUpdate()
	 */
	public void createPopulateDB(SQLiteDatabase database) {
		try {
			database.execSQL(CREATE_TRIPS_TABLE);
			database.execSQL(CREATE_LOCATIONS_TABLE);
			database.execSQL(CREATE_BUDGETED_EXPENSE_TABLE);
			database.execSQL(CREATE_ACTUAL_EXPENSE_TABLE);
		} catch (SQLException e) {
			Log.e(DBHelper.class.getName(), "CREATE exception" + Log.getStackTraceString(e));
			throw e;
		}
	}

	// TODO: Note sure what trip id is
	/**
	 * Creates a new trip.
	 * 
	 * @param tripId
	 *            Trip id from the web
	 * @param name
	 *            Name of the trip.
	 * @param description
	 *            Description of the trip.
	 * @return Id of the newly inserted row or -1 if there was an error.
	 */
	public long createTrip(int tripId, String name, String description) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_TRIP_ID, tripId);
		cv.put(COLUMN_NAME, name);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CREATE_DATE, getDateTime(new Date()));

		long code = getWritableDatabase().insert(TABLE_TRIPS, null, cv);
		return code;
	}

	/**
	 * Creates a new location.
	 * 
	 * @param tripId
	 *            Id of the trip this location belongs to.
	 * @param name
	 *            Name of the location.
	 * @param description
	 *            Description of the location.
	 * @param city
	 *            City of the location.
	 * @param countryCode
	 *            Country code of the location.
	 * @return Id of the newly inserted row or -1 if there was an error.
	 */
	public long createLocation(int tripId, String name, String description, String city, String countryCode) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_TRIP_ID, tripId);
		cv.put(COLUMN_NAME, name);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CITY, city);
		cv.put(COLUMN_COUNTRY_CODE, countryCode);

		long code = getWritableDatabase().insert(TABLE_LOCATIONS, null, cv);
		return code;
	}

	/**
	 * Creates a new budgeted expense.
	 * 
	 * @param locationId
	 *            Id of the location this budgeted expense belongs to.
	 * @param plannedArrivalDate
	 *            Planned arrival date.
	 * @param plannedDepartureDate
	 *            Planned departure date.
	 * @param amount
	 *            Amount expected to be spent for the budgeted expense.
	 * @param description
	 *            Description of the budgeted expense.
	 * @param category
	 *            Category of the budgeted expense.
	 * @return Id of the newly inserted row or -1 if there was an error.
	 */
	public long createBudgetedExpense(int locationId, Date plannedArrivalDate, Date plannedDepartureDate, double amount,
			String description, String category) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_LOCATION_ID, locationId);
		cv.put(COLUMN_PLANNED_ARRIVAL_DATE, getDateTime(plannedArrivalDate));
		cv.put(COLUMN_PLANNED_DEPARTURE_DATE, getDateTime(plannedDepartureDate));
		cv.put(COLUMN_AMOUNT, amount);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CATEGORY, category);

		long code = getWritableDatabase().insert(TABLE_BUDGETED_EXPENSE, null, cv);
		return code;
	}

	/**
	 * Creates a new actual expense.
	 * 
	 * @param locationId
	 *            Id of the location this actual expense belongs to.
	 * @param arrivalDate
	 *            Arrival date.
	 * @param departureDate
	 *            departure date.
	 * @param amount
	 *            Amount spent for the actual expense.
	 * @param description
	 *            Description of the actual expense.
	 * @param category
	 *            Category of the actual expense.
	 * @param nameOfSupplier
	 *            Name of the supplier of the actual expense.
	 * @param address
	 *            Address of the actual expense.
	 * @return Id of the newly inserted row or -1 if there was an error.
	 */
	public long createActualExpense(int locationId, Date arrivalDate, Date departureDate, double amount,
			String description, String category, String nameOfSupplier, String address) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_LOCATION_ID, locationId);
		cv.put(COLUMN_ARRIVAL_DATE, getDateTime(arrivalDate));
		cv.put(COLUMN_DEPARTURE_DATE, getDateTime(departureDate));
		cv.put(COLUMN_AMOUNT, amount);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CATEGORY, category);
		cv.put(COLUMN_NAME_OF_SUPPLIER, nameOfSupplier);
		cv.put(COLUMN_ADDRESS, address);

		long code = getWritableDatabase().insert(TABLE_ACTUAL_EXPENSE, null, cv);
		return code;
	}

	/**
	 * Gets all the trips.
	 * 
	 * @return A cursor of all the trips.
	 */
	public Cursor getAllTrips() {
		return getReadableDatabase().query(TABLE_TRIPS, null, null, null, null, null, null);
	}

	/**
	 * Gets all the locations for a certain trip.
	 * 
	 * @param tripId
	 *            Trip id of the trip to get the locations of.
	 * @return A cursor of all the locations for a certain trip.
	 */
	public Cursor getLocations(int tripId) {
		String whereClause = COLUMN_TRIP_ID + " = ?";
		String[] whereArgs = { String.valueOf(tripId) };
		return getReadableDatabase().query(TABLE_LOCATIONS, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * Gets all the budgeted expenses for a certain location.
	 * 
	 * @param locationId
	 *            Location id of the location to get the budgeted expenses of.
	 * @return A cursor of all the budgeted expenses for a certain location.
	 */
	public Cursor getBudgetedExpenses(int locationId) {
		String whereClause = COLUMN_LOCATION_ID + " = ?";
		String[] whereArgs = { String.valueOf(locationId) };
		return getReadableDatabase().query(TABLE_BUDGETED_EXPENSE, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * Gets all the actual expenses for a certain location.
	 * 
	 * @param locationId
	 *            Location id of the location to get the budgeted expenses of.
	 * @return A cursor of all the budgeted expenses for a certain location.
	 */
	public Cursor getActualExpenses(int locationId) {
		String whereClause = COLUMN_LOCATION_ID + " = ?";
		String[] whereArgs = { String.valueOf(locationId) };
		return getReadableDatabase().query(TABLE_ACTUAL_EXPENSE, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * Updates a trip.
	 * 
	 * @param id
	 *            Id of the trip to be updated.
	 * @param name
	 *            Name of the trip.
	 * @param description
	 *            Description of the trip.
	 * @return The number of rows affected.
	 */
	public int updateTrip(int id, String name, String description) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_NAME, name);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_UPDATE_DATE, getDateTime(new Date()));

		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		int rowsAffected = getWritableDatabase().update(TABLE_TRIPS, cv, whereClause, whereArgs);
		return rowsAffected;
	}

	/**
	 * Updates a location.
	 * 
	 * @param id
	 *            Id of the location to update.
	 * @param name
	 *            Name of the location.
	 * @param description
	 *            Description of the location.
	 * @param city
	 *            City of the location.
	 * @param countryCode
	 *            Country code of the location.
	 * @return The number of rows affected.
	 */
	public int updateLocation(int id, String name, String description, String city, String countryCode) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_NAME, name);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CITY, city);
		cv.put(COLUMN_COUNTRY_CODE, countryCode);

		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		int rowsAffected = getWritableDatabase().update(TABLE_LOCATIONS, cv, whereClause, whereArgs);
		return rowsAffected;
	}

	/**
	 * Updates a new budgeted expense.
	 * 
	 * @param id
	 *            Id of the budgeted expense to update.
	 * @param plannedArrivalDate
	 *            Planned arrival date.
	 * @param plannedDepartureDate
	 *            Planned departure date.
	 * @param amount
	 *            Amount expected to be spent for the budgeted expense.
	 * @param description
	 *            Description of the budgeted expense.
	 * @param category
	 *            Category of the budgeted expense.
	 * @return The number of rows affected.
	 */
	public int updateBudgetedExpense(int id, Date plannedArrivalDate, Date plannedDepartureDate, double amount,
			String description, String category) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_PLANNED_ARRIVAL_DATE, getDateTime(plannedArrivalDate));
		cv.put(COLUMN_PLANNED_DEPARTURE_DATE, getDateTime(plannedDepartureDate));
		cv.put(COLUMN_AMOUNT, amount);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CATEGORY, category);

		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		int rowsAffected = getWritableDatabase().update(TABLE_BUDGETED_EXPENSE, cv, whereClause, whereArgs);
		return rowsAffected;
	}

	/**
	 * Updates a new actual expense.
	 * 
	 * @param id
	 *            Id of the actual expense to update.
	 * @param arrivalDate
	 *            Arrival date.
	 * @param departureDate
	 *            Departure date.
	 * @param amount
	 *            Amount spent for the actual expense.
	 * @param description
	 *            Description of the actual expense.
	 * @param category
	 *            Category of the actual expense.
	 * @param nameOfSupplier
	 *            Name of the supplier of the actual expense.
	 * @param address
	 *            Address of the actual expense.
	 * @return The number of rows affected.
	 */
	public int updateActualExpense(int id, Date arrivalDate, Date departureDate, double amount, String description,
			String category, String nameOfSupplier, String address) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ARRIVAL_DATE, getDateTime(arrivalDate));
		cv.put(COLUMN_DEPARTURE_DATE, getDateTime(departureDate));
		cv.put(COLUMN_AMOUNT, amount);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CATEGORY, category);
		cv.put(COLUMN_NAME_OF_SUPPLIER, nameOfSupplier);
		cv.put(COLUMN_ADDRESS, address);

		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		int rowsAffected = getWritableDatabase().update(TABLE_ACTUAL_EXPENSE, cv, whereClause, whereArgs);
		return rowsAffected;
	}

	/**
	 * Deletes a trip.
	 * 
	 * @param id
	 *            Id of the trip to delete.
	 * @return The number of rows deleted.
	 */
	public int delteTrip(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().delete(TABLE_TRIPS, whereClause, whereArgs);
	}

	/**
	 * Deletes a location.
	 * 
	 * @param id
	 *            Id of the trip to delete.
	 * @return The number of rows deleted.
	 */
	public int deleteLocation(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().delete(TABLE_LOCATIONS, whereClause, whereArgs);
	}

	/**
	 * Deletes a budgeted expense.
	 * 
	 * @param id
	 *            Id of the budgeted expense to delete.
	 * @return The number of rows deleted.
	 */
	public int deleteBudgetedExpense(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().delete(TABLE_BUDGETED_EXPENSE, whereClause, whereArgs);
	}

	/**
	 * Deletes an actual expense.
	 * 
	 * @param id
	 *            Id of the actual expense to delete.
	 * @return The number of rows delete.
	 */
	public int deleteActualExpense(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().delete(TABLE_ACTUAL_EXPENSE, whereClause, whereArgs);
	}

	/**
	 * Takes a Date object and formats it into a string that can be inserted
	 * into an SQLite database.
	 * 
	 * @param date
	 *            The date that you want to format into a string.
	 * @return The formated string.
	 */
	private String getDateTime(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return dateFormat.format(date);
	}
}
