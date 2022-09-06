package timetrkkr.users

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
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

@AutoConfigureMockMvc
class UserIntegrationTest: ApplicationTests() {

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
    fun `create user return OK given valid data`(){
        val user = EntityGenerator.createUser()

        mockMvc.post("/api/users"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(user)
        }.andExpect {
            status { isOk() }
            jsonPath("$"){ isNotEmpty() }
        }
    }

    @Test
    fun `create user will return 409 if email already exist`(){
        val user = EntityGenerator.createUser()
        val duplicateUser = userServiceImpl.createUser(user)

        mockMvc.post("/api/users"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(duplicateUser)
        }.andExpect {
            status { isConflict() }
            //jsonPath("$"){ isNotEmpty() }
        }
    }

    @Test
    fun `will return error 422 if firstName empty`(){
        val user = EntityGenerator.createUser()
        val duplicateUser = (user.copy(
            firstName = ""
        ))

        mockMvc.post("/api/users"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(duplicateUser)
        }.andExpect {
            status { isUnprocessableEntity() }
            //jsonPath("$"){ }
        }
    }
    @Test
    fun `will return error 422 if lastName empty`(){
        val user = EntityGenerator.createUser()
        val duplicateUser = (user.copy(
            lastName = ""
        ))

        mockMvc.post("/api/users"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(duplicateUser)
        }.andExpect {
            status { isUnprocessableEntity() }
            //jsonPath("$"){ }
        }
    }

    @Test
    fun `will return error 422 if email  is empty`(){
        val user = EntityGenerator.createUser()
        val duplicateUser = (user.copy(
            email = ""
        ))

        mockMvc.post("/api/users"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(duplicateUser)
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$"){ user.email != duplicateUser.email }
        }
    }

    @Test
    fun `will return error 422 if all property  is empty`(){
        val user = EntityGenerator.createUser()
        val duplicateUser = (user.copy(
            firstName = "",
            lastName = "",
            email = ""
        ))

        mockMvc.post("/api/users"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(duplicateUser)
        }.andExpect {
            status { isUnprocessableEntity() }
            //jsonPath("$"){ user.id == duplicateUser.id }
        }
    }

    @Test
    fun `will get and return user given valid id`(){
        val user = userRepository.save(EntityGenerator.createUser())
        mockMvc.get("/api/users/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.id"){ value(user.id) }
        }
    }

    @Test
    fun `will get and return user given invalid id`(){
        val user = userRepository.save(EntityGenerator.createUser())
        mockMvc.get("/api/users/${TestConstants.invalidId}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `will update user data given valid datas`(){
        val user = userRepository.save(EntityGenerator.createUser())
        val updatedUser = user.copy(
            firstName = "Lina",
            lastName = "Han"
        )

        mockMvc.put("/api/users/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updatedUser)
        }.andExpect {
            status { isOk() }
            jsonPath("$"){ compareValues(user.firstName, updatedUser.firstName)  }
        }
    }

    @Test
    fun `will delete and return user with valid given id`(){
        val user = userRepository.save( EntityGenerator.createUser() )

        mockMvc.delete("/api/users/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `will get and return user time record by day if given valid data`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ) )

        val body = TimeRecordFilter(
            dateInput = "2022-08-26"
        )

        mockMvc.get("/api/users/daily-timeRecords/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            jsonPath("$"){user.timeRecord.filter { record->
                record.dateLogin.toString() == body.dateInput
            }}
        }
    }

    @Test
    fun `will return 422 if user time record by day given date login is invalid`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ) )

        val body = TimeRecordFilter(
            dateInput = "2022-08-264"
        )

        mockMvc.get("/api/users/daily-timeRecords/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$"){user.timeRecord.filter { record->
                record.dateLogin.toString() != body.dateInput
            }}
        }
    }

    @Test
    fun `will return all user time record in a month given valid month number`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now(),
            user = user
        ) )

        val body = SearchCriteria(
            monthNumber = 8,
            year = 2022
        )

        mockMvc.get("/api/users/monthly-timeRecords/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            jsonPath("$"){user.timeRecord.filter { record->
                record.dateLogin.month.value == body.monthNumber
            }}
        }
    }

    @Test
    fun `will return error getting user record in a month given invalid month number`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            dateLogin = LocalDate.now(),
            user = user
        ) )

        val body = SearchCriteria(
            monthNumber = 45
        )

        mockMvc.get("/api/users/monthly-timeRecords/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
        }
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

        mockMvc.get("/api/users/getBy-dateRange/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `will return user time records given invalid date range`(){
        val user = userRepository.save( EntityGenerator.createUser() )
        val timeRecords = timeRecordRepository.save( EntityGenerator.createTimeRecord().copy(
            user = user
        ))

        val body = TimeRecordFilter(
            dateFrom = LocalDate.now(),
            dateUntil = LocalDate.now().minusDays(3)
        )

        mockMvc.get("/api/users/getBy-dateRange/${user.id}"){
            accept(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$"){ timeRecords.dateLogin != body.dateUntil }
        }
    }
}