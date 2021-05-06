package com.udacity.project4.locationreminders.data.local

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    //    TODO: Add testing implementation to the RemindersLocalRepository.kt [[DONE]]
    private lateinit var roomDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        roomDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(roomDatabase.reminderDao(), Dispatchers.Main)
    }

    @After
    fun disconnectDB() = roomDatabase.close()

    @Test
    fun remindersLocalRepository_savingReminder_retrievesInsertedReminder() = runBlocking {
        // GIVEN a reminder to be saved
        val reminder = ReminderDTO(
            "Test title",
            "A test description",
            "Test location",
            -74.61,
            -86.54)
        remindersLocalRepository.saveReminder(reminder)

        // WHEN the database is searched for the id of the saved reminder
        val result = (remindersLocalRepository.getReminder(reminder.id) as Result.Success)

        // THEN the retrieved data is the same as the inserted data
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
        assertThat(result.data.location, `is`(reminder.location))
    }

    @Test
    fun remindersLocalRepository_deleteAllReminders_resultMessageIsNotFound() = runBlocking {
        // GIVEN a reminder to be saved
        val reminder = ReminderDTO(
            "Other test title",
            "Another test description",
            "Test same location",
            -74.61,
            -86.54)
        remindersLocalRepository.saveReminder(reminder)

        // WHEN all records are deleted from the database
        remindersLocalRepository.deleteAllReminders()

        // THEN the result message mentions that it was not found
        val result = (remindersLocalRepository.getReminder(reminder.id) as Result.Error)
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun remindersLocalRepository_queryNonexistentReminder_databaseIsEmpty() = runBlocking {
        // GIVEN a reminder to be saved
        val reminder = ReminderDTO(
            "Test title",
            "A test description",
            "Test location",
            -74.61,
            -86.54)
        remindersLocalRepository.saveReminder(reminder)

        // WHEN all records are deleted from the database
        remindersLocalRepository.deleteAllReminders()

        // THEN the data base is empty
        val result = (remindersLocalRepository.getReminders() as Result.Success).data
        assertThat(result, `is`(emptyList()))
    }
}