package timetrkkr.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*

@Entity
@Table(name = "time_records")
data class TimeRecord(
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    val id: Long = 0,

    @Column(
        nullable = false,
        updatable = true,
        name = "dateLogin"
    )
    val dateLogin: LocalDate = LocalDate.now(),

    @Column(
        nullable = false,
        updatable = true,
        name = "timeIn"
    )
    val timeIn: LocalTime = LocalTime.now(),

    @Column(
        nullable = false,
        updatable = true,
        name = "timeOut"
    )
    val timeOut: LocalTime = LocalTime.now(),

    @JsonIgnoreProperties(value = ["users"], allowSetters = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "userId",
        referencedColumnName = "id"
    )
    val user : User?
)
