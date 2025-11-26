package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp

object Users : Table("users") {
    val id = integer("user_id").autoIncrement()
    val name = varchar("user_name", 255)
    val email = varchar("user_email", 255).uniqueIndex()
    val password = varchar("password", 255) //tutaj bede trzymał hash
    val role = enumerationByName("user_type", 20, UserRole::class) //enum
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

// Enum do ról użytkowników
enum class UserRole{
    ADMIN, CLIENT, FIELD_OWNER
}

// tabela obiektów
object Facilities : Table("facilities") {
    val id = integer("facility_id").autoIncrement()
    val ownerId = integer("user_id").references(Users.id)
    val name = varchar("name", 255)
    val location = varchar("location", 255)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object Fields : Table("fields") {
    val id = integer("field_id").autoIncrement()
    val facilityId = integer("facility_id").references(Facilities.id)
    val name = varchar("name", 255)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val typeField = enumerationByName("field_type", 50, TypeField::class)
    val isActive = bool("is_active").default(true)

    // JSONB - kluczowe dla Twojego projektu ("Elastyczny Grafik")
    // Na początku użyjemy text, żeby nie komplikować konfiguracji, Exposed obsługuje JSONB ale wymaga to dodatkowego setupu dialektu
    val hours = text("hours")
    val exceptions = text("exceptions").nullable()

    val slotLengtMinutes = integer("slot_lengt_minutes")
    val pricePerSlot = decimal("price_per_slot", 10, 2)

    override val primaryKey = PrimaryKey(id)
}

enum class TypeField{
    FOOTBALL, VOLLEYBALL, TENNIS, OTHER
}

