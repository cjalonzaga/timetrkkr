package timetrkkr.services

import timetrkkr.entities.TimeRecord
import timetrkkr.entities.User
import timetrkkr.utils.SearchCriteria
import timetrkkr.utils.TimeRecordFilter

interface UserService {

    fun createUser(user: User): User

    fun getUserById(userId: Long): User

    fun updateUser(userId: Long, user: User): User

    fun deletedUser(userId: Long)

    fun getUserTimeRecordByDay(userId: Long, body: TimeRecordFilter) :TimeRecord?

    fun getUserTimeRecordsPerMonth(userId: Long, body: SearchCriteria?) :List<TimeRecord>

    fun getUserTimeRecordGivenDateRangePaginated(userId: Long, body: TimeRecordFilter): List<TimeRecord>
}