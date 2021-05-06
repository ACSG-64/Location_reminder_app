package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var testReminder : ReminderDataItem

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(getApplicationContext(), fakeDataSource)
        testReminder = ReminderDataItem(
                "Test title",
                "A test description",
                "Test location",
                -74.61,
                -86.54)
    }

    @Test
    fun saveReminderViewModel_clearViewModel_isNull() {
        // GIVEN a ReminderDataItem to be stored
        val reminder = testReminder
        saveReminderViewModel.validateAndSaveReminder(reminder)

        // WHEN the view model is cleared
        saveReminderViewModel.onClear()

        // THEN reminderTitle must be null along with the rest of the variables.
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), nullValue())
    }

    @Test
    fun saveReminderViewModel_saveAReminder_equalToDataSource() = mainCoroutineRule.runBlockingTest {
        // GIVEN a ReminderDataItem that is saved
        val reminder = testReminder
        saveReminderViewModel.validateAndSaveReminder(reminder)

        // WHEN searching the data source
        val result = (fakeDataSource.getReminders() as Result.Success).data

        // THEN the result is the same as the ReminderDataItem that was saved
        assertThat(reminder.id, `is`(result[0].id))
        assertThat(reminder.title, `is`(result[0].title))
        assertThat(reminder.description, Is.`is`(result[0].description))
        assertThat(reminder.location, `is`(result[0].location))
        assertThat(reminder.longitude, `is`(result[0].longitude))
        assertThat(reminder.latitude, `is`(result[0].latitude))
    }

    @Test
    fun saveReminderViewModel_whileSavingAReminder_isLoading() = mainCoroutineRule.runBlockingTest {
        // GIVEN a ReminderDataItem
        val reminder = testReminder

        // WHEN the ReminderDataItem is saved
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)

        // THEN while saving, the loading status is displayed.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // When it is already saved THEN the loading status is not displayed.
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun saveReminderViewModel_savingSuccess_toastMessage() = mainCoroutineRule.runBlockingTest{
        // GIVEN a ReminderDataItem
        val reminder = testReminder

        // WHEN the ReminderDataItem is saved
        saveReminderViewModel.saveReminder(reminder)

        // When it is saved THEN a toast is displayed with the message confirming that it was saved.
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(getApplicationContext<Context>().getString(R.string.reminder_saved)))
    }

    @Test
    fun saveReminderViewModel_saveInvalidReminder_snackBarTrigger() = mainCoroutineRule.runBlockingTest{
        // GIVEN an invalid ReminderDataItem (without title)
        val incorrectReminder = ReminderDataItem(
                "",
                "A test description",
                "Test location",
                -74.61,
                -86.54)

        // WHEN trying to save the ReminderDataItem
        saveReminderViewModel.validateAndSaveReminder(incorrectReminder)

        // THEN A snackBar is then displayed
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

}