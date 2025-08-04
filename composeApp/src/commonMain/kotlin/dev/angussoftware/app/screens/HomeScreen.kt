package dev.angussoftware.app.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets

/**
 * HomeScreen is the main landing page of the application.
 * It displays personal and professional information about the user,
 * including a hero section, about me section, and contact information.
 * 
 * The screen uses a scrollable layout to accommodate all content
 * and implements a fade-in animation for a polished user experience.
 * 
 * Top and bottom padding are added to account for the status bar and navigation bar,
 * while maintaining the edge-to-edge effect.
 */
@Composable
fun HomeScreen() {
    // Scroll state for the entire screen to enable scrolling through all sections
    val scrollState = rememberScrollState()
    
    // Animation states for fade-in effect when the screen is first displayed
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeIn"
    )
    
    // Trigger animation on composition
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // Get the status bar height and navigation bar height
    val statusBarHeightPx = WindowInsets.statusBars.getTop(LocalDensity.current)
    val navigationBarHeightPx = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    
    // Convert to DP
    val density = LocalDensity.current
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }
    val navigationBarHeightDp = with(density) { navigationBarHeightPx.toDp() }
    
    // Add horizontal padding
    val verticalPadding = 16.dp
    
    // Main container for all sections
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(statusBarHeightDp))

        // Hero Section - displays profile image, name, title, and tagline
        HeroSection(alpha)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // About Me Section - displays professional summary and skills
        AboutMeSection(alpha)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Contact Information Section - displays email, location, and social media links
        ContactSection(alpha)

        Spacer(modifier = Modifier.height(navigationBarHeightDp))
    }
}

/**
 * HeroSection displays the top section of the home page with the user's
 * profile image, name, professional title, and a brief tagline.
 * 
 * @param alpha The opacity value for the fade-in animation
 */
@Composable
fun HeroSection(alpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image (using a colored Box as placeholder)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            Text(
                text = "John Doe",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Professional Title
            Text(
                text = "Full-Stack Developer",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tagline
            Text(
                text = "Building innovative solutions with cutting-edge technologies",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * AboutMeSection displays professional information about the user,
 * including a summary of their background and skills.
 * 
 * @param alpha The opacity value for the fade-in animation
 */
@Composable
fun AboutMeSection(alpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Section Title
            Text(
                text = "About Me",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Professional Summary
            Text(
                text = "I am a passionate Full-Stack Developer with expertise in modern web and mobile technologies. " +
                      "With a strong foundation in both frontend and backend development, I create seamless, " +
                      "user-centered applications that solve real-world problems.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "My approach combines technical excellence with creative problem-solving. " +
                      "I'm dedicated to writing clean, maintainable code and staying current with " +
                      "industry best practices and emerging technologies.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Skills Section
            Text(
                text = "Key Skills",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Skills as chips in rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillChip("Kotlin")
                SkillChip("Compose")
                SkillChip("Android")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillChip("JavaScript")
                SkillChip("React")
                SkillChip("Node.js")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillChip("UI/UX")
                SkillChip("Git")
                SkillChip("CI/CD")
            }
        }
    }
}

/**
 * SkillChip displays a single skill as a chip/tag with appropriate styling.
 * 
 * @param text The name of the skill to display
 */
@Composable
fun SkillChip(text: String) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * ContactSection displays the user's contact information and social media links.
 * 
 * @param alpha The opacity value for the fade-in animation
 */
@Composable
fun ContactSection(alpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Section Title
            Text(
                text = "Contact Information",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Email
            ContactItem(
                title = "Email",
                content = "john.doe@example.com"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location
            ContactItem(
                title = "Location",
                content = "San Francisco, CA"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Social Media
            Text(
                text = "Connect with me",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Social Media Links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialMediaButton("LinkedIn")
                SocialMediaButton("GitHub")
                SocialMediaButton("Twitter")
            }
        }
    }
}

/**
 * ContactItem displays a single contact information item with a title and content.
 * 
 * @param title The label for the contact information (e.g., "Email", "Location")
 * @param content The actual contact information value
 */
@Composable
fun ContactItem(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * SocialMediaButton displays a button for a social media platform.
 * 
 * @param platform The name of the social media platform
 */
@Composable
fun SocialMediaButton(platform: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = platform,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}