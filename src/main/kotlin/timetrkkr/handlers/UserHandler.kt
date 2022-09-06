package timetrkkr.handlers

import org.springframework.web.bind.annotation.*
import timetrkkr.entities.TimeRecord
import timetrkkr.entities.User
import timetrkkr.services.UserService
import timetrkkr.utils.SearchCriteria
import timetrkkr.utils.TimeRecordFilter

@RestController
@RequestMapping("/api/")
class UserHandler(
    private val userService: UserService
) {

    @PostMapping("users")
    fun createUser(@RequestBody user: User): User = userService.createUser(user)

    @GetMapping("users/{userId}")
    fun getUserById(@PathVariable("userId") userId: Long): User = userService.getUserById(userId)

    @PutMapping("users/{userId}")
    fun updateUser(@PathVariable("userId") userId: Long,
                   @RequestBody user: User): User = userService.updateUser(userId , user)

    @DeleteMapping("users/{userId}")
    fun deleteUser(@PathVariable("userId") userId: Long) = userService.deletedUser(userId)

    @GetMapping("users/daily-timeRecords/{userId}")
    fun getUserTimeRecordPerDay(@PathVariable("userId") userId: Long,
                                @RequestBody body: TimeRecordFilter) =
                                userService.getUserTimeRecordByDay(userId , body)

    @GetMapping("users/monthly-timeRecords/{userId}")
    fun getUserTimeRecordsPerMonth(@PathVariable("userId") userId: Long ,
                                   @RequestBody body: SearchCriteria?): List<TimeRecord>
                                   = userService.getUserTimeRecordsPerMonth(userId , body)

    @GetMapping("users/getBy-dateRange/{userId}")
    fun getUserTimeRecordsByDateRange(@PathVariable("userId") userId: Long ,
                                   @RequestBody body: TimeRecordFilter): List<TimeRecord>
            = userService.getUserTimeRecordGivenDateRangePaginated( userId, body )
}