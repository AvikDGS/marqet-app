package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryData.allCategories
import com.example.model.NewsCategory
import com.example.AppBackgroundGradient
import com.example.GlassBackground
import com.example.GlassBorder
import com.example.TextSecondaryColor
import com.example.viewmodel.AuthViewModel

@Composable
fun CategorySelectionScreen(
    viewModel: AuthViewModel,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    val selectedCategories = remember { mutableStateListOf<NewsCategory>() }

    Box(modifier = Modifier.fillMaxSize().background(AppBackgroundGradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 80.dp, bottom = 48.dp)
        ) {
            Text(
                "What interests you?",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Select at least 3 categories to personalize your feed",
                color = TextSecondaryColor,
                fontSize = 16.sp,
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allCategories) { category ->
                    val isSelected = selectedCategories.contains(category)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .background(
                                if (isSelected) Color.White else GlassBackground,
                                RoundedCornerShape(24.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.White else GlassBorder,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable {
                                if (isSelected) selectedCategories.remove(category)
                                else selectedCategories.add(category)
                            }
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                category.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                category.name,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color(0xFF6B4EE6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription="Selected", tint=Color.White, modifier=Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onSkip) {
                    Text("Skip", color = TextSecondaryColor, fontSize = 16.sp)
                }
                
                val enabled = selectedCategories.size >= 3
                Button(
                    onClick = {
                        // In a real app we would save this to firestore
                        onContinue()
                    },
                    modifier = Modifier.height(56.dp).width(160.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, disabledContainerColor = GlassBackground),
                    shape = RoundedCornerShape(16.dp),
                    enabled = enabled
                ) {
                    Text("Continue", color = if (enabled) Color.Black else TextSecondaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
