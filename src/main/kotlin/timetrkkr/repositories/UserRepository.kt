package timetrkkr.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import timetrkkr.entities.TimeRecord
import timetrkkr.entities.User
import java.time.LocalDate
import java.time.LocalDateTime

interface UserRepository: CrudRepository<User, Long> {
    @Query(
        """
            SELECT CASE WHEN COUNT(u) > 0
            THEN TRUE
            ELSE FALSE 
            END
            FROM User u WHERE u.firstName = :firstName AND u.lastName = :lastName
        """
    )
    fun ifUserExist(firstName: String, lastName: String):Boolean

    @Query(
        """
            SELECT CASE WHEN COUNT(u) > 0
            THEN TRUE
            ELSE FALSE 
            END
            FROM User u WHERE u.email = :email
        """
    )
    fun ifEmailExist(email: String):Boolean

    @Query(
        """
            SELECT CASE WHEN COUNT(u) > 0
            THEN TRUE
            ELSE FALSE 
            END
            FROM User u JOIN u.timeRecord as time_record 
            WITH time_record.dateLogin = :dateLogin AND u.id = :userId
        """
    )
    fun ifUserAlreadyLogin(userId: Long , dateLogin: LocalDate):Boolean

}