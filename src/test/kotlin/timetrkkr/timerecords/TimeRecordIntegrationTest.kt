package timetrkkr.timerecords

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
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

@AutoConfigureMockMvc
class TimeRecordIntegrationTest: ApplicationTests() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userServiceImpl: UserServiceImpl

    @Autowired
    private lateinit var timeRecordServiceImpl: TimeRecordServiceImpl

    @Autowired
    private lateinit var timeRecordRepository: TimeRecordRepository

    @BeforeEach
    fun clearAll(){
        timeRecordRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `will create and return time record given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(
            user = user
        )

        mockMvc.post("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(timeRecord)
        }.andExpect {
            status { isOk() }
            jsonPath("$"){ isNotEmpty() }
        }

    }

    @Test
    fun `will return error 409 given if user already login within the day`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy( user = user )
        timeRecordRepository.save(timeRecord)
        val body = timeRecord.copy()

        mockMvc.post("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isConflict() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }

    }

    @Test
    fun `will return error 422 when creating time record given invalid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now().minusDays(2)
            ,user = user )
//        timeRecordRepository.save(timeRecord)

        mockMvc.post("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(timeRecord)
        }.andExpect {
            status { isUnprocessableEntity() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }

    }

    @Test
    fun `will return time records list by user when given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now().minusDays(2)
            ,user = user )
        timeRecordRepository.save(timeRecord)

        val body = SearchCriteria(
            timeRecordIds = listOf(timeRecord.id)
        )

        mockMvc.get("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will return error 422 if time record don't belong to a user`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now().minusDays(2)
            ,user = user )
        timeRecordRepository.save(timeRecord)

        val body = SearchCriteria(
            timeRecordIds = listOf(timeRecord.id , TestConstants.invalidId)
        )

        mockMvc.get("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will update and logout user given valid logout time`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = timeRecord.copy(
            dateLogin = LocalDate.now(),
            timeOut = LocalTime.now().plusHours(2)
        )
        mockMvc.put("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will return error 404 on update or logout user given invalid login date`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = timeRecord.copy(
            dateLogin = LocalDate.now().minusDays(1),
            timeOut = LocalTime.now().plusHours(2)
        )
        mockMvc.put("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isNotFound() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will return error 409 on update or logout user given invalid logout Time`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = timeRecord.copy(
            dateLogin = LocalDate.now(),
            timeOut = LocalTime.now().minusMinutes(1)
        )
        mockMvc.put("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isConflict() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will delete user time records given valid TimeRecords id or ids`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = SearchCriteria(
            timeRecordIds = listOf(timeRecord.id)
        )
        mockMvc.delete("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will return error 422 deleting user time records given invalid TimeRecords id or ids`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)

        val body = SearchCriteria(
            timeRecordIds = listOf(timeRecord.id, TestConstants.invalidId)
        )
        mockMvc.delete("/api/time-records/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will return all user Time Records under time in a month given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)


        val body = SearchCriteria(
            monthNumber = 8,
            year = 2022
        )

        mockMvc.get("/api/time-records/getAll-monthly-underTime/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }

    }

    @Test
    fun `will return error 422 getting total monthly under time given invalid monthNumber`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)


        val body = SearchCriteria(
            monthNumber = 23,
            year = 2022
        )
        mockMvc.get("/api/time-records/getAll-monthly-underTime/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will return error 422 getting total monthly under time given invalid year`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(user = user)
        timeRecordRepository.save(timeRecord)
        val body = SearchCriteria(
            monthNumber = 8,
            year = 1334
        )
        mockMvc.get("/api/time-records/getAll-monthly-underTime/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
            //jsonPath("$.dateLogin"){ value(timeRecord.dateLogin) }
        }
    }

    @Test
    fun `will return computed user time records given valid date range`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(7)
            ,user = user)
        val timeRecord2 = EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now().plusDays(1),
            timeOut = LocalTime.now().plusHours(7), user = user)

        timeRecordRepository.saveAll( mutableListOf( timeRecord, timeRecord2) )

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now().minusDays(1),
            dateUntil = LocalDate.now().plusDays(4)
        )

        mockMvc.get("/api/time-records/totalTime-render/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            jsonPath("$"){ isNotEmpty() }
        }
    }

    @Test
    fun `will return error 422 when computing user time records given invalid date range`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(7)
            ,user = user)
        val timeRecord2 = EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now().plusDays(1),
            timeOut = LocalTime.now().plusHours(7), user = user)

        timeRecordRepository.saveAll( mutableListOf( timeRecord, timeRecord2) )

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now().minusDays(1),
            dateUntil = LocalDate.now().minusDays(4)
        )

        mockMvc.get("/api/time-records/totalTime-render/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$.dateLogin"){ body.dateUntil.isBefore(body.dateFrom) }
        }
    }

    @Test
    fun `will return list of all time records in a day paginated`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val user2 = user.copy(
            firstName = "Lina",
            email = "lina@mail.com"
        )

        userRepository.saveAll(mutableListOf(user, user2))

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(7)
            ,user = user)

        val timeRecord2 = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(7)
            ,user = user2)


        timeRecordRepository.saveAll( mutableListOf( timeRecord, timeRecord2) )

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            size = 3,
            page = 0
        )

        mockMvc.get("/api/time-records/"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            jsonPath("$"){ isNotEmpty() }
        }
    }

    @Test
    fun `will return error 422 getting list of all time records in a day given invalid page and size value`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val user2 = user.copy(
            firstName = "Lina",
            email = "lina@mail.com"
        )

        userRepository.saveAll(mutableListOf(user, user2))

        val timeRecord = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(7)
            ,user = user)

        val timeRecord2 = EntityGenerator.createTimeRecord().copy(
            timeOut = LocalTime.now().plusHours(7)
            ,user = user2)


        timeRecordRepository.saveAll( mutableListOf( timeRecord, timeRecord2) )

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            size = -3,
            page = -1
        )

        mockMvc.get("/api/time-records/"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$.size"){ body.size < 1 }
            jsonPath("$.page"){ body.size < 0 }
        }
    }
}