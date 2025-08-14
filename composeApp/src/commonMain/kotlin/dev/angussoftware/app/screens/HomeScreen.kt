package dev.angussoftware.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.angussoftware.app.currentWindowAdaptiveInfo
import dev.angussoftware.app.ui.components.ScreenContainer
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.components.SkillChip
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import angussoftwareapp.composeapp.generated.resources.Res
import angussoftwareapp.composeapp.generated.resources.home_name
import angussoftwareapp.composeapp.generated.resources.home_title
import angussoftwareapp.composeapp.generated.resources.home_tagline
import angussoftwareapp.composeapp.generated.resources.home_about_title
import angussoftwareapp.composeapp.generated.resources.home_about_paragraph1
import angussoftwareapp.composeapp.generated.resources.home_about_paragraph2
import angussoftwareapp.composeapp.generated.resources.home_skills_title
import angussoftwareapp.composeapp.generated.resources.tech_kotlin
import angussoftwareapp.composeapp.generated.resources.tech_compose
import angussoftwareapp.composeapp.generated.resources.home_skill_android
import angussoftwareapp.composeapp.generated.resources.home_skill_javascript
import angussoftwareapp.composeapp.generated.resources.home_skill_react
import angussoftwareapp.composeapp.generated.resources.home_skill_nodejs
import angussoftwareapp.composeapp.generated.resources.home_skill_uiux
import angussoftwareapp.composeapp.generated.resources.home_skill_git
import angussoftwareapp.composeapp.generated.resources.home_skill_cicd
import angussoftwareapp.composeapp.generated.resources.home_contact_title
import angussoftwareapp.composeapp.generated.resources.home_contact_email_label
import angussoftwareapp.composeapp.generated.resources.home_contact_email_value
import angussoftwareapp.composeapp.generated.resources.home_contact_location_label
import angussoftwareapp.composeapp.generated.resources.home_contact_location_value
import angussoftwareapp.composeapp.generated.resources.home_connect_with_me
import angussoftwareapp.composeapp.generated.resources.platform_bluesky
import angussoftwareapp.composeapp.generated.resources.platform_linkedin
import angussoftwareapp.composeapp.generated.resources.platform_github
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.clickable

private const val SKILL_CHIPS_PER_ROW = 3

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
@Preview
fun HomeScreen() {
    ScreenContainer { alpha, tilePadding ->
        // Hero Section - displays profile image, name, title, and tagline
        HeroSection(alpha)

        Spacer(modifier = Modifier.height(tilePadding * 2))

        // About Me Section - displays professional summary and skills
        AboutMeSection(alpha)

        Spacer(modifier = Modifier.height(tilePadding * 2))

        // Contact Information Section - displays email, location, and social media links
        ContactSection(alpha)
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
    SectionCard(alpha = alpha) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
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
                text = stringResource(Res.string.home_name),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Professional Title
            Text(
                text = stringResource(Res.string.home_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            Text(
                text = stringResource(Res.string.home_tagline),
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

    val isCompactScreen = currentWindowAdaptiveInfo().isCompact

    SectionCard(alpha = alpha) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Section Title
            Text(
                text = stringResource(Res.string.home_about_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Professional Summary
            Text(
                text = stringResource(Res.string.home_about_paragraph1),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(Res.string.home_about_paragraph2),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Skills Section
            Text(
                text = stringResource(Res.string.home_skills_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val skillChipList = listOf(
                Res.string.tech_kotlin,
                Res.string.tech_compose,
                Res.string.home_skill_android,
                Res.string.home_skill_javascript,
                Res.string.home_skill_react,
                Res.string.home_skill_nodejs,
                Res.string.home_skill_uiux,
                Res.string.home_skill_git,
                Res.string.home_skill_cicd,
            ).map { stringResource(it) }
            if (isCompactScreen && SKILL_CHIPS_PER_ROW > 0) {
                for (i in 0..skillChipList.size - 1 step SKILL_CHIPS_PER_ROW) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skillChipList.subList(i, minOf(i + SKILL_CHIPS_PER_ROW, skillChipList.size))
                            .forEach {
                                SkillChip(it)
                            }
                    }
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skillChipList.forEach {
                        SkillChip(it)
                    }
                }
            }
        }
    }
}

/**
 * SkillChip displays a single skill as a chip/tag with appropriate styling.
 *
 * @param text The name of the skill to display
 */

/**
 * ContactSection displays the user's contact information and social media links.
 *
 * @param alpha The opacity value for the fade-in animation
 */
@Composable
fun ContactSection(alpha: Float) {
    SectionCard(alpha = alpha) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Section Title
            Text(
                text = stringResource(Res.string.home_contact_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Email
            ContactItem(
                title = stringResource(Res.string.home_contact_email_label),
                content = stringResource(Res.string.home_contact_email_value)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location
            ContactItem(
                title = stringResource(Res.string.home_contact_location_label),
                content = stringResource(Res.string.home_contact_location_value)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Social Media
            Text(
                text = stringResource(Res.string.home_connect_with_me),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Social Media Links
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocialMediaButton(
                    platform = stringResource(Res.string.platform_linkedin),
                    url = "https://www.linkedin.com/in/harry-cliff/"
                )
                SocialMediaButton(
                    platform = stringResource(Res.string.platform_github),
                    url = "https://github.com/RhoMancer/Blink-Reader"
                )
                SocialMediaButton(
                    platform = stringResource(Res.string.platform_bluesky),
                    url = "https://bsky.app/profile/rhomancer.bsky.social"
                )
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
fun SocialMediaButton(platform: String, url: String) {
    val uriHandler = LocalUriHandler.current
    androidx.compose.material3.OutlinedCard(
        modifier = Modifier.clickable { uriHandler.openUri(url) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = platform,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "↗",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}