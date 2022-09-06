package timetrkkr.utils

import timetrkkr.entities.User

data class ComputedTimeRecords(
    val totalTime: Double = 0.0,
    val totalExcessTime: Double = 0.0,
    val user: User = User()
)
