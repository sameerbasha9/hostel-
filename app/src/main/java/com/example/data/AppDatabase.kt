package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        HostelEntity::class,
        RoomEntity::class,
        BookingEntity::class,
        PaymentEntity::class,
        ComplaintEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun hostelDao(): HostelDao
    abstract fun roomDao(): RoomDao
    abstract fun bookingDao(): BookingDao
    abstract fun paymentDao(): PaymentDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hostel_manager_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // Seed Hostel
            val hostelId = 1
            db.hostelDao().insertHostel(
                HostelEntity(
                    id = hostelId,
                    name = "Urban Living Student Hostel",
                    address = "742 Evergreen Terrace, Springfield",
                    description = "A premium, fully-furnished, modern living space designed for students and young working professionals. Located right near the campus with top-tier amenities, safety, and a vibrant community.",
                    facilities = "Wi-Fi, AC, Laundry, Gym, Food, CCTV, Study Lounge, Power Backup",
                    priceRange = "$150 - $350/mo",
                    imageUrl = ""
                )
            )

            // Seed Users
            val ownerId = db.userDao().insertUser(
                UserEntity(
                    id = 1,
                    name = "Alexander Wright",
                    email = "admin@hostel.com",
                    passwordHash = "admin123",
                    role = "OWNER",
                    phone = "+1 (555) 123-4567",
                    avatarUrl = ""
                )
            ).toInt()

            val customerId = db.userDao().insertUser(
                UserEntity(
                    id = 2,
                    name = "Jane Doe",
                    email = "student@hostel.com",
                    passwordHash = "student123",
                    role = "CUSTOMER",
                    phone = "+1 (555) 987-6543",
                    avatarUrl = ""
                )
            ).toInt()

            val customer2Id = db.userDao().insertUser(
                UserEntity(
                    id = 3,
                    name = "Mark Ronson",
                    email = "mark@test.com",
                    passwordHash = "test123",
                    role = "CUSTOMER",
                    phone = "+1 (555) 444-5555",
                    avatarUrl = ""
                )
            ).toInt()

            // Seed Rooms
            db.roomDao().insertRoom(RoomEntity(id = 101, hostelId = hostelId, roomNumber = "101-A", capacity = 1, occupiedCount = 0, price = 300.0, type = "Single", description = "Spacious single private room with workspace, ensuite bathroom, and street-view balcony."))
            db.roomDao().insertRoom(RoomEntity(id = 102, hostelId = hostelId, roomNumber = "102-B", capacity = 2, occupiedCount = 1, price = 200.0, type = "Double", description = "Shared double room with individual study desks, storage cabinets, and large window."))
            db.roomDao().insertRoom(RoomEntity(id = 201, hostelId = hostelId, roomNumber = "201-C", capacity = 1, occupiedCount = 1, price = 350.0, type = "Deluxe", description = "Premium deluxe private room with individual kitchen counter, mini-fridge, and plush bedding."))
            db.roomDao().insertRoom(RoomEntity(id = 202, hostelId = hostelId, roomNumber = "202-D", capacity = 4, occupiedCount = 2, price = 150.0, type = "Dorm", description = "Cozy 4-bed dormitory room with bunk beds, personal privacy curtains, and security lockers."))

            // Seed Booking for Jane Doe (Approved & Checked-In)
            val startTime = System.currentTimeMillis() - 10 * 24 * 3600 * 1000L // 10 days ago
            val endTime = startTime + 30 * 24 * 3600 * 1000L // 30 days total
            val booking1Id = db.bookingDao().insertBooking(
                BookingEntity(
                    id = 1,
                    customerId = customerId,
                    roomId = 102,
                    startDate = startTime,
                    endDate = endTime,
                    status = "CHECKED_IN",
                    totalAmount = 200.0
                )
            ).toInt()

            // Seed Booking for Mark (Pending)
            val booking2Id = db.bookingDao().insertBooking(
                BookingEntity(
                    id = 2,
                    customerId = customer2Id,
                    roomId = 101,
                    startDate = System.currentTimeMillis(),
                    endDate = System.currentTimeMillis() + 15 * 24 * 3600 * 1000L,
                    status = "PENDING",
                    totalAmount = 300.0
                )
            ).toInt()

            // Seed Payments
            db.paymentDao().insertPayment(
                PaymentEntity(
                    id = 1,
                    bookingId = booking1Id,
                    customerId = customerId,
                    amount = 200.0,
                    billingMonth = "July 2026",
                    date = startTime + 100000L,
                    status = "COMPLETED",
                    paymentMethod = "Card"
                )
            )

            db.paymentDao().insertPayment(
                PaymentEntity(
                    id = 2,
                    bookingId = booking2Id,
                    customerId = customer2Id,
                    amount = 300.0,
                    billingMonth = "July 2026",
                    date = System.currentTimeMillis(),
                    status = "PENDING",
                    paymentMethod = "Bank Transfer"
                )
            )

            // Seed Complaints
            db.complaintDao().insertComplaint(
                ComplaintEntity(
                    id = 1,
                    customerId = customerId,
                    title = "High Wi-Fi Latency",
                    description = "The internet speed in room 102 fluctuates heavily in the evening, making it difficult to attend virtual lectures.",
                    category = "Wi-Fi",
                    status = "PENDING",
                    date = System.currentTimeMillis() - 3 * 24 * 3600 * 1000L
                )
            )

            db.complaintDao().insertComplaint(
                ComplaintEntity(
                    id = 2,
                    customerId = customerId,
                    title = "Leaky Shower Head",
                    description = "The shower in the shared bathroom is dripping continuously.",
                    category = "Plumbing",
                    status = "RESOLVED",
                    date = System.currentTimeMillis() - 8 * 24 * 3600 * 1000L,
                    resolutionNotes = "Plumber replaced the worn rubber washer. Fixed."
                )
            )

            // Seed Notifications
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = 1,
                    userId = customerId,
                    title = "Welcome to Urban Living!",
                    message = "Your check-in is complete. We hope you enjoy your stay with us!",
                    timestamp = startTime
                )
            )

            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = 2,
                    userId = ownerId,
                    title = "New Booking Request",
                    message = "Mark Ronson has requested to book Room 101-A.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
