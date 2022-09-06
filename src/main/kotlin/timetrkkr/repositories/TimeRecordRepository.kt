package timetrkkr.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import timetrkkr.entities.TimeRecord
import java.time.LocalDate

interface TimeRecordRepository: CrudRepository<TimeRecord , Long> {
    @Query(
        """
            SELECT t FROM TimeRecord t
            WHERE t.user.id = :userId AND t.dateLogin = :dateLogin
        """
    )
    fun getUserTimeRecordByDay(userId: Long , dateLogin: LocalDate): TimeRecord?

    @Query(
        """
            SELECT t FROM TimeRecord t
            WHERE t.user.id = :userId AND t.id IN ( :timeRecordIds )
        """
    )
    fun getAllUserTimeRecords(userId: Long, timeRecordIds: List<Long>): List<TimeRecord>

    @Query(
        """
            SELECT t FROM TimeRecord t
            WHERE t.user.id = :userId AND ( MONTH(t.dateLogin) = :monthNumber AND YEAR(t.dateLogin) = :year )
        """
    )
    fun getAllUserMonthlyUnderTime(userId: Long, monthNumber: Int , year: Int): List<TimeRecord>

    @Query(
        """
            SELECT * FROM time_records WHERE date_login = :date LIMIT :limit OFFSET (:offset * :limit)
        """, nativeQuery = true
    )
    fun getAllPaginatedTimeRecords(limit: Int , offset: Int , date: LocalDate): List<TimeRecord>

    @Query(
        """
           SELECT t FROM TimeRecord t WHERE t.user.id = :userId AND 
           (t.dateLogin >= :dateFrom AND t.dateLogin <= :dateTo) 
       """
    )
    fun getUserTimeRecordByDateRange(userId: Long, dateFrom: LocalDate , dateTo: LocalDate): List<TimeRecord>

    @Query(
        """
            SELECT t FROM TimeRecord t WHERE t.user.id = :userId AND
            MONTH(t.dateLogin) = :monthNumber AND YEAR(t.dateLogin) = :year
        """
    )
    fun getUserTimeRecordPerMonth(userId: Long , monthNumber: Int , year: Int): List<TimeRecord>

    @Query(
        """
           SELECT * FROM time_records WHERE user_id = :userId AND 
           ( date_login >= :dateFrom AND date_login <= :dateTo ) LIMIT :limit OFFSET (:offset * :limit )
       """, nativeQuery = true
    )
    fun getUserTimeRecordByDateRangePaginated( userId: Long ,dateFrom: LocalDate ,
                                               dateTo: LocalDate , limit: Int , offset: Int): List<TimeRecord>

}