package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.MatcherAssert
import org.robolectric.annotation.Config

import com.udacity.project4.locationreminders.data.dto.Result

@Config(sdk = [Build.VERSION_CODES.O])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects [[DONE]]

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        val remindersToAdd = mutableListOf<ReminderDTO>(
            ReminderDTO(
                "Test title",
                "A test description",
                "Test location",
                -74.61,
                -86.54),
            ReminderDTO(
                "Other test title",
                "Another test description",
                "Test same location",
                -74.61,
                -86.54)
        )
        fakeDataSource = FakeDataSource(remindersToAdd)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun remindersListViewModel_loadReminder_loadingStatus() {
        // GIVEN a viewModel with data
        mainCoroutineRule.pauseDispatcher()

        // WHEN they are retrieving
        remindersListViewModel.loadReminders()

        // THEN the charging status is displayed.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // When they are retrieved THEN the charging status is no longer displayed.
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun remindersListViewModel_loadExistentReminders_sameDataAsTheDataSource() = mainCoroutineRule.runBlockingTest {
        // GIVEN a view model without data

        // WHEN attempting to recover data both view model and data source
        remindersListViewModel.loadReminders()
        val reminderInViewModel = remindersListViewModel.remindersList.getOrAwaitValue()
        val reminderInDataSource = (fakeDataSource.getReminders() as Result.Success).data

        // THEN the data is the same on both sides
        assertThat(reminderInViewModel[0].id, `is`(reminderInDataSource[0].id))
        assertThat(reminderInViewModel[0].title, `is`(reminderInDataSource[0].title))
        assertThat(reminderInViewModel[0].description, `is`(reminderInDataSource[0].description))
        assertThat(reminderInViewModel[0].location, `is`(reminderInDataSource[0].location))
        assertThat(reminderInViewModel[0].longitude, `is`(reminderInDataSource[0].longitude))
        assertThat(reminderInViewModel[0].latitude, `is`(reminderInDataSource[0].latitude))
    }

    @Test
    fun remindersListViewModel_loadNonexistentReminders_showError() = runBlockingTest {
        // GIVEN a view model without data
        fakeDataSource.deleteAllReminders()
        // WHEN attempting to recover data
        remindersListViewModel.loadReminders()
        // THEN it is shown that there is no data
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

}