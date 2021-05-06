package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var testReminder : ReminderDTO

    private val testModules = module {
        viewModel { RemindersListViewModel(appContext, get()) }
        single { SaveReminderViewModel(appContext, get()) }
        single { LocalDB.createRemindersDao(appContext) }
        single<ReminderDataSource> { RemindersLocalRepository(get()) }
    }

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()
        appContext = getApplicationContext()
        startKoin {
            androidContext(appContext)
            modules(listOf(testModules))
        }
        repository = get()
        runBlocking {
            repository.deleteAllReminders()
        }
        testReminder = ReminderDTO(
            "Test title",
            "A test description",
            "Test location",
            -74.61,
            -86.54)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun reminderListFragment_noDataLoaded_noDataStatusDisplayed() {
        // GIVEN an application with no saved reminders
        runBlocking {
            repository.deleteAllReminders()
        }
        // WHEN the ReminderList fragment is opened
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        // THEN an indicator is displayed saying that there is no data.
//    TODO: add testing for the error messages [[DONE]]
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun reminderListFragment_clickFAB_navigateToSaveReminderFragment() {
        // GIVEN a navigation controller from the ragment ReminderList
        val scenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        val navController = mock(NavController::class.java)

        // WHEN the FAB addReminder is clicked.
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
//    TODO: test the navigation of the fragments [[DONE]]
        onView(withId(R.id.addReminderFAB)).perform(click())

        //THEN navigate to the SaveReminder fragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun reminderListFragment_thereIsDataLoaded_reminderIsDisplayed() {
        // GIVEN a reminder that is saved
        val reminder = testReminder
        runBlocking {
            repository.saveReminder(reminder)
        }

        // WHEN the fragment opens
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        // THEN the reminder is shown on the screen
//    TODO: test the displayed data on the UI [[DONE]]
        onView(withText(reminder.title)).check(matches(isDisplayed()))
        onView(withText(reminder.location)).check(matches(isDisplayed()))
    }

    @Test
    fun reminderListFragment_deleteAllOptionInRuntime_noDataStatusDisplayed() {
        // GIVEN a reminder that is saved
        val reminder = testReminder
        runBlocking {
            repository.saveReminder(reminder)
        }

        // WHEN the fragment is opened and all inserted data is removed at runtime
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        runBlocking {
            repository.deleteAllReminders()
        }

        // THEN even though the data is refreshed, the no data indicator is displayed.
        onView(withId(R.id.refreshLayout)).perform(swipeDown())
//    TODO: add testing for the error messages [[DONE]]
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }
}