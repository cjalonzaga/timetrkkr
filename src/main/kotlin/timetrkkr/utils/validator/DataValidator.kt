package timetrkkr.utils.validator

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import timetrkkr.entities.User
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

abstract class DataValidator {

     fun User.validate(){

        val exceptionMessages = mutableListOf<String>()

        if (User::firstName.get(this) == ""){
            exceptionMessages.add("Firstname should not be empty")
        }

        if (User::lastName.get(this) == ""){
            exceptionMessages.add("Lastname should not be empty")
        }

        if (User::email.get(this) == ""){
            exceptionMessages.add("Email should not be empty")
        }

        if (exceptionMessages.size > 0){
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                exceptionMessages.toString()
            )
        }
    }

    protected fun validateDateString(date: String): Boolean{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateFormat.isLenient = false
        try {
            dateFormat.parse(date)
        }catch (ex: Exception){
            return false
        }
        return true
    }

}