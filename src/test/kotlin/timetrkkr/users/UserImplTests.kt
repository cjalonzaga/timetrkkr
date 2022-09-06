package timetrkkr.users

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserImplTests : ApplicationTests() {
    @Autowired
    private lateinit var userServiceImpl: UserServiceImpl

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var timeRecordRepository: TimeRecordRepository

    @Autowired
    private lateinit var timeRecordServiceImpl: TimeRecordServiceImpl


    @BeforeEach
    private fun cleanUp(){
        timeRecordRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `will create and return user given valid data`(){
        val user = EntityGenerator.createUser()
        userRepository.save(user)

        val createdUser = userServiceImpl.getUserById(user.id)

        assertEquals( user.id ,createdUser.id )
    }

    @Test
    fun `will return error when creating user given invalid firstname`(){
        val user = EntityGenerator.createUser()
        val user2 = user.copy(
            firstName = ""
        )

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"[Firstname should not be empty]\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.createUser(user2)
        }

        assertEquals(expectedMessage, expectedError.message)
    }

    @Test
    fun `will return error when creating user given empty lastname`(){
        val user = EntityGenerator.createUser()
        val user2 = user.copy(
            lastName = ""
        )

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"[Lastname should not be empty]\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.createUser(user2)
        }

        assertEquals(expectedMessage, expectedError.message)
    }

    @Test
    fun `will return error when creating user given empty email`(){
        val user = EntityGenerator.createUser()
        val user2 = user.copy(
            email = ""
        )

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"[Email should not be empty]\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.createUser(user2)
        }

        assertEquals(expectedMessage, expectedError.message)
    }

    @Test
    fun `will return error when creating user given empty data`(){
        val user = EntityGenerator.createUser()
        val user2 = user.copy(
            firstName = "",
            lastName = "",
            email = ""
        )

        val expectedMessage =
            "422 UNPROCESSABLE_ENTITY \"[Firstname should not be empty, Lastname should not be empty, Email should not be empty]\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.createUser(user2)
        }

        assertEquals(expectedMessage, expectedError.message)
    }

    @Test
    fun `will get and return user given valid id`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val expectedUser = userServiceImpl.getUserById(user.id)

        assertEquals(user.id, expectedUser.id)
        assertEquals(user.firstName, expectedUser.firstName)
        assertEquals(user.lastName, expectedUser.lastName)
        assertEquals(user.email, expectedUser.email)
    }

    @Test
    fun `will return error given invalid user id`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val user2 = user.copy(id = 2)

        val expectedMessage = "404 NOT_FOUND \"User with id: ${user2.id} don't exist\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.getUserById(user2.id)
        }

        assertEquals(expectedMessage, expectedError.message)
    }

    @Test
    fun `will update and return user given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val body = user.copy(
            firstName = "Brenda",
            lastName = "Cruz"
        )

        val updatedUser = userServiceImpl.updateUser(user.id , body)

        assertEquals(body.firstName , updatedUser.firstName)
        assertEquals(body.lastName , updatedUser.lastName)
    }

    @Test
    fun `will delete user given valid user id`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        assertEquals(1, userRepository.findAll().count())
        userServiceImpl.deletedUser(user.id)

        assertEquals(0, userRepository.findAll().count())
    }

    @Test
    fun `will return error deleting user given invalid user id`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        assertEquals(1, userRepository.findAll().count())
        val expectedMessage = "404 NOT_FOUND \"User with id: ${TestConstants.invalidId} don't exist\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.deletedUser(TestConstants.invalidId)
        }

        assertEquals(expectedMessage , expectedError.message)
        assertEquals(1, userRepository.findAll().count())
    }

    @Test
    fun `will get and return user time record by day if given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ) )
        val body = TimeRecordFilter(
            dateInput = "2022-09-05"
        )
        userServiceImpl.getUserTimeRecordByDay(user.id , body)

        assertEquals(user.id , timeRecord.user!!.id)
        //assertEquals(timeRecord.id , record!!.id)
    }

    @Test
    fun `will return error getting user time record by day if given invalid login date`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ) )
        val body = TimeRecordFilter(
            dateInput = "2022-08-26222"
        )

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Login date ${body.dateInput} is invalid!\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.getUserTimeRecordByDay(user.id, body)
        }
        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return all time record under a month given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecords = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ))

        val body = SearchCriteria(
            monthNumber = 8,
            year = 2022
        )
        val allUserRecords = timeRecordServiceImpl.getAllTimeRecordByUser(user.id , body)
        val records = userServiceImpl.getUserTimeRecordsPerMonth( user.id, body )

        assertEquals(allUserRecords.size , records.size)
    }

    @Test
    fun `will return error getting user monthly records given invalid month number invalid`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecords = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ))

        val body = SearchCriteria(
            monthNumber = 20,
            year = 2022
        )

        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Invalid month ${body.monthNumber} number\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.getUserTimeRecordsPerMonth(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return error getting user monthly records given invalid year invalid`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecords = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ))

        val body = SearchCriteria(
            monthNumber = 8,
            year = 1234
        )
        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Invalid year ${body.year} number\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.getUserTimeRecordsPerMonth(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return error given search body is empty`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecords = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ))

        val body = null
        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Empty Request Body\""
        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.getUserTimeRecordsPerMonth(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }

    @Test
    fun `will return user time records given valid date range`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecords = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ))

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            dateUntil = LocalDate.now().plusMonths(1)
        )
        val records = userServiceImpl.getUserTimeRecordGivenDateRangePaginated(user.id , body)
        assertEquals(1 , records.size)
    }

    @Test
    fun `will return error getting user time records given invalid date range`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecords = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ))

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            dateUntil = LocalDate.now().minusDays(2)
        )
        val expectedMessage = "422 UNPROCESSABLE_ENTITY \"Unreachable given date range\""

        val expectedError = assertFailsWith<ResponseStatusException> {
            userServiceImpl.getUserTimeRecordGivenDateRangePaginated(user.id , body)
        }

        assertEquals(expectedMessage , expectedError.message)
    }
}