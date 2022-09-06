package timetrkkr.servicesImpl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import timetrkkr.entities.TimeRecord
import timetrkkr.entities.User
import timetrkkr.repositories.TimeRecordRepository
import timetrkkr.repositories.UserRepository
import timetrkkr.services.TimeRecordService
import timetrkkr.utils.ComputedTimeRecords
import timetrkkr.utils.SearchCriteria
import timetrkkr.utils.TimeRecordFilter
import timetrkkr.utils.validator.DataValidator
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.Format
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.util.Formatter
import kotlin.math.roundToInt
import kotlin.time.toKotlinDuration

@Service
class TimeRecordServiceImpl(

    private val timeRecordRepository: TimeRecordRepository,
    private val userRepository: UserRepository

    ): DataValidator(), TimeRecordService {

    override fun createTimeRecord(userId: Long, timeRecord: TimeRecord): TimeRecord {

        val user = getUserById(userId)


        if ( userRepository.ifUserAlreadyLogin(user.id, timeRecord.dateLogin) ){
            throw ResponseStatusException(
                HttpStatus.CONFLICT, "User have already login!"
            )
        }

        if ( timeRecord.dateLogin.isBefore( LocalDate.now() ) ){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, "Login date should be current date"
            )
        }

        val timeLog = TimeRecord(
            dateLogin = timeRecord.dateLogin,
            user = user
        )

        return timeRecordRepository.save(timeLog)
    }

    override fun getAllTimeRecordByUser(userId: Long , body: SearchCriteria): List<TimeRecord> {
        val user = getUserById(userId)

        val timeRecordList = timeRecordRepository.getAllUserTimeRecords(user.id , body.timeRecordIds)

        if ( timeRecordList.size != body.timeRecordIds.size ){
            val userTodoIds = timeRecordList.map {todo-> todo.id}
            val missingIds = body.timeRecordIds.filterNot {
                    id -> userTodoIds.contains(id)
            }
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY , "The user does not have particular time record Ids: $missingIds"
            )
        }

        return timeRecordList
    }

    override fun logOutUser(userId: Long, timeRecord: TimeRecord): TimeRecord {
        val user = getUserById(userId)

        val userTimeRecord = timeRecordRepository.getUserTimeRecordByDay(user.id , timeRecord.dateLogin)
            ?: throw  ResponseStatusException(
                HttpStatus.NOT_FOUND ,
                "TimeLog not found given ${timeRecord.dateLogin}"
            )

        if ( timeRecord.timeOut.isBefore(timeRecord.timeIn)){
            throw ResponseStatusException(
                HttpStatus.CONFLICT , "Invalid logout time"
            )
        }

        val logOutTime = LocalTime.of(timeRecord.timeOut.hour, timeRecord.timeOut.minute)


        return timeRecordRepository.save( userTimeRecord.copy(
            timeOut = logOutTime
        ))
    }

    override fun deleteTimeLogByUser(userId: Long, body: SearchCriteria) {
        val user = getUserById(userId)
        val timeRecordList = timeRecordRepository.getAllUserTimeRecords(user.id , body.timeRecordIds)

        if ( timeRecordList.size != body.timeRecordIds.size ){
            val userTodoIds = timeRecordList.map {todo-> todo.id}
            val missingIds = body.timeRecordIds.filterNot {
                    id -> userTodoIds.contains(id)
            }
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY , "The user does not have particular time record Ids: $missingIds"
            )
        }

        return timeRecordRepository.deleteAllById( body.timeRecordIds.toMutableList() )

    }

    override fun getAllMonthlyRecordsByUserUnderTime(userId: Long, body: SearchCriteria): List<TimeRecord> {
        val user = getUserById(userId)

        if (body.monthNumber !in 1..12){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Month number ${body.monthNumber}"
            )
        }

        if (body.year !in 1990..2030){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, "Enter year from 1990 to present: Invalid: ${body.year}"
            )
        }

        val timeRecords = timeRecordRepository.getAllUserMonthlyUnderTime(user.id , body.monthNumber , body.year)

        timeRecords.forEach { timeRecord ->
           println ( Duration.between(timeRecord.timeIn , timeRecord.timeOut ).toMinutes().toDouble() )
        }

        val underTimeRecords = timeRecords.filter { timeRecord ->
            Duration.between(timeRecord.timeIn , timeRecord.timeOut ).toMinutes().toDouble()  < 480.0
        }

        return underTimeRecords
    }

    override fun computeUserRenderedTime(userId: Long, body: TimeRecordFilter): ComputedTimeRecords {

        val user = getUserById(userId)

        if ( body.dateUntil.isBefore(body.dateFrom) ){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, "Invalid given date range"
            )
        }
        //cut out dec
        val decimalFormat = DecimalFormat("#.##")
        decimalFormat.roundingMode = RoundingMode.CEILING

        val userTimeRecord = timeRecordRepository.getUserTimeRecordByDateRange(user.id, body.dateFrom, body.dateUntil)

        val totalTime = userTimeRecord.sumOf { time ->
            Duration.between(time.timeIn, time.timeOut).toMinutes().toDouble()
        }
        val totalTimeDecimal =  (  totalTime ) / 60.0

        val excessTime = userTimeRecord.filter { time->
            Duration.between(time.timeIn, time.timeOut).toMinutes().toDouble() > 480.0
        }

        val allExcessTime = excessTime.sumOf { time->
            Duration.between(time.timeIn, time.timeOut).toMinutes().toDouble() - 480
        }

        val totalExcessTime = allExcessTime / 60.0

        return ComputedTimeRecords(
            totalTime = decimalFormat.format(totalTimeDecimal).toDouble(),
            totalExcessTime = decimalFormat.format(totalExcessTime).toDouble(),
            user = user
        )
    }

    override fun getAllTimeRecords(body: TimeRecordFilter): List<TimeRecord> {

        if (body.size < 0 || body.page < 0){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY ,
                "page: ${body.page} should not be negative and size: ${body.size} is greater than zero"
            )
        }

        return timeRecordRepository.getAllPaginatedTimeRecords( body.size , body.page , body.dateFrom )
    }

    private fun getUserById(userId: Long): User {
        return userRepository.findById(userId).orElseThrow {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User with id : $userId does not exist"
            )
        }
    }
}

