package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AppBackgroundGradient
import com.example.GlassBackground
import com.example.GlassBorder
import com.example.TextSecondaryColor
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onSignOutComplete: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", color = Color.White) },
            text = { Text("Are you sure you want to sign out?", color = TextSecondaryColor) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    viewModel.signOut()
                    onSignOutComplete()
                }) {
                    Text("Sign Out", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E2E),
            titleContentColor = Color.White,
            textContentColor = TextSecondaryColor
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(AppBackgroundGradient).padding(padding)) {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // User Avatar and Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(80.dp).background(Color(0xFF6B4EE6), CircleShape), contentAlignment = Alignment.Center) {
                        Text((currentUser?.email?.take(1) ?: "U").uppercase(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(currentUser?.displayName ?: "User", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentUser?.email ?: "user@example.com", color = TextSecondaryColor, fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Library
                Text("Library", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileSectionItem(icon = Icons.Default.BookmarkBorder, title = "My Saved News", subtitle = "12 articles saved")
                ProfileSectionItem(icon = Icons.Default.Notifications, title = "My Tracked Stories", subtitle = "3 active trackers")
                ProfileSectionItem(icon = Icons.Default.History, title = "Reading History", subtitle = "Last 50 articles")

                Spacer(modifier = Modifier.height(32.dp))

                // Preferences
                Text("Preferences", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileSectionItem(icon = Icons.Default.Category, title = "Category Preferences", subtitle = "Edit your feed topics")
                ProfileSectionItem(icon = Icons.Default.NotificationsActive, title = "Notification Settings", subtitle = "Manage alerts per category")
                ProfileSectionItem(icon = Icons.Default.Language, title = "Language", subtitle = "English (US)")
                ProfileSectionItem(icon = Icons.Default.DarkMode, title = "Appearance", subtitle = "System Default")

                Spacer(modifier = Modifier.height(32.dp))

                // About
                Text("About", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileSectionItem(icon = Icons.Default.Info, title = "About Marqet", subtitle = "Version 1.0.0")
                ProfileSectionItem(icon = Icons.Default.PrivacyTip, title = "Privacy Policy", subtitle = "")

                Spacer(modifier = Modifier.height(48.dp))
                
                // Sign out / Delete
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showSignOutDialog = true }.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Sign Out", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red.copy(alpha=0.5f))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Delete Account", color = Color.Red.copy(alpha=0.5f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun ProfileSectionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(GlassBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, color = TextSecondaryColor, fontSize = 12.sp)
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextSecondaryColor, modifier = Modifier.size(16.dp))
    }
}
