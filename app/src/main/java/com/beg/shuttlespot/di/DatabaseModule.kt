package com.beg.shuttlespot.di

// Overview: Defines database module for the shuttlespot di area of ShuttleSpot.

// Dependencies used by this file.
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import com.beg.shuttlespot.data.local.ShuttleSpotDatabase
import dagger.Module
import dagger.Provides
// Dependencies used by this file.
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
// Defines the database module type.
object DatabaseModule {
    @Provides @Singleton
    // Prepares or returns provide database.
    fun provideDatabase(@ApplicationContext context: Context): ShuttleSpotDatabase =
        // Builds the local Room database used for offline persistence.
        Room.databaseBuilder(context, ShuttleSpotDatabase::class.java, "shuttlespot.db")
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .build()

    @Provides fun provideDao(database: ShuttleSpotDatabase): ShuttleSpotDao = database.shuttleSpotDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        // Implements framework behavior for migrate.
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS cached_sessions (profileId TEXT NOT NULL, role TEXT NOT NULL, displayName TEXT NOT NULL, contact TEXT NOT NULL, updatedAtEpochMillis INTEGER NOT NULL, PRIMARY KEY(profileId))")
            database.execSQL("CREATE TABLE IF NOT EXISTS cached_notifications (id TEXT NOT NULL, profileId TEXT NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, target TEXT NOT NULL, createdAtEpochMillis INTEGER NOT NULL, isRead INTEGER NOT NULL, PRIMARY KEY(id))")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        // Implements framework behavior for migrate.
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE cached_routes ADD COLUMN serviceNotice TEXT")
            database.execSQL("ALTER TABLE cached_routes ADD COLUMN updatedAtEpochMillis INTEGER NOT NULL DEFAULT 0")
            database.execSQL("CREATE TABLE IF NOT EXISTS cached_stops_new (id TEXT NOT NULL, routeId TEXT NOT NULL, name TEXT NOT NULL, sequence INTEGER NOT NULL, latitude REAL, longitude REAL, scheduledTime TEXT, PRIMARY KEY(id, routeId))")
            database.execSQL("INSERT INTO cached_stops_new (id, routeId, name, sequence, latitude, longitude) SELECT id, routeId, name, sequence, latitude, longitude FROM cached_stops")
            database.execSQL("DROP TABLE cached_stops")
            database.execSQL("ALTER TABLE cached_stops_new RENAME TO cached_stops")
            database.execSQL("CREATE TABLE IF NOT EXISTS cached_route_points (routeId TEXT NOT NULL, sequence INTEGER NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, PRIMARY KEY(routeId, sequence))")
            database.execSQL("CREATE TABLE IF NOT EXISTS cached_timetable_entries (routeId TEXT NOT NULL, sequence INTEGER NOT NULL, period TEXT NOT NULL, time TEXT NOT NULL, description TEXT NOT NULL, PRIMARY KEY(routeId, sequence))")
            database.execSQL("CREATE TABLE IF NOT EXISTS trip_snapshots_new (tripId TEXT NOT NULL, routeId TEXT NOT NULL, vehicleLabel TEXT NOT NULL DEFAULT '', status TEXT NOT NULL, progress REAL NOT NULL DEFAULT 0, previousStopName TEXT, nextStopName TEXT, etaMinutes INTEGER, latitude REAL, longitude REAL, capturedAtEpochMillis INTEGER, updatedAtEpochMillis INTEGER NOT NULL, PRIMARY KEY(tripId))")
            database.execSQL("INSERT INTO trip_snapshots_new (tripId, routeId, status, updatedAtEpochMillis) SELECT tripId, routeId, status, updatedAtEpochMillis FROM trip_snapshots")
            database.execSQL("DROP TABLE trip_snapshots")
            database.execSQL("ALTER TABLE trip_snapshots_new RENAME TO trip_snapshots")
            database.execSQL("CREATE TABLE IF NOT EXISTS cached_boarding_intents (profileId TEXT NOT NULL, tripId TEXT NOT NULL, stopId TEXT NOT NULL, status TEXT NOT NULL, idempotencyKey TEXT NOT NULL, updatedAtEpochMillis INTEGER NOT NULL, PRIMARY KEY(profileId, tripId, stopId))")
        }
    }
}
