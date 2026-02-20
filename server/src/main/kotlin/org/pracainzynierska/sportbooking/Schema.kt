package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.pracainzynierska.sportbooking.Fields.nullable

object Users : Table("users") {
    val id = integer("user_id").autoIncrement()
    val name = varchar("user_name", 255)
    val email = varchar("user_email", 255).uniqueIndex()
    val password = varchar("password", 255) //tutaj bede trzymał hash
    val role = enumerationByName("user_type", 20, UserRole::class) //enum
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

enum class FieldType {
    PILKA_NOZNA, KORT_TENISOWY, KOSZYKOWKA, INNE
}

// Enum do ról użytkowników
enum class UserRole{
    ADMIN, CLIENT, FIELD_OWNER
}

enum class FacilityRole { ADMIN, STAFF }
enum class FieldStatus { ACTIVE, INACTIVE, MAINTENANCE }

// tabela obiektów
object Facilities : Table("facilities") {
    val id = integer("facility_id").autoIncrement()
    val userId = integer("user_id").references(Users.id) // Główny właściciel
    val name = varchar("name", 100)
    val location = varchar("location", 255)
    val description = text("description").nullable()

    // 👇 USUNĘLIŚMY STĄD openingTime, closingTime i maxDaysAdvance!

    override val primaryKey = PrimaryKey(id)
}

// --- NOWA TABELA: PRACOWNICY OBIEKTU ---
object FacilityMembers : Table("facility_members") {
    val id = integer("member_id").autoIncrement()
    val facilityId = integer("facility_id").references(Facilities.id)
    val userId = integer("user_id").references(Users.id)
    val role = enumerationByName("role", 20, FacilityRole::class)

    override val primaryKey = PrimaryKey(id)
}


// --- ZAKTUALIZOWANA TABELA BOISK ---
object Fields : Table("fields") {
    val id = integer("field_id").autoIncrement()
    val facilityId = integer("facility_id").references(Facilities.id)
    val name = varchar("name", 100)
    val fieldType = enumerationByName("field_type", 20, FieldType::class)
    val pricePerSlot = decimal("price_per_slot", 10, 2)
    val minSlotDuration = integer("min_slot_duration").default(60)
    val description = text("description").nullable()
    val status = enumerationByName("status", 20, FieldStatus::class).default(FieldStatus.ACTIVE)
    val photoUrl = varchar("photo_url", 500).nullable() // Link do zdjęcia
    val maxDaysAdvance = integer("max_days_advance").default(30)
    val cancellationHours = integer("cancellation_hours").default(24) // Na ile h przed można anulować

    // JSON zapisany jako tekst. Tutaj będziemy trzymać Map<DayOfWeekIso, DaySchedule>
    val weeklySchedule = text("weekly_schedule").nullable()

    override val primaryKey = PrimaryKey(id)
}

enum class TypeField{
    FOOTBALL, VOLLEYBALL, TENNIS, OTHER
}

// Enum statusu rezerwacji
// : active, cancelled, waiting, completed
enum class BookingStatus {
    WAITING, ACTIVE, COMPLETED, CANCELLED, TECHNICAL
}

// Tabela Booking
object Bookings : Table("bookings") {
    val id = integer("booking_id").autoIncrement()

    // Powiązania (Klucze obce)
    val userId = integer("user_id").references(Users.id) // Kto rezerwuje?
    val fieldId = integer("field_id").references(Fields.id) // Co rezerwuje?

    // Czas rezerwacji (Timestamp)
    val start = timestamp("start_time") // Zmieniam nazwę na start_time dla jasności w SQL
    val end = timestamp("end_time")

    // Status i cena
    val status = enumerationByName("status", 20, BookingStatus::class).default(BookingStatus.WAITING)
    val price = decimal("price", 10, 2) // Cena za tę konkretną rezerwację

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val manualClientName = varchar("manual_client_name", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}

