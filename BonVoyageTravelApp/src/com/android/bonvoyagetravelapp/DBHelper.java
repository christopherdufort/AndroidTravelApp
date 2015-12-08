package com.android.bonvoyagetravelapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
	private static final String TABLE_CATEGORIES = "categories";
	private static final String TABLE_BUDGETED_EXPENSES = "budgeted_expenses";
	private static final String TABLE_ACTUAL_EXPENSES = "actual_expenses";

	// Database field names
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TRIP_ID = "trip_id";
	public static final String COLUMN_CREATED_ON = "created_on";
	public static final String COLUMN_UPDATED_ON = "updated_on";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_CITY = "city";
	public static final String COLUMN_COUNTRY_CODE = "country_code";
	public static final String COLUMN_PROVINCE = "province";
	public static final String COLUMN_CATEGORY = "category";
	public static final String COLUMN_LOCATION_ID = "location_id";
	public static final String COLUMN_BUDGETED_ID = "budgeted_id";
	public static final String COLUMN_PLANNED_ARRIVAL_DATE = "planned_arrival_date";
	public static final String COLUMN_PLANNED_DEPARTURE_DATE = "planned_departure_date";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_CATEGORY_ID = "category_id";
	public static final String COLUMN_ARRIVAL_DATE = "arrival_date";
	public static final String COLUMN_DEPARTURE_DATE = "departure_date";
	public static final String COLUMN_NAME_OF_SUPPLIER = "name_of_supplier";
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_STARS = "stars";

	// Database creation raw SQL statement
	// TODO: Choose a data type for the dates
	private static final String CREATE_TRIPS_TABLE = "create table " + TABLE_TRIPS + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TRIP_ID + " integer not null, " + COLUMN_CREATED_ON
			+ " integer not null, " + COLUMN_UPDATED_ON + " integer, " + COLUMN_NAME + " text not null, "
			+ COLUMN_DESCRIPTION + " text);";

	private static final String CREATE_LOCATIONS_TABLE = "create table " + TABLE_LOCATIONS + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_CITY + " text not null, " + COLUMN_COUNTRY_CODE
			+ " text not null, " + COLUMN_PROVINCE + " text);";

	private static final String CREATE_CATEGORIES_TABLE = "create table " + TABLE_CATEGORIES + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_CATEGORY + " text not null);";

	private static final String CREATE_BUDGETED_EXPENSES_TABLE = "create table " + TABLE_BUDGETED_EXPENSES + "( "
			+ COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TRIP_ID + " integer not null, "
			+ COLUMN_LOCATION_ID + " integer not null, " + COLUMN_PLANNED_ARRIVAL_DATE + " integer not null, "
			+ COLUMN_PLANNED_DEPARTURE_DATE + " integer not null, " + COLUMN_AMOUNT + " real not null, "
			+ COLUMN_DESCRIPTION + " text not null, " + COLUMN_CATEGORY_ID + " integer not null, "
			+ COLUMN_NAME_OF_SUPPLIER + " text not null, " + COLUMN_ADDRESS + " text not null, " + "foreign key ("
			+ COLUMN_TRIP_ID + ") references " + TABLE_TRIPS + " (" + COLUMN_ID + ") on delete cascade, "
			+ "foreign key (" + COLUMN_LOCATION_ID + ") references " + TABLE_LOCATIONS + " (" + COLUMN_ID + "), "
			+ "foreign key (" + COLUMN_CATEGORY_ID + ") references " + TABLE_CATEGORIES + " (" + COLUMN_ID + "));";

	private static final String CREATE_ACTUAL_EXPENSES_TABLE = "create table " + TABLE_ACTUAL_EXPENSES + "( "
			+ COLUMN_ID + " integer primary key autoincrement, " + COLUMN_BUDGETED_ID + " integer not null, "
			+ COLUMN_ARRIVAL_DATE + " integer not null, " + COLUMN_DEPARTURE_DATE + " integer not null, "
			+ COLUMN_AMOUNT + " real not null, " + COLUMN_DESCRIPTION + " text not null, " + COLUMN_CATEGORY_ID
			+ " integer not null, " + COLUMN_NAME_OF_SUPPLIER + " text not null, " + COLUMN_ADDRESS + " text not null, "
			+ COLUMN_STARS + " integer not null, " + "foreign key (" + COLUMN_BUDGETED_ID + ") references "
			+ TABLE_BUDGETED_EXPENSES + " (" + COLUMN_ID + ") on delete cascade, " + "foreign key ("
			+ COLUMN_CATEGORY_ID + ") references " + TABLE_CATEGORIES + " (" + COLUMN_ID + "));";

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

	// GregorianCalendar's months start at 0.
	// GregorianCalendar's use 0 to represent noon and midnight and work on a 12
	// hour clock.
	// If calendar.get(Calendar.AM_PM) == Calendar.AM, it is AM.
	// If calendar.get(Calendar.AM_PM) == Calendar.PM, it is PM.
	@SuppressWarnings("deprecation")
	public void seedDB(SQLiteDatabase db) {
		db.execSQL("INSERT INTO " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY + ") VALUES('Accomodation')");
		db.execSQL("INSERT INTO " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY + ") VALUES('Food And Drink')");
		db.execSQL("INSERT INTO " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY + ") VALUES('Gifts')");
		db.execSQL("INSERT INTO " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY + ") VALUES('Transport')");
		db.execSQL("INSERT INTO " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY + ") VALUES('Entertainment')");

		// Trip 1
		db.execSQL("INSERT INTO " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ", " + COLUMN_NAME + ", " + COLUMN_DESCRIPTION
				+ ", " + COLUMN_CREATED_ON + ") VALUES(1, 'Trip 1', 'The first trip', "
				+ getDateTime(new GregorianCalendar()) + ")");

		db.execSQL("INSERT INTO " + TABLE_LOCATIONS + "(" + COLUMN_CITY + ", " + COLUMN_COUNTRY_CODE + ", "
				+ COLUMN_PROVINCE + ") VALUES('Paris', 'FR', 'Ile-de-France')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,1, 1,"
				+ getDateTime(new GregorianCalendar(2015, 11, 23, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 11, 23, 12, 0, 0))
				+ ",750.00, 'Plane trip to destination', 4, 'Air Canada',	'Montreal-Pierre Elliot Trudeau International Airport')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,1, 1, "
				+ getDateTime(new GregorianCalendar(2015, 11, 23, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 11, 26, 120, 0, 0))
				+ ",700.00, '2 star hotel', 1, 'Hotel Supplier',	'Hotels address')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,1, 1, "
				+ getDateTime(new GregorianCalendar(2015, 11, 24, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 11, 26, 12, 0, 0))
				+ ",300.00, 'All meals', 2, 'Food And drink supplier', 'Some restaurant address')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,1, 1, "
				+ getDateTime(new GregorianCalendar(2015, 11, 24, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 11, 26, 12, 0, 0))
				+ ",50.00, 'Gift for mom', 3, 'Gift supplier', 'Shopping here')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,1, 1, "
				+ getDateTime(new GregorianCalendar(2015, 11, 24, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 11, 26, 12, 0, 0))
				+ ",250.00, 'All entertainment', 5, 'Entertainment Supplier', 'Here be a museum')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,1, 1, "
				+ getDateTime(new GregorianCalendar(2015, 11, 23, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 11, 27, 12, 0, 0))
				+ ",100.00, 'All taxis', 4, 'Local Transport Supplier', 'There is no address')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,1, 1, "
				+ getDateTime(new GregorianCalendar(2015, 11, 27, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 11, 27, 12, 0, 0))
				+ ",750.00, 'Plane trip back home', 4, 'Air Canada', 'Montreal-Pierre Elliot Trudeau International Airport')");

		db.execSQL("INSERT INTO " + TABLE_ACTUAL_EXPENSES + " VALUES(null,1, "
				+ getDateTime(new GregorianCalendar(2015, 11, 23, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 11, 23, 12, 0, 0))
				+ ",750.00, 'Plane trip to destination', 4, 'Air Canada', 'Montreal-Pierre Elliot Trudeau International Airport', 5)");

		// Trip 2

		db.execSQL("INSERT INTO " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ", " + COLUMN_NAME + ", " + COLUMN_DESCRIPTION
				+ ", " + COLUMN_CREATED_ON + ") VALUES(2, 'Trip 2', 'The second trip', "
				+ getDateTime(new GregorianCalendar()) + ")");

		db.execSQL("INSERT INTO " + TABLE_LOCATIONS + "(" + COLUMN_CITY + ", " + COLUMN_COUNTRY_CODE + ", "
				+ COLUMN_PROVINCE + ") VALUES('Atlanta', 'US', 'Georgia')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,2, 2,"
				+ getDateTime(new GregorianCalendar()) + " , " + getDateTime(new GregorianCalendar())
				+ ",150.00, 'Todays buss trip!', 4, 'Delta', 'Grey Hound Buss')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,2, 2,"
				+ getDateTime(new GregorianCalendar(2015, 12, 24, 12, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2015, 12, 24, 12, 0, 0))
				+ ",300.00, 'One way plane trip', 4, 'Delta', 'Mirabel Airport')");

		db.execSQL("INSERT INTO " + TABLE_BUDGETED_EXPENSES + " VALUES(null,2, 2, "
				+ getDateTime(new GregorianCalendar(2015, 12, 24, 15, 0, 0)) + " , "
				+ getDateTime(new GregorianCalendar(2995, 12, 26, 120, 0, 0))
				+ ",1000.00, '5 star hotel', 1, 'Hilton',	'123 main street')");

	}

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
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETED_EXPENSES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTUAL_EXPENSES);
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
			database.execSQL(CREATE_CATEGORIES_TABLE);
			database.execSQL(CREATE_BUDGETED_EXPENSES_TABLE);
			database.execSQL(CREATE_ACTUAL_EXPENSES_TABLE);

			seedDB(database);

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
		cv.put(COLUMN_CREATED_ON, getDateTime(new GregorianCalendar()));

		long code = getWritableDatabase().insert(TABLE_TRIPS, null, cv);
		return code;
	}

	/**
	 * Creates a new location. Only used when pulling down information from the
	 * PHP server. Cannot create new locations within the app.
	 * 
	 * @param city
	 *            City of the location.
	 * @param countryCode
	 *            Country code of the location.
	 * @param province
	 *            Province of the location.
	 * @return Id of the newly inserted row or -1 if there was an error.
	 */
	public long createLocation(String city, String countryCode, String province) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_CITY, city);
		cv.put(COLUMN_COUNTRY_CODE, countryCode);
		cv.put(COLUMN_PROVINCE, province);

		long code = getWritableDatabase().insert(TABLE_LOCATIONS, null, cv);
		return code;
	}

	/**
	 * Creates a new category. Only used when pulling down information from the
	 * PHP server. Cannot create new categories within the app.
	 * 
	 * @param category
	 *            Name of the new category
	 * @return Id of the newly inserted row or -1 if there was an error.
	 */
	public long createCategory(String category) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_CATEGORY, category);

		long code = getWritableDatabase().insert(TABLE_CATEGORIES, null, cv);
		return code;
	}

	/**
	 * Creates a new budgeted expense.
	 * 
	 * @param tripId
	 *            Id of the trip this budgeted expense belongs to.
	 * @param locationId
	 *            Id of the location this budgeted expense takes place in.
	 * @param plannedArrivalDate
	 *            Planned arrival date.
	 * @param plannedDepartureDate
	 *            Planned departure date.
	 * @param amount
	 *            Amount expected to be spent for the budgeted expense.
	 * @param description
	 *            Description of the budgeted expense.
	 * @param categoryId
	 *            Id of the category of the budgeted expense.
	 * @param nameOfSupplier
	 *            Name of the supplier of the budgeted expense.
	 * @param address
	 *            Address of the budgeted expense.
	 * @return Id of the newly inserted row or -1 if there was an error.
	 */
	public long createBudgetedExpense(int tripId, int locationId, GregorianCalendar plannedArrivalDate, GregorianCalendar plannedDepartureDate,
			double amount, String description, int categoryId, String nameOfSupplier, String address) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_TRIP_ID, tripId);
		cv.put(COLUMN_LOCATION_ID, locationId);
		cv.put(COLUMN_PLANNED_ARRIVAL_DATE, getDateTime(plannedArrivalDate));
		cv.put(COLUMN_PLANNED_DEPARTURE_DATE, getDateTime(plannedDepartureDate));
		cv.put(COLUMN_AMOUNT, amount);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CATEGORY_ID, categoryId);
		cv.put(COLUMN_NAME_OF_SUPPLIER, nameOfSupplier);
		cv.put(COLUMN_ADDRESS, address);

		long code = getWritableDatabase().insert(TABLE_BUDGETED_EXPENSES, null, cv);
		return code;
	}

	/**
	 * Creates a new actual expense.
	 * 
	 * @param budgetedId
	 *            Id of the budgeted expense the actual expense belongs to.
	 * @param arrivalDate
	 *            Arrival date.
	 * @param departureDate
	 *            departure date.
	 * @param amount
	 *            Amount spent for the actual expense.
	 * @param description
	 *            Description of the actual expense.
	 * @param categoryId
	 *            Id of the category of the actual expense.
	 * @param nameOfSupplier
	 *            Name of the supplier of the actual expense.
	 * @param address
	 *            Address of the actual expense.
	 * @param stars
	 *            Rating of the supplier of the actual expense.
	 * @return Id of the newly inserted row or -1 if there was an error.
	 */
	public long createActualExpense(int budgetedId, GregorianCalendar arrivalDate, GregorianCalendar departureDate, double amount,
			String description, int categoryId, String nameOfSupplier, String address, int stars) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_BUDGETED_ID, budgetedId);
		cv.put(COLUMN_ARRIVAL_DATE, getDateTime(arrivalDate));
		cv.put(COLUMN_DEPARTURE_DATE, getDateTime(departureDate));
		cv.put(COLUMN_AMOUNT, amount);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CATEGORY_ID, categoryId);
		cv.put(COLUMN_NAME_OF_SUPPLIER, nameOfSupplier);
		cv.put(COLUMN_ADDRESS, address);
		cv.put(COLUMN_STARS, stars);

		long code = getWritableDatabase().insert(TABLE_ACTUAL_EXPENSES, null, cv);
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
	 * Gets all the locations.
	 * 
	 * @return A cursor of all the locations.
	 */
	public Cursor getLocations() {
		Log.d("debug", "in get locations!!");
		return getReadableDatabase().query(TABLE_LOCATIONS, null, null, null, null, null, null);
	}

	/**
	 * Gets the location with the specified id.
	 * 
	 * @param id
	 *            Id of the location to get.
	 * @return A cursor of the specific location.
	 */
	public Cursor getLocationById(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().query(TABLE_LOCATIONS, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * Gets all the categories.
	 * 
	 * @return A Cursor of all the categories.
	 */
	public Cursor getCategories() {
		return getReadableDatabase().query(TABLE_CATEGORIES, null, null, null, null, null, null, null);
	}

	/**
	 * Gets the category with the specified id.
	 * 
	 * @param id
	 *            Id of the category to get.
	 * @return A cursor of the specific category.
	 */
	public Cursor getCategoryById(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().query(TABLE_CATEGORIES, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * Gets all the budgeted expenses for a certain location.
	 * 
	 * @param tripId
	 *            Id of the trip to get the budgeted expenses of.
	 * @return A cursor of all the budgeted expenses for a certain trip.
	 */
	public Cursor getBudgetedExpenses(int tripId) {
		String whereClause = COLUMN_TRIP_ID + " = ?";
		String[] whereArgs = { String.valueOf(tripId) };
		return getReadableDatabase().query(TABLE_BUDGETED_EXPENSES, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * Gets all the budgeted expenses for a specified day.
	 * 
	 * @since 2015-12-06
	 * @param date
	 *            day of the itinerary to get the budgeted expenses of.
	 * @return A cursor of all the budgeted expenses for a certain day.
	 */
	public Cursor getBudgetedExpenses(GregorianCalendar date) {
		String whereClause = COLUMN_PLANNED_ARRIVAL_DATE + " BETWEEN ? AND ?";
		GregorianCalendar startDate = new GregorianCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
				date.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		GregorianCalendar endDate = new GregorianCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
				date.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
		String[] whereArgs = { String.valueOf(getDateTime(startDate)), String.valueOf(getDateTime(endDate)) };
		return getReadableDatabase().query(TABLE_BUDGETED_EXPENSES, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * 
	 * Gets all the budgeted expenses fields for a given budgeted id. Used for
	 * displaying details.
	 * 
	 * @since 2015-12-06
	 * @param budgetedId
	 *            Id of the budgeted expense for which to retrieve all rows.
	 * @return A cursor of all the budgeted fields for a certain id.
	 */
	public Cursor getBudgetedDetails(int budgetedId) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(budgetedId) };
		return getReadableDatabase().query(TABLE_BUDGETED_EXPENSES, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * 
	 * Gets all the actual expenses fields for a given actual id. Used for
	 * displaying details.
	 * 
	 * @since 2015-12-06
	 * @param actualId
	 *            Id of the actual expense for which to retrieve all rows.
	 * @return A cursor of all the actual fields for a certain id.
	 */
	public Cursor getActualDetails(int actualId) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(actualId) };
		return getReadableDatabase().query(TABLE_ACTUAL_EXPENSES, null, whereClause, whereArgs, null, null, null);
	}

	/**
	 * Gets all actual expenses for a certain budgeted expense.
	 * 
	 * @param budgetedId
	 *            Id of the budgeted expense to get the actual expense of.
	 * @return A cursor of the actual expenses for a certain budgeted expense.
	 */
	public Cursor getActualExpenses(int budgetedId) {
		String whereClause = COLUMN_BUDGETED_ID + " = ?";
		String[] whereArgs = { String.valueOf(budgetedId) };
		return getReadableDatabase().query(TABLE_BUDGETED_EXPENSES, null, whereClause, whereArgs, null, null, null);
	}

	public Cursor getLocationIdWithTripId(String tripId) {
		String whereClause = COLUMN_TRIP_ID + " = ?";
		String[] whereArgs = { String.valueOf(tripId) };
		String[] columns = { COLUMN_LOCATION_ID };

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_BUDGETED_EXPENSES, columns, whereClause, whereArgs, null, null, null);
		return cursor;
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
	 * @return The number of rows updated.
	 */
	public int updateTrip(int id, String name, String description) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_NAME, name);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_UPDATED_ON, getDateTime(new GregorianCalendar()));

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
	 * @param city
	 *            City of the location.
	 * @param countryCode
	 *            Country code of the location.
	 * @param province
	 *            Province of the location
	 * @return The number of rows updated.
	 */
	public int updateLocation(int id, String city, String countryCode, String province) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_CITY, city);
		cv.put(COLUMN_COUNTRY_CODE, countryCode);
		cv.put(COLUMN_PROVINCE, province);

		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		int rowsAffected = getWritableDatabase().update(TABLE_LOCATIONS, cv, whereClause, whereArgs);
		return rowsAffected;
	}

	/**
	 * Updates a budgeted expense.
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
	 * @param categoryId
	 *            Id of the category of the budgeted expense.
	 * @param nameOfSupplier
	 *            Name of the supplier of the budgeted expense.
	 * @param address
	 *            Address of the budgeted expense.
	 * @return The number of rows updated.
	 */
	public int updateBudgetedExpense(int id, GregorianCalendar plannedArrivalDate, GregorianCalendar plannedDepartureDate, double amount,
			String description, int categoryId, String nameOfSupplier, String address) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_PLANNED_ARRIVAL_DATE, getDateTime(plannedArrivalDate));
		cv.put(COLUMN_PLANNED_DEPARTURE_DATE, getDateTime(plannedDepartureDate));
		cv.put(COLUMN_AMOUNT, amount);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CATEGORY_ID, categoryId);
		cv.put(COLUMN_NAME_OF_SUPPLIER, nameOfSupplier);
		cv.put(COLUMN_ADDRESS, address);

		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		int rowsAffected = getWritableDatabase().update(TABLE_BUDGETED_EXPENSES, cv, whereClause, whereArgs);
		return rowsAffected;
	}

	/**
	 * Updates an actual expense.
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
	 * @param categoryId
	 *            Id of the category of the actual expense.
	 * @param nameOfSupplier
	 *            Name of the supplier of the actual expense.
	 * @param address
	 *            Address of the actual expense.
	 * @param stars
	 *            Rating of the supplier of the actual expense.
	 * @return The number of rows updated.
	 */
	public int updateActualExpense(int id, GregorianCalendar arrivalDate, GregorianCalendar departureDate, double amount, String description,
			int categoryId, String nameOfSupplier, String address, int stars) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ARRIVAL_DATE, getDateTime(arrivalDate));
		cv.put(COLUMN_DEPARTURE_DATE, getDateTime(departureDate));
		cv.put(COLUMN_AMOUNT, amount);
		cv.put(COLUMN_DESCRIPTION, description);
		cv.put(COLUMN_CATEGORY_ID, categoryId);
		cv.put(COLUMN_NAME_OF_SUPPLIER, nameOfSupplier);
		cv.put(COLUMN_ADDRESS, address);
		cv.put(COLUMN_STARS, stars);

		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		int rowsAffected = getWritableDatabase().update(TABLE_ACTUAL_EXPENSES, cv, whereClause, whereArgs);
		return rowsAffected;
	}

	/**
	 * Deletes a trip.
	 * 
	 * @param id
	 *            Id of the trip to delete.
	 * @return The number of rows deleted.
	 */
	public int deleteTrip(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().delete(TABLE_TRIPS, whereClause, whereArgs);
	}

	/**
	 * Deletes a location.
	 * 
	 * @param id
	 *            Id of the location to delete.
	 * @return The number of rows deleted.
	 */
	public int deleteLocation(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().delete(TABLE_LOCATIONS, whereClause, whereArgs);
	}

	/**
	 * Deletes a category.
	 * 
	 * @param id
	 *            Id of the category to delete.
	 * @return The number of rows deleted.
	 */
	public int deleteCategory(int id) {
		String whereClause = COLUMN_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };
		return getReadableDatabase().delete(TABLE_CATEGORIES, whereClause, whereArgs);
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
		return getReadableDatabase().delete(TABLE_BUDGETED_EXPENSES, whereClause, whereArgs);
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
		return getReadableDatabase().delete(TABLE_ACTUAL_EXPENSES, whereClause, whereArgs);
	}

	/**
	 * Takes a Date object and formats it into a string that can be inserted
	 * into an SQLite database.
	 * 
	 * @param date
	 *            The date that you want to format into a unix time.
	 * @return The unix time.
	 */
	private long getDateTime(GregorianCalendar date) {
		return date.getTimeInMillis() / 1000;
	}
}