package timetrkkr.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    val id: Long = 0,

    @Column(
        nullable = false,
        updatable = true,
        name = "firstName"
    )
    val firstName: String = "",

    @Column(
        nullable = false,
        updatable = true,
        name = "lastName"
    )
    val lastName: String = "",

    @Column(
        nullable = false,
        updatable = true,
        unique =  true,
        name = "email"
    )
    val email: String = "",

    @Column(
        nullable = false,
        updatable = true,
        name = "isActive"
    )
    val isActive: Boolean = true,

    @Column(
        nullable = false,
        updatable = true,
        name = "dateAdded"
    )
    val dateAdded: LocalDate = LocalDate.now(),

    @JsonIgnoreProperties(value = ["user"] , allowSetters = true)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    val timeRecord: List<TimeRecord> = listOf()
){
    override fun toString(): String {
        return super.toString()
    }
}
