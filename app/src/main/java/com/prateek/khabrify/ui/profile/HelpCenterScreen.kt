package com.prateek.khabrify.ui.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prateek.khabrify.ui.theme.KhabrifyNavy
import androidx.core.net.toUri

// Data class to hold our FAQ questions and answers
data class FaqItem(val question: String, val answer: String)

@Composable
fun HelpCenterScreen() {
    val context = LocalContext.current

    // List of Frequently Asked Questions
    val faqs = listOf(
        FaqItem(
            question = "How do I change the language or country of my news?",
            answer = "You can change your default Region and Language in the Profile Settings tab. To change them temporarily just for one search, use the dropdown filters at the top of the Explore screen."
        ),
        FaqItem(
            question = "Why is my news feed empty or showing errors?",
            answer = "First, check your internet connection. If you are on the Explore screen, try resetting your filters. Sometimes searching for a very specific topic (e.g., 'Mars') with narrow filters (e.g., 'Sports' in 'French') will yield zero results."
        ),
        FaqItem(
            question = "Where are my saved articles stored?",
            answer = "When you bookmark an article, it is securely saved to your account. You can view all your bookmarked articles at any time in the 'Saved' tab at the bottom of the screen."
        ),
        FaqItem(
            question = "How do I reset my password?",
            answer = "Go to the Profile tab, scroll down to Account Settings, and tap 'Reset Password'. We will send a secure recovery link directly to your registered email address."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Light gray background matching Profile
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. HEADER ---
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Help & Support",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }

        // --- 2. FAQ SECTION ---
        item {
            SectionTitle("Frequently Asked Questions")
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(faqs) { faq ->
            ExpandableFaqCard(faqItem = faq)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- 3. CONTACT SUPPORT SECTION ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Still need help?")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "If you found a bug or have a suggestion, our support team is ready to help.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            val emailAddress = "khabrify@gmail.com"
                            val subject = "[Feedback/Bug] - [Enter short summary here]"
                            val body = """
    Hi Support Team,
    
    [Please describe your issue or suggestion here]
    
    ------------------------------
    CATEGORY: [] Bug Report  [] Suggestion/Feedback
    ------------------------------
    
    Additional Details:
    - App Version: 1.0.0
    - Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
    - Android Version: ${android.os.Build.VERSION.RELEASE}
    ------------------------------
    
    Thanks & Regards
    [Your Name]
""".trimIndent()

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:".toUri() // Only email apps should handle this
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                putExtra(Intent.EXTRA_TEXT, body)
                            }

                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app installed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("Email Support Team", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 4. LEGAL LINKS ---
        item {
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("Legal")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    LegalLinkRow(
                        title = "Privacy Policy",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://doc-hosting.flycricket.io/khabrify-privacy-policy/2d048653-2b0b-4e44-8bca-b1613007fb7e/privacy".toUri())
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    LegalLinkRow(
                        title = "Terms of Service",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://doc-hosting.flycricket.io/khabrify-terms-conditions/31b6b644-2784-4519-b999-6a5ee7ed7c75/terms".toUri())
                            context.startActivity(intent)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp)) // Bottom padding
        }
    }
}

// --- REUSABLE UI COMPONENTS ---
@Composable
fun ExpandableFaqCard(faqItem: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    // Animate the rotation of the arrow icon
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Arrow Rotation Animation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top Row (Question + Arrow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = faqItem.question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f) // Takes up available space before the icon
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationState) // Applies the animation
                )
            }

            // Expanding Answer Section
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = faqItem.answer,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LegalLinkRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Open Link",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}