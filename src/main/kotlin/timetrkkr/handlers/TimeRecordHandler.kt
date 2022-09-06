package timetrkkr.handlers

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import timetrkkr.entities.TimeRecord
import timetrkkr.services.TimeRecordService
import timetrkkr.utils.ComputedTimeRecords
import timetrkkr.utils.SearchCriteria
import timetrkkr.utils.TimeRecordFilter


@RestController
@RequestMapping("/api/")
class TimeRecordHandler(
    private val timeRecordService: TimeRecordService
) {
    @PostMapping("time-records/{userId}")
    fun createTimeRecord(@PathVariable("userId") userId: Long,
                         @RequestBody timeRecord: TimeRecord): TimeRecord
                         = timeRecordService.createTimeRecord(userId , timeRecord)

    @GetMapping("time-records/{userId}")
    fun getUserTimeRecords(@PathVariable("userId") userId: Long, @RequestBody body: SearchCriteria):List<TimeRecord>
    = timeRecordService.getAllTimeRecordByUser(userId , body)

    @PutMapping("time-records/{userId}")
    fun logOutUser(@PathVariable("userId") userId: Long, @RequestBody body: TimeRecord): TimeRecord =
        timeRecordService.logOutUser(userId, body)

    @DeleteMapping("time-records/{userId}")
    fun deleteTimeLogByUser(@PathVariable("userId") userId: Long, @RequestBody body: SearchCriteria)
    = timeRecordService.deleteTimeLogByUser(userId , body)

    @GetMapping("time-records/getAll-monthly-underTime/{userId}")
    fun computeMonthlyTimeLogByUser(@PathVariable("userId") userId: Long ,
                                    @RequestBody body: SearchCriteria):
             List<TimeRecord> = timeRecordService.getAllMonthlyRecordsByUserUnderTime(userId , body)

    @GetMapping("time-records/totalTime-render/{userId}")
    fun computeUserTimeRendered(@PathVariable("userId") userId: Long ,
                                @RequestBody body: TimeRecordFilter):ComputedTimeRecords =
        timeRecordService.computeUserRenderedTime(userId , body)

    @GetMapping("time-records/")
    fun getAllPaginatedTimeRecords(@RequestBody body: TimeRecordFilter): List<TimeRecord> =
        timeRecordService.getAllTimeRecords(body)
}