package timetrkkr.servicesImpl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import timetrkkr.entities.TimeRecord
import timetrkkr.entities.User
import timetrkkr.repositories.TimeRecordRepository
import timetrkkr.repositories.UserRepository
import timetrkkr.services.UserService
import timetrkkr.utils.SearchCriteria
import timetrkkr.utils.TimeRecordFilter
import timetrkkr.utils.validator.DataValidator
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId

@Service
class UserServiceImpl(

    private val userRepository: UserRepository,
    private val timeRecordRepository: TimeRecordRepository

): DataValidator(), UserService {

    override fun createUser(user: User): User {

        user.validate()

        if ( userRepository.ifUserExist(user.firstName , user.lastName) ){
            throw ResponseStatusException(
                HttpStatus.CONFLICT ,
                "User with first name: ${user.firstName} and lastname: ${user.lastName} already exist"
            )
        }

        if ( userRepository.ifEmailExist(user.email) ){
            throw ResponseStatusException(
                HttpStatus.CONFLICT , "Email ${user.email} already exist"
            )
        }

        return userRepository.save(user)
    }

    override fun getUserById(userId: Long): User {

        return userRepository.findById(userId).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND , "User with id: $userId don't exist"
            )
        }

    }

    override fun updateUser(userId: Long, user: User): User {

        val originalUser = getUserById(userId)

        if ( userRepository.ifUserExist(user.firstName , user.lastName) ){
            throw ResponseStatusException(
                HttpStatus.CONFLICT ,
                "User with first name: ${user.firstName} and lastname: ${user.lastName} already exist"
            )
        }

        return userRepository.save(originalUser.copy(
            firstName = user.firstName,
            lastName = user.lastName
        ))
    }

    override fun deletedUser(userId: Long) {
        val user = getUserById(userId)
        return userRepository.deleteById(user.id)
    }

    override fun getUserTimeRecordByDay(userId: Long, body: TimeRecordFilter): TimeRecord? {

        val user = getUserById(userId)
        val formatter = SimpleDateFormat("yyyy-MM-dd")

        if( !validateDateString(body.dateInput) ){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY , "Login date ${body.dateInput} is invalid!"
            )
        }

        val dateLogin = formatter.parse(body.dateInput).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate()

        return timeRecordRepository.getUserTimeRecordByDay(user.id , dateLogin )
    }

    override fun getUserTimeRecordsPerMonth(userId: Long, body: SearchCriteria?): List<TimeRecord> {
        val user = getUserById(userId)

        if (body == null){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY , "Empty Request Body"
            )
        }

        if (body!!.monthNumber !in 1..12){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY , "Invalid month ${body.monthNumber} number"
            )
        }

        if (body.year !in 1990..2030){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY , "Invalid year ${body.year} number"
            )
        }

        return timeRecordRepository.getUserTimeRecordPerMonth(user.id , body.monthNumber , body.year)

    }

    override fun getUserTimeRecordGivenDateRangePaginated(userId: Long, body: TimeRecordFilter): List<TimeRecord> {
        val user = getUserById(userId)

        if ( body.dateUntil.isBefore( body.dateFrom ) ){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY , "Unreachable given date range"
            )
        }

        if (body.size < 0 || body.page < 0){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY ,
                "page: ${body.page} should not be negative and ${body.size} greater zero"
            )
        }

        return  timeRecordRepository.getUserTimeRecordByDateRangePaginated(
            user.id , body.dateFrom , body.dateUntil , body.size, body.page
        )
    }

}