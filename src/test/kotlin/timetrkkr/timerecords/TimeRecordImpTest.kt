package timetrkkr.timerecords

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.server.ResponseStatusException
import timetrkkr.ApplicationTests
import timetrkkr.repositories.TimeRecordRepository
import timetrkkr.repositories.UserRepository
import timetrkkr.servicesImpl.TimeRecordServiceImpl
import timetrkkr.servicesImpl.UserServiceImpl
import timetrkkr.utils.EntityGenerator
import timetrkkr.utils.SearchCriteria
import timetrkkr.utils.TestConstants
import timetrkkr.utils.TimeRecordFilter
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TimeRecordImpTest: ApplicationTests() {
    @Autowired
    private lateinit var userServiceImpl: UserServiceImpl

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var timeRecordRepository: TimeRecordRepository

    @Autowired
    private lateinit var timeRecordServiceImpl: TimeRecordServiceImpl


    @BeforeEach
    fun cleanUp(){
        timeRecordRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `will create and return time record given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now(),
            user = user
        ) )
        assertEquals(1, timeRecordRepository.findAll().count())
        assertEquals(user.id , timeRecord.user!!.id)
    }

    @Test
    fun `will return error given if user already login within the day`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy( user = user )
        timeRecordRepository.save(timeRecord)
        val body = timeRecord.copy()

        val expectedMessage = "409 CONFLICT \"User have already login!\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.createTimeRecord(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return error when creating time record given invalid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now().minusDays(1)
        )
        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Login date should be current date\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.createTimeRecord(user.id ,timeRecord)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return time records list by user when given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = SearchCriteria(
            timeRecordIds = listOf(timeRecord.id)
        )

        println(user.timeRecord.size)

        assertEquals(1, timeRecordRepository.getAllUserTimeRecords(user.id, body.timeRecordIds).size)
    }

    @Test
    fun `will return error if time record don't belong to a user`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = SearchCriteria(
            timeRecordIds = listOf(TestConstants.invalidId)
        )

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"The user does not have particular time record Ids: ${body.timeRecordIds}\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.getAllTimeRecordByUser(user.id ,body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will update and logout user given valid logout time`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = timeRecord.copy(
            timeOut = LocalTime.of(23, 59)
        )

        val record = timeRecordServiceImpl.logOutUser(user.id , body)

        assertEquals(record.timeOut , body.timeOut)
    }

    @Test
    fun `will return error on update or logout user given invalid login date`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = timeRecord.copy(
            dateLogin = LocalDate.now().minusDays(1)
        )
        val expectedMessage = "404 NOT_FOUND \"TimeLog not found given ${body.dateLogin}\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.logOutUser(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return error on update or logout user given invalid logout Time`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = timeRecord.copy(
            timeOut = LocalTime.now().minusHours(4)
        )
        val expectedMessage = "409 CONFLICT \"Invalid logout time\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.logOutUser(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will delete user time records given valid TimeRecords id or ids`(){
        val user = userRepository.save( EntityGenerator.createUser() )


        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        assertEquals(1 , timeRecordRepository.findAll().count() )

        val body = SearchCriteria(
            timeRecordIds = listOf(timeRecord.id)
        )

        timeRecordServiceImpl.deleteTimeLogByUser(user.id, body)

        assertEquals(0 , timeRecordRepository.findAll().count() )
    }

    @Test
    fun `will return error deleting user time records given invalid TimeRecords id or ids`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = SearchCriteria(
            timeRecordIds = listOf(TestConstants.invalidId)
        )

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"The user does not have particular time record Ids: ${body.timeRecordIds}\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.deleteTimeLogByUser(user.id, body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return all user Time Records under time in a month given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(1),user = user)

        val record2 = timeRecord.copy(
            dateLogin = LocalDate.now().plusDays(1),
            timeOut = LocalTime.now().plusHours(1)
        )

        timeRecordRepository.saveAll(mutableListOf(timeRecord, record2))

        assertEquals(2, timeRecordRepository.findAll().count())
    }

    @Test
    fun `will return error getting total monthly under time given invalid monthNumber`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(1),user = user)

        val record2 = timeRecord.copy(
            dateLogin = LocalDate.now().plusDays(1),
            timeOut = LocalTime.now().plusHours(1)
        )

        val body = SearchCriteria(
            monthNumber = 23,
            year = 2022
        )

        timeRecordRepository.saveAll(mutableListOf(timeRecord,record2))

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Invalid Month number ${body.monthNumber}\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.getAllMonthlyRecordsByUserUnderTime(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return error getting total monthly under time given invalid year`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(1),user = user)

        val record2 = timeRecord.copy(
            dateLogin = LocalDate.now().plusDays(1),
            timeOut = LocalTime.now().plusHours(1)
        )

        val body = SearchCriteria(
            monthNumber = 8,
            year = 1234
        )

        timeRecordRepository.saveAll(mutableListOf(timeRecord,record2))

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Enter year from 1990 to present: Invalid: ${body.year}\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.getAllMonthlyRecordsByUserUnderTime(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return computed user time records given valid date range`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(1),user = user)

        val record2 = timeRecord.copy(
            dateLogin = LocalDate.now().plusDays(1),
            timeOut = LocalTime.now().plusHours(1)
        )

        timeRecordRepository.saveAll(mutableListOf(timeRecord,record2))
        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            dateUntil = LocalDate.now().plusDays(1)
        )
        val computedTimeRecords = timeRecordServiceImpl.computeUserRenderedTime(user.id , body)

        assertEquals(2,
            timeRecordRepository.getUserTimeRecordByDateRange(user.id, body.dateFrom, body.dateUntil).size)
    }

    @Test
    fun `will return error when computing user time records given invalid date range`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(1),user = user)

        val record2 = timeRecord.copy(
            dateLogin = LocalDate.now().plusDays(1),
            timeOut = LocalTime.now().plusHours(1)
        )

        timeRecordRepository.saveAll(mutableListOf(timeRecord,record2))
        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            dateUntil = LocalDate.now().minusDays(5)
        )
        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Invalid given date range\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.computeUserRenderedTime(user.id, body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return list of all time records in a day paginated`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(1),user = user)


        timeRecordRepository.saveAll(mutableListOf(timeRecord))

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            page = 0
        )

        assertEquals(1, timeRecordRepository.getAllPaginatedTimeRecords(body.size, body.page, body.dateFrom).size)

//        assertEquals(2, userRepository.findAll().count())
//        assertEquals(2, timeRecordRepository.findAll().count())

    }

    @Test
    fun `will return error getting list of all time records in a day given invalid page and size value`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val user2 = userRepository.save(EntityGenerator.createUser().copy(
            firstName = "Lina" ,
            lastName = "Inverse",
            email = "test@mailing.com"
        ))

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(1),user = user)

        val timeRecord2 = timeRecord.copy(
            dateLogin = LocalDate.now().plusDays(1),
            timeOut = LocalTime.now().plusHours(1),
            user = user2
        )

        timeRecordRepository.saveAll(mutableListOf(timeRecord, timeRecord2))

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            size = -1,
            page = -1
        )
        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"page: ${body.page} should not be negative and size: ${body.size} is greater than zero\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            timeRecordServiceImpl.getAllTimeRecords(body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

}