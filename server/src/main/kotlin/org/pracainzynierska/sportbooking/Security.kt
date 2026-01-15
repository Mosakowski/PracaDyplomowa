package org.pracainzynierska.sportbooking

import org.mindrot.jbcrypt.BCrypt

object Security {
    // Funkcja haszowania hasła
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    // Funkcja sprawdzająca, czy podane hasło pasuje do hasha w bazie
    fun checkPassword(candidate: String, hashed: String): Boolean {
        return BCrypt.checkpw(candidate, hashed)
    }
}