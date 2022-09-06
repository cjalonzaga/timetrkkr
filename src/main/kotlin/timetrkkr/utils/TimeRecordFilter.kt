package timetrkkr.utils

import java.time.LocalDate

data class TimeRecordFilter(
    val dateInput: String = "",
    val dateFrom : LocalDate = LocalDate.now(),
    val dateUntil: LocalDate = LocalDate.now(),
    val size : Int = 100,
    val page : Int = 0
)