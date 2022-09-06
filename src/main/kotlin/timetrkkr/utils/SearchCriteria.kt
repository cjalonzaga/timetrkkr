package timetrkkr.utils

data class SearchCriteria(
    val monthNumber : Int = 0,
    val year : Int = 0,
    val timeRecordIds: List<Long> = listOf()
)
