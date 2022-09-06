package timetrkkr.services

import timetrkkr.entities.TimeRecord
import timetrkkr.utils.ComputedTimeRecords
import timetrkkr.utils.SearchCriteria
import timetrkkr.utils.TimeRecordFilter

interface TimeRecordService {
    fun createTimeRecord(userId: Long, timeRecord: TimeRecord): TimeRecord

    fun getAllTimeRecordByUser(userId: Long, body: SearchCriteria): List<TimeRecord>

    fun logOutUser(userId: Long , timeRecord: TimeRecord): TimeRecord

    fun deleteTimeLogByUser(userId: Long, body: SearchCriteria)

    fun getAllMonthlyRecordsByUserUnderTime(userId: Long , body: SearchCriteria): List<TimeRecord>

    fun computeUserRenderedTime(userId: Long , body: TimeRecordFilter):ComputedTimeRecords

    fun getAllTimeRecords( body: TimeRecordFilter ): List<TimeRecord>
}