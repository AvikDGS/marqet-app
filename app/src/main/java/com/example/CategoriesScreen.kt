package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CategoryData
import com.example.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: NewsViewModel, onBackClick: () -> Unit) {
    val selectedCategoryIds by viewModel.settingsRepository.selectedCategoryIds.collectAsStateWithLifecycle()
    
    val groupedCategories = CategoryData.allCategories.groupBy { it.group }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("All Categories", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Select your favorite topics. They will appear right on your home screen.",
                    color = TextSecondaryColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            groupedCategories.forEach { (groupName, categoriesInGroup) ->
                item {
                    Text(
                        text = groupName.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(categoriesInGroup) { category ->
                    val isSelected = selectedCategoryIds.contains(category.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GlassBackground, RoundedCornerShape(12.dp))
                            .border(1.dp, if (isSelected) Color.White else GlassBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.settingsRepository.toggleCategorySelection(category.id)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            category.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else TextSecondaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = category.name,
                            color = if (isSelected) Color.White else TextSecondaryColor,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color.White)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp)) // padding for bottom nav
            }
        }
    }
}
