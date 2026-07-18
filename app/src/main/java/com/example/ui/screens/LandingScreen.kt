package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.HostelViewModel
import com.example.viewmodel.Screen

@Composable
fun LandingScreen(
    viewModel: HostelViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 1. Navigation Header
            LandingHeader(onNavigateToLogin, onNavigateToRegister)

            // 2. Hero Section
            LandingHero(onNavigateToRegister)

            // 3. Stats section
            LandingStats()

            // 4. Facilities Grid
            LandingFacilities()

            // 5. Room Types Section
            LandingRoomTypes()

            // 6. Why Choose Us (Value Props)
            LandingValueProps()

            // 7. Testimonials
            LandingTestimonials()

            // 8. FAQ Section
            LandingFAQ()

            // 9. Contact Section with map
            LandingContact(viewModel)

            // 10. Footer
            LandingFooter()
        }
    }
}

@Composable
fun LandingHeader(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Elegant American-inspired geometric shield/star logo
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Simplified minimalist "Star & Stripe Shield" look inside the box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Star symbol at the top
                    Text(
                        text = "★",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    // Subtle stripes below the star
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(3.dp).height(8.dp).background(Color.White))
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(modifier = Modifier.width(3.dp).height(8.dp).background(MaterialTheme.colorScheme.secondary))
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(modifier = Modifier.width(3.dp).height(8.dp).background(Color.White))
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Liberty Stay",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Gold Star Stamp
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "★ US",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.testTag("header_login_button")
            ) {
                Text(
                    text = "Sign In",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onNavigateToRegister,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("header_register_button")
            ) {
                Text(text = "Get Started", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LandingHero(onNavigateToRegister: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.08f),
                        secondaryColor.copy(alpha = 0.03f)
                    )
                )
            )
            .drawBehind {
                // Modern American Backdrop: Soft star blue circle and sleek subtle horizontal stripes
                drawCircle(
                    color = primaryColor.copy(alpha = 0.08f),
                    radius = 260f,
                    center = Offset(size.width * 0.9f, size.height * 0.3f)
                )
                // Red horizontal stripes at the bottom left
                drawLine(
                    color = secondaryColor.copy(alpha = 0.04f),
                    start = Offset(0f, size.height * 0.75f),
                    end = Offset(size.width * 0.3f, size.height * 0.75f),
                    strokeWidth = 8f
                )
                drawLine(
                    color = secondaryColor.copy(alpha = 0.04f),
                    start = Offset(0f, size.height * 0.82f),
                    end = Offset(size.width * 0.25f, size.height * 0.82f),
                    strokeWidth = 8f
                )
                drawLine(
                    color = secondaryColor.copy(alpha = 0.04f),
                    start = Offset(0f, size.height * 0.89f),
                    end = Offset(size.width * 0.2f, size.height * 0.89f),
                    strokeWidth = 8f
                )
            }
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🇺🇸 Premium Shared Living • The American Standard",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Elevated Living\nSophisticated Comfort",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 44.sp,
                    letterSpacing = (-1).sp,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Experience the premier student and professional living community. Featuring high-speed internet, premium fitness lounges, fully-furnished master and shared suites, and 24/7 dedicated service.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.widthIn(max = 380.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNavigateToRegister,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .padding(end = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Reserve Stay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                OutlinedButton(
                    onClick = onNavigateToRegister,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .padding(start = 6.dp)
                ) {
                    Text(
                        text = "Explore Rooms",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LandingStats() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val stats = listOf(
            "99%" to "Satisfaction",
            "150+" to "Residents",
            "4.9★" to "Rating",
            "24/7" to "Security"
        )
        stats.forEach { (value, label) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun LandingFacilities() {
    val facilities = listOf(
        Triple(Icons.Default.Wifi, "Gigabit Wi-Fi", "Ultra fast speeds all over the hostel"),
        Triple(Icons.Default.AcUnit, "Full AC Rooms", "Stay perfectly cool during peak summers"),
        Triple(Icons.Default.LocalLaundryService, "In-House Laundry", "Professional laundry service weekly"),
        Triple(Icons.Default.FitnessCenter, "Premium Gym", "Modern machines and free weights space"),
        Triple(Icons.Default.Restaurant, "Nutritious Food", "Delicious breakfast, lunch, and dinner"),
        Triple(Icons.Default.Security, "CCTV & Security", "Biometric locks and security guard 24/7")
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        HostelSectionHeading(
            title = "World-Class Facilities",
            subtitle = "Everything you need to study, work, and thrive comfortably."
        )
        Spacer(modifier = Modifier.height(12.dp))

        facilities.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { (icon, name, desc) ->
                    HostelGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                lineHeight = 14.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

data class RoomTypeInfo(
    val name: String,
    val price: String,
    val occupants: String,
    val description: String,
    val imageIcon: ImageVector,
    val popular: Boolean = false
)

@Composable
fun LandingRoomTypes() {
    val roomTypes = listOf(
        RoomTypeInfo("Single Private Suite", "$300/mo", "1 Resident", "Ultimate privacy with private bath and dedicated custom workspace.", Icons.Default.Person, popular = false),
        RoomTypeInfo("Double Shared Room", "$200/mo", "2 Residents", "Spacious shared living space with individual study desks and lockers.", Icons.Default.People, popular = true),
        RoomTypeInfo("Premium Quad Dorm", "$150/mo", "4 Residents", "Bunk beds with private curtains, reading light, and storage.", Icons.Default.Group, popular = false)
    )

    Column(modifier = Modifier.padding(vertical = 20.dp)) {
        PaddingValues(horizontal = 20.dp).let {
            HostelSectionHeading(
                title = "Curated Room Types",
                subtitle = "Choose the space that matches your style and budget.",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(roomTypes) { room ->
                val cardBorderColor = if (room.popular) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .shadow(2.dp, shape = RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (room.popular) 2.dp else 1.dp,
                            color = cardBorderColor,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = room.imageIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            if (room.popular) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Popular",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = room.occupants,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = room.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                lineHeight = 18.sp
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Starting from",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = room.price,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LandingValueProps() {
    val items = listOf(
        Triple(Icons.Default.VerifiedUser, "Verified & Safe", "Biometric locks, fully gated campus, security guards, and constant CCTV monitoring."),
        Triple(Icons.Default.FlashOn, "Fast & Hassle-free", "Book room in 2 minutes, pay rent digitally, and raise complaints instantly."),
        Triple(Icons.Default.EmojiPeople, "Vibrant Community", "Meet students and young professionals with weekly events and mixers.")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        HostelSectionHeading(
            title = "Why Choose Liberty Stay?",
            subtitle = "We craft premier shared living spaces that blend modern convenience with outstanding hospitality."
        )
        Spacer(modifier = Modifier.height(12.dp))

        items.forEach { (icon, title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LandingTestimonials() {
    val testimonials = listOf(
        "Highly recommended! The internet is incredibly fast, and raising complaints literally takes 5 seconds on the app." to "Sarah Jenkins (Medical Student)",
        "The community here is amazing. The study lounges are quiet, and the gym has excellent modern equipment." to "Marcus Chen (Software Engineer)",
        "Clean, spacious rooms and very secure. The owner Alexander is extremely friendly and resolves problems immediately." to "Emily Watson (Business Student)"
    )

    Column(modifier = Modifier.padding(vertical = 20.dp)) {
        HostelSectionHeading(
            title = "Loved by Residents",
            subtitle = "See what our tenants have to say about their experience.",
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(testimonials) { (quote, author) ->
                HostelGlassCard(modifier = Modifier.width(260.dp)) {
                    Row {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ColorWarning,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "\"$quote\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        ),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = author,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LandingFAQ() {
    val faqs = listOf(
        "How do I book a room?" to "Simply sign up as a customer, view our real-time available rooms, and submit a booking request. The owner will review and approve your booking promptly.",
        "What is included in the rent?" to "Rent covers fully-furnished rooms, unlimited Wi-Fi, laundry facilities, access to the gym, and study lounges. Meals can be added as an optional package.",
        "How do I pay my rent?" to "Rent can be paid securely directly through the customer dashboard using credit/debit cards or bank transfer. You can track all historic and pending payments.",
        "How are maintenance complaints resolved?" to "Raise a complaint in the app, and the owner will assign an technician immediately. You will receive real-time notifications on the status."
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        HostelSectionHeading(
            title = "Frequently Asked Questions",
            subtitle = "Everything you need to know about staying with us."
        )
        Spacer(modifier = Modifier.height(12.dp))

        faqs.forEach { (question, answer) ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = answer,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LandingContact(viewModel: HostelViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        HostelSectionHeading(
            title = "Get In Touch",
            subtitle = "Have questions or want a physical tour? Send us a message."
        )
        Spacer(modifier = Modifier.height(12.dp))

        HostelGlassCard {
            HostelTextField(value = name, onValueChange = { name = it }, label = "Full Name", placeholder = "John Doe", leadingIcon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(12.dp))
            HostelTextField(value = email, onValueChange = { email = it }, label = "Email Address", placeholder = "john@example.com", leadingIcon = Icons.Default.Email)
            Spacer(modifier = Modifier.height(12.dp))
            HostelTextField(value = msg, onValueChange = { msg = it }, label = "Message", placeholder = "Ask us anything...", leadingIcon = Icons.Default.Message, singleLine = false, modifier = Modifier.height(100.dp))
            Spacer(modifier = Modifier.height(18.dp))
            HostelButton(
                text = "Send Message",
                onClick = {
                    if (name.isBlank() || email.isBlank() || msg.isBlank()) {
                        viewModel.showToast("Please fill all fields", isSuccess = false)
                    } else {
                        viewModel.showToast("Message sent! We'll reply within 24 hours.", isSuccess = true)
                        name = ""
                        email = ""
                        msg = ""
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Google Maps UI Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Draw simulated grid/map lines for modern aesthetic
                        val gridCount = 8
                        for (i in 0..gridCount) {
                            val x = size.width * (i.toFloat() / gridCount)
                            val y = size.height * (i.toFloat() / gridCount)
                            drawLine(Color.LightGray.copy(alpha = 0.2f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                            drawLine(Color.LightGray.copy(alpha = 0.2f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OrangeAirbnb,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "742 Evergreen Terrace, Springfield",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Tap to open in Google Maps",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
fun LandingFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Liberty Stay",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Premium, secure, and smart living accommodations for modern residents.",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.widthIn(max = 280.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "© 2026 Liberty Stay Inc. All rights reserved.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
