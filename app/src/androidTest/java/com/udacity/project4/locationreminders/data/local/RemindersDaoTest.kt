package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //    TODO: Add testing implementation to the RemindersDao.kt [[DONE]]
    private lateinit var roomDatabase: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        roomDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun disconnectDB() = roomDatabase.close()

    @Test
    fun remindersDao_insertReminderAndFindById_obtainData() = runBlockingTest {
        // GIVEN an insertion of a reminder
        val reminder = ReminderDTO(
            "Test title",
            "A test description",
            "Test location",
            -74.61,
            -86.54)
        roomDatabase.reminderDao().saveReminder(reminder)

        // THEN the retrieved data is the same as the inserted data
        val result = roomDatabase.reminderDao().getReminderById(reminder.id) as ReminderDTO

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(result, notNullValue())
        assertThat(result.id, `is`(reminder.id))
        assertThat(result.title, `is`(reminder.title))
        assertThat(result.description, `is`(reminder.description))
        assertThat(result.location, `is`(reminder.location))
        assertThat(result.latitude, `is`(reminder.latitude))
        assertThat(result.longitude, `is`(reminder.longitude))
    }

    @Test
    fun remindersDao_deleteAllData_databaseIsEmpty() = runBlockingTest {
        // GIVEN an insertion of a reminder
        val reminder = ReminderDTO(
            "Other test title",
            "Another test description",
            "Test same location",
            -74.61,
            -86.54)
        roomDatabase.reminderDao().saveReminder(reminder)

        // WHEN all data are deleted
        roomDatabase.reminderDao().deleteAllReminders()

        // THEN the results are an empty list
        val result = roomDatabase.reminderDao().getReminders()
        assertThat(result, `is`(emptyList()))
    }

    @Test
    fun remindersDao_queryNonexistentReminder_resultIsNull() = runBlockingTest {
        // GIVEN a database without the required data,
        // WHEN a non-existent id is queried
        val result = roomDatabase.reminderDao().getReminderById("911")
        // THEN the results is a null value
        assertThat(result, `is`(nullValue()))
    }
}