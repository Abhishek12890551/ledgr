package app.ledgr.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ledgr.data.db.LedgrDatabase
import app.ledgr.engine.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExportImportTest {

    private lateinit var db1: LedgrDatabase
    private lateinit var db2: LedgrDatabase

    @Before
    fun setup() {
        db1 = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LedgrDatabase::class.java
        ).allowMainThreadQueries().build()

        db2 = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LedgrDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        db1.close()
        db2.close()
    }

    @Test
    fun testExportImportReproducesIdenticalBalances() = runTest {
        val repo1 = EventRepository(db1.eventDao())
        val exportRepo1 = ExportRepository(db1, repo1)

        // Create an event
        val event = Expense(
            id = EventId("e1"),
            scope = Scope.Personal,
            occurredDate = "2026-10-07",
            occurredAtMillis = 1000L,
            recordedAtMillis = 1001L,
            enteredBy = PersonId("me"),
            currency = "INR",
            supersedes = null,
            note = null,
            total = Money(100_00L),
            description = "Test expense",
            categoryId = null,
            accountId = null,
            paid = mapOf(PersonId("me") to Money(100_00L)),
            split = SplitSpec.Equal(listOf(PersonId("me"))),
            shares = mapOf(PersonId("me") to Money(100_00L)),
            refundOf = null,
            emoji = null
        )
        
        repo1.processTransaction(listOf(event))

        // Export from db1
        val exportFile = exportRepo1.export()

        // Import to db2
        val repo2 = EventRepository(db2.eventDao())
        val exportRepo2 = ExportRepository(db2, repo2)
        
        exportRepo2.import(exportFile)

        // Compare effective events
        val events1 = repo1.getEffectiveEventsSync()
        val events2 = repo2.getEffectiveEventsSync()

        assertEquals(events1.size, events2.size)
        assertEquals(events1[0], events2[0])
    }
}
