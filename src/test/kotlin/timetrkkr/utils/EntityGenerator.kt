package timetrkkr.utils

import timetrkkr.entities.TimeRecord
import timetrkkr.entities.User
import java.time.LocalDate
import java.time.LocalTime

object EntityGenerator {

    fun createUser() = User(
        firstName = "John",
        lastName = "Smith",
        email = "jsmith@mail.com",
        isActive = true,
        dateAdded = LocalDate.now()
    )

    fun createTimeRecord() = TimeRecord(
        dateLogin = LocalDate.now(),
        timeIn = LocalTime.now(),
        timeOut = LocalTime.now(),
        user = createUser()
    )
}