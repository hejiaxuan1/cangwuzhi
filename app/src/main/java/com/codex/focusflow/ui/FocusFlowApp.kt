@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)

package com.codex.focusflow.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codex.focusflow.AppTab
import com.codex.focusflow.FocusFlowState
import com.codex.focusflow.FocusFlowViewModel
import com.codex.focusflow.FocusFlowViewModel.Companion.INVALID_CATEGORY_NAME
import com.codex.focusflow.InventoryItem
import com.codex.focusflow.ThemeMode
import com.codex.focusflow.UiStylePreset
import com.codex.focusflow.UsageFrequency
import com.codex.focusflow.dailyCost
import com.codex.focusflow.holdingDays
import com.codex.focusflow.practicalityScore
import com.codex.focusflow.ui.theme.FocusFlowTheme
import com.codex.focusflow.usageLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@Composable
fun FocusFlowApp(viewModel: FocusFlowViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FocusFlowTheme(settings = state.settings) {
        InventoryRoot(
            state = state,
            onTabChange = viewModel::setCurrentTab,
            onSearchChange = viewModel::updateSearchQuery,
            onCategoryFilterChange = viewModel::updateCategoryFilter,
            onRoomFilterChange = viewModel::updateRoomFilter,
            onClearFilters = viewModel::clearFilters,
            onSaveItem = viewModel::saveItem,
            onDeleteItem = viewModel::deleteItem,
            onDeleteItems = viewModel::deleteItems,
            onMarkItemInvalid = viewModel::markItemInvalid,
            onMarkItemsInvalid = viewModel::markItemsInvalid,
            onUpdateItemsCategory = viewModel::updateItemsCategory,
            onThemeModeChange = viewModel::updateThemeMode,
            onUiStyleChange = viewModel::updateUiStyle,
            exportDataJson = viewModel::exportDataJson,
            restoreDataJson = viewModel::restoreDataJson
        )
    }
}

@Composable
private fun InventoryRoot(
    state: FocusFlowState,
    onTabChange: (AppTab) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onRoomFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onSaveItem: (String?, String, String?, String, String, UsageFrequency, String, String, String, String, String, String, Int) -> Boolean,
    onDeleteItem: (String) -> Unit,
    onDeleteItems: (Set<String>) -> Unit,
    onMarkItemInvalid: (String) -> Unit,
    onMarkItemsInvalid: (Set<String>) -> Unit,
    onUpdateItemsCategory: (Set<String>, String) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUiStyleChange: (UiStylePreset) -> Unit,
    exportDataJson: () -> String,
    restoreDataJson: (String) -> Boolean
) {
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var showBulkCategory by rememberSaveable { mutableStateOf(false) }
    val selectionMode = selectedIds.isNotEmpty()
    val darkTheme = useDarkTheme(state.settings.themeMode)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("藏物志", fontWeight = FontWeight.Bold)
                        Text(
                            if (selectionMode) "已选择 ${selectedIds.size} 件" else "家庭资产、位置与实用性",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (selectionMode) {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Outlined.Close, contentDescription = "退出批量选择")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            if (!selectionMode && state.currentTab != AppTab.SETTINGS) {
                PulsingFab {
                    editingItem = null
                    showEditor = true
                }
            }
        },
        bottomBar = {
            if (selectionMode) {
                BulkActionBar(
                    count = selectedIds.size,
                    onChangeCategory = { showBulkCategory = true },
                    onMarkInvalid = {
                        onMarkItemsInvalid(selectedIds)
                        selectedIds = emptySet()
                    },
                    onDelete = {
                        onDeleteItems(selectedIds)
                        selectedIds = emptySet()
                    }
                )
            } else {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)) {
                    listOf(
                        AppTab.ITEMS to Icons.AutoMirrored.Outlined.ListAlt,
                        AppTab.CATEGORIES to Icons.Outlined.Category,
                        AppTab.RANKING to Icons.Outlined.Leaderboard,
                        AppTab.SETTINGS to Icons.Outlined.Settings
                    ).forEach { (tab, icon) ->
                        NavigationBarItem(
                            selected = state.currentTab == tab,
                            onClick = { onTabChange(tab) },
                            icon = { Icon(icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackground(state.settings.uiStylePreset, darkTheme))
                .padding(padding)
        ) {
            when (state.currentTab) {
                AppTab.ITEMS -> ItemsScreen(
                    state = state,
                    selectedIds = selectedIds,
                    selectionMode = selectionMode,
                    onSearchChange = onSearchChange,
                    onCategoryFilterChange = onCategoryFilterChange,
                    onRoomFilterChange = onRoomFilterChange,
                    onClearFilters = onClearFilters,
                    onAddItem = {
                        editingItem = null
                        showEditor = true
                    },
                    onEditItem = {
                        editingItem = it
                        showEditor = true
                    },
                    onLongDelete = { pendingDelete = it },
                    onToggleSelected = { item ->
                        selectedIds = if (item.id in selectedIds) selectedIds - item.id else selectedIds + item.id
                    }
                )

                AppTab.CATEGORIES -> CategoriesScreen(state = state)
                AppTab.RANKING -> RankingScreen(
                    state = state,
                    onEditItem = {
                        editingItem = it
                        showEditor = true
                    }
                )

                AppTab.SETTINGS -> SettingsScreen(
                    state = state,
                    onThemeModeChange = onThemeModeChange,
                    onUiStyleChange = onUiStyleChange,
                    exportDataJson = exportDataJson,
                    restoreDataJson = restoreDataJson
                )
            }
        }
    }

    if (showEditor) {
        ItemEditorDialog(
            item = editingItem,
            state = state,
            onDismiss = {
                showEditor = false
            },
            onDelete = { item ->
                onDeleteItem(item.id)
                showEditor = false
            },
            onMarkInvalid = { item ->
                onMarkItemInvalid(item.id)
                showEditor = false
            },
            onSave = { id, name, photoUri, price, purchaseDate, frequency, monthlyUses, expiryDate, note, category, room, spot, rating ->
                if (onSaveItem(id, name, photoUri, price, purchaseDate, frequency, monthlyUses, expiryDate, note, category, room, spot, rating)) {
                    showEditor = false
                }
            }
        )
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除物品") },
            text = { Text("确定删除「${item.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteItem(item.id)
                        pendingDelete = null
                    }
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("取消") } }
        )
    }

    if (showBulkCategory) {
        WheelPickerDialog(
            title = "批量更改分类",
            options = state.categories.map { it.name }.ifEmpty { listOf("未分类") },
            initialValue = state.categories.firstOrNull()?.name ?: "未分类",
            onDismiss = { showBulkCategory = false },
            onConfirm = { category ->
                onUpdateItemsCategory(selectedIds, category)
                selectedIds = emptySet()
                showBulkCategory = false
            }
        )
    }
}

@Composable
private fun ItemsScreen(
    state: FocusFlowState,
    selectedIds: Set<String>,
    selectionMode: Boolean,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onRoomFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (InventoryItem) -> Unit,
    onLongDelete: (InventoryItem) -> Unit,
    onToggleSelected: (InventoryItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AssetHeader(
                totalValue = state.totalValue,
                totalDailyCost = state.totalDailyCost,
                itemCount = state.items.size
            )
        }
        item {
            SearchAndFilters(
                state = state,
                onSearchChange = onSearchChange,
                onCategoryFilterChange = onCategoryFilterChange,
                onRoomFilterChange = onRoomFilterChange,
                onClearFilters = onClearFilters
            )
        }
        if (state.filteredItems.isEmpty()) {
            item { EmptyState(onAddItem = onAddItem) }
        } else {
            items(state.filteredItems, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    categoryName = state.categoryName(item.categoryId),
                    selected = item.id in selectedIds,
                    selectionMode = selectionMode,
                    onClick = {
                        if (selectionMode) onToggleSelected(item) else onEditItem(item)
                    },
                    onLongClick = {
                        if (selectionMode) onToggleSelected(item) else onLongDelete(item)
                    },
                    onSelectionClick = { onToggleSelected(item) }
                )
            }
        }
    }
}

@Composable
private fun AssetHeader(totalValue: Double, totalDailyCost: Double, itemCount: Int) {
    GlassSurface {
        Column(Modifier.padding(18.dp)) {
            Text("家庭资产概览", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeaderMetric("总资产", formatCurrency(totalValue), Modifier.weight(1f))
                HeaderMetric("日均", "${formatCurrency(totalDailyCost)}/天", Modifier.weight(1f))
                HeaderMetric("物品", itemCount.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeaderMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f))
            .padding(12.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Text(value, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SearchAndFilters(
    state: FocusFlowState,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onRoomFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    var showCategoryFilter by rememberSaveable { mutableStateOf(false) }
    var showRoomFilter by rememberSaveable { mutableStateOf(false) }

    GlassSurface {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                label = { Text("搜索物品、房间、备注") },
                shape = RoundedCornerShape(18.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showCategoryFilter = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(state.categoryFilterId?.let(state::categoryName) ?: "分类")
                }
                OutlinedButton(onClick = { showRoomFilter = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(state.roomFilter ?: "房间")
                }
                IconButton(onClick = onClearFilters) {
                    Icon(Icons.Outlined.Close, contentDescription = "清除筛选")
                }
            }
        }
    }

    if (showCategoryFilter) {
        OptionGridDialog(
            title = "筛选分类",
            options = listOf("全部") + state.categories.map { it.name },
            selected = state.categoryFilterId?.let(state::categoryName) ?: "全部",
            onDismiss = { showCategoryFilter = false },
            onSelect = { value ->
                onCategoryFilterChange(state.categories.firstOrNull { it.name == value }?.id)
                showCategoryFilter = false
            }
        )
    }
    if (showRoomFilter) {
        OptionGridDialog(
            title = "筛选房间",
            options = listOf("全部") + state.rooms,
            selected = state.roomFilter ?: "全部",
            onDismiss = { showRoomFilter = false },
            onSelect = { value ->
                onRoomFilterChange(value.takeIf { it != "全部" })
                showRoomFilter = false
            }
        )
    }
}

@Composable
private fun ItemCard(
    item: InventoryItem,
    categoryName: String,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectionClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = { onSelectionClick() })
            }
            ItemPhoto(uri = item.photoUri, modifier = Modifier.size(68.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    RatingStars(item.rating)
                }
                Text(
                    "${if (item.invalid) "失效 · " else ""}$categoryName · ${item.room.ifBlank { "未设房间" }} / ${item.storageSpot.ifBlank { "未设位置" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${usageLabel(item.expectedMonthlyUses)} · 持有 ${holdingDays(item)} 天",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(formatCurrency(item.price), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${formatCurrency(dailyCost(item))}/天", color = Color(0xFFE2B665), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CategoriesScreen(state: FocusFlowState) {
    val stats = state.categoryStats()
    var expandedIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    LazyColumn(
        contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SectionTitle("分类资产") }
        if (stats.isEmpty()) {
            item { EmptyState(message = "还没有可统计的分类", onAddItem = null) }
        } else {
            items(stats, key = { it.category.id }) { stat ->
                GlassSurface(modifier = Modifier.combinedClickable(onClick = {
                    expandedIds = if (stat.category.id in expandedIds) expandedIds - stat.category.id else expandedIds + stat.category.id
                })) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(stat.category.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("${stat.itemCount} 件 ${if (stat.category.id in expandedIds) "收起" else "展开"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SmallMetric("总资产", formatCurrency(stat.totalValue), Modifier.weight(1f))
                            SmallMetric("日均", "${formatCurrency(stat.totalDailyCost)}/天", Modifier.weight(1f))
                        }
                        if (stat.category.id in expandedIds) {
                            state.items
                                .filter { it.categoryId == stat.category.id }
                                .sortedByDescending { it.updatedAt }
                                .forEach { item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(formatCurrency(item.price), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingScreen(state: FocusFlowState, onEditItem: (InventoryItem) -> Unit) {
    val rankedItems = state.rankedItems()
    LazyColumn(
        contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SectionTitle("实用性风险排行") }
        if (rankedItems.isEmpty()) {
            item { EmptyState(message = "添加物品后会生成排行", onAddItem = null) }
        } else {
            items(rankedItems, key = { it.id }) { item ->
                RankingCard(
                    rank = rankedItems.indexOf(item) + 1,
                    item = item,
                    categoryName = state.categoryName(item.categoryId),
                    topScore = practicalityScore(rankedItems.first()).coerceAtLeast(1.0),
                    onClick = { onEditItem(item) }
                )
            }
        }
    }
}

@Composable
private fun RankingCard(rank: Int, item: InventoryItem, categoryName: String, topScore: Double, onClick: () -> Unit) {
    val progress = (practicalityScore(item) / topScore).toFloat().coerceIn(0.06f, 1f)
    GlassSurface(modifier = Modifier.combinedClickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#$rank", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "$categoryName · ${formatCurrency(item.price)} · ${usageLabel(item.expectedMonthlyUses)} · 评分 ${item.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF7AA7FF), Color(0xFFB889FF), Color(0xFFFFD36F))))
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: FocusFlowState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUiStyleChange: (UiStylePreset) -> Unit,
    exportDataJson: () -> String,
    restoreDataJson: (String) -> Boolean
) {
    val context = LocalContext.current
    var pendingExportJson by remember { mutableStateOf("") }
    var restoreMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pendingExportJson.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val json = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            }.getOrNull()
            restoreMessage = if (json != null && restoreDataJson(json)) "恢复成功" else "恢复失败，请检查 JSON 文件"
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SectionTitle("设置") }
        item {
            GlassSurface {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingHeader(icon = Icons.Outlined.Style, title = "界面风格")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        UiStylePreset.values().forEach { style ->
                            FilterChip(
                                selected = state.settings.uiStylePreset == style,
                                onClick = { onUiStyleChange(style) },
                                label = { Text(style.title) }
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingHeader(icon = Icons.Outlined.Settings, title = "系统模式")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.values().forEach { mode ->
                            FilterChip(
                                selected = state.settings.themeMode == mode,
                                onClick = { onThemeModeChange(mode) },
                                label = {
                                    Text(
                                        when (mode) {
                                            ThemeMode.SYSTEM -> "跟随系统"
                                            ThemeMode.LIGHT -> "亮色"
                                            ThemeMode.DARK -> "深色"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        item {
            GlassSurface {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingHeader(icon = Icons.Outlined.Download, title = "数据备份与恢复")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                pendingExportJson = exportDataJson()
                                exportLauncher.launch("藏物志-backup-${System.currentTimeMillis()}.json")
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("备份 JSON") }
                        OutlinedButton(
                            onClick = { restoreLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("恢复")
                        }
                    }
                    restoreMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            GlassSurface {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingHeader(icon = Icons.Outlined.CheckCircle, title = "开源协议")
                    Text("MIT License", fontWeight = FontWeight.Bold)
                    Text("你可以自由使用、修改和分发本项目，需保留版权和协议声明。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ItemEditorDialog(
    item: InventoryItem?,
    state: FocusFlowState,
    onDismiss: () -> Unit,
    onDelete: (InventoryItem) -> Unit,
    onMarkInvalid: (InventoryItem) -> Unit,
    onSave: (String?, String, String?, String, String, UsageFrequency, String, String, String, String, String, String, Int) -> Unit
) {
    val context = LocalContext.current
    var name by rememberSaveable(item?.id) { mutableStateOf(item?.name.orEmpty()) }
    var photoUri by rememberSaveable(item?.id) { mutableStateOf(item?.photoUri) }
    var price by rememberSaveable(item?.id) { mutableStateOf(item?.price?.takeIf { it > 0.0 }?.toPlainString().orEmpty()) }
    var purchaseDate by rememberSaveable(item?.id) { mutableStateOf((item?.purchaseDate ?: LocalDate.now()).toString()) }
    var usageFrequency by rememberSaveable(item?.id) { mutableStateOf(frequencyFromMonthlyUses(item?.expectedMonthlyUses)) }
    var monthlyUses by rememberSaveable(item?.id) { mutableStateOf(item?.expectedMonthlyUses?.toPlainString().orEmpty()) }
    var expiryDate by rememberSaveable(item?.id) { mutableStateOf(item?.expiryDate?.toString().orEmpty()) }
    var note by rememberSaveable(item?.id) { mutableStateOf(item?.note.orEmpty()) }
    var categoryName by rememberSaveable(item?.id) {
        mutableStateOf(
            item?.let { state.categoryName(it.categoryId) }
                ?: state.categories.firstOrNull()?.name.orEmpty()
        )
    }
    var room by rememberSaveable(item?.id) { mutableStateOf(item?.room.orEmpty()) }
    var storageSpot by rememberSaveable(item?.id) { mutableStateOf(item?.storageSpot.orEmpty()) }
    var rating by rememberSaveable(item?.id) { mutableStateOf(item?.rating ?: 3) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) photoUri = pendingCameraUri?.toString()
    }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "新增物品" else "编辑物品") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ItemPhoto(uri = photoUri, modifier = Modifier.size(118.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val uri = createImageUri(context)
                            pendingCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("拍照")
                    }
                    OutlinedButton(onClick = { documentLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("相册")
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("价格") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    DatePickerField(label = "购入日期", value = purchaseDate, onValueChange = { purchaseDate = it }, modifier = Modifier.weight(1f))
                }
                DatePickerField(label = "失效日期", value = expiryDate, onValueChange = { expiryDate = it }, allowEmpty = true, modifier = Modifier.fillMaxWidth())
                WheelPickerField(
                    label = "分类",
                    value = categoryName,
                    options = state.categories.map { it.name }.ifEmpty { listOf("未分类") },
                    onValueChange = { categoryName = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WheelPickerField(
                        label = "房间",
                        value = room,
                        options = (state.rooms + listOf("客厅", "卧室", "厨房", "书房")).distinct(),
                        onValueChange = { room = it },
                        modifier = Modifier.weight(1f)
                    )
                    WheelPickerField(
                        label = "收纳位置",
                        value = storageSpot,
                        options = (state.storageSpots + listOf("柜子", "抽屉", "书架", "收纳箱")).distinct(),
                        onValueChange = { storageSpot = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("使用频率", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UsageFrequency.values().forEach { frequency ->
                        FilterChip(
                            selected = usageFrequency == frequency,
                            onClick = {
                                usageFrequency = frequency
                                monthlyUses = frequency.monthlyUses.toPlainString()
                            },
                            label = { Text(frequency.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = monthlyUses,
                    onValueChange = { monthlyUses = it },
                    label = { Text("每月使用次数") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                )
                Text("体验评分", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { score ->
                        FilterChip(
                            selected = rating == score,
                            onClick = { rating = score },
                            label = { Text(score.toString()) },
                            leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注") }, minLines = 2, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(item?.id, name, photoUri, price, purchaseDate, usageFrequency, monthlyUses, expiryDate, note, categoryName, room, storageSpot, rating) }) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                if (item != null) {
                    TextButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("删除")
                    }
                    if (!item.invalid) {
                        TextButton(onClick = { onMarkInvalid(item) }) {
                            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("失效")
                        }
                    }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowEmpty: Boolean = false
) {
    var open by rememberSaveable { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = modifier.height(56.dp), shape = RoundedCornerShape(18.dp)) {
        Text(if (value.isBlank()) label else value, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
    }
    if (open) {
        val initialMillis = parseLocalDateMillis(value) ?: System.currentTimeMillis()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onValueChange(millisToLocalDate(it).toString()) }
                        open = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                Row {
                    if (allowEmpty) TextButton(onClick = { onValueChange(""); open = false }) { Text("清空") }
                    TextButton(onClick = { open = false }) { Text("取消") }
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun WheelPickerField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by rememberSaveable { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = modifier.height(56.dp), shape = RoundedCornerShape(18.dp)) {
        Text(value.ifBlank { label }, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
    }
    if (open) {
        WheelPickerDialog(
            title = label,
            options = options.ifEmpty { listOf("未分类") },
            initialValue = value,
            onDismiss = { open = false },
            onConfirm = {
                onValueChange(it)
                open = false
            }
        )
    }
}

@Composable
private fun WheelPickerDialog(
    title: String,
    options: List<String>,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val normalizedOptions = options.ifEmpty { listOf("未分类") }
    var selectedIndex by rememberSaveable(normalizedOptions.joinToString(), initialValue) {
        mutableStateOf(normalizedOptions.indexOf(initialValue).takeIf { it >= 0 } ?: 0)
    }
    var customValue by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 0
                            maxValue = normalizedOptions.lastIndex
                            displayedValues = normalizedOptions.toTypedArray()
                            value = selectedIndex
                            wrapSelectorWheel = normalizedOptions.size > 2
                            setOnValueChangedListener { _, _, newValue -> selectedIndex = newValue }
                        }
                    },
                    update = { picker ->
                        picker.minValue = 0
                        picker.maxValue = normalizedOptions.lastIndex
                        picker.displayedValues = normalizedOptions.toTypedArray()
                        picker.value = selectedIndex.coerceIn(0, normalizedOptions.lastIndex)
                    }
                )
                OutlinedTextField(
                    value = customValue,
                    onValueChange = { customValue = it },
                    label = { Text("手动添加") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(customValue.trim().ifEmpty { normalizedOptions[selectedIndex] }) }) {
                Text("确定")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun OptionGridDialog(
    title: String,
    options: List<String>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { onSelect(option) },
                        label = { Text(option) }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun BulkActionBar(count: Int, onChangeCategory: () -> Unit, onMarkInvalid: () -> Unit, onDelete: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), tonalElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("已选 $count 件", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onChangeCategory) { Text("改分类") }
            OutlinedButton(onClick = onMarkInvalid) { Text("失效") }
            OutlinedButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("删除")
            }
        }
    }
}

@Composable
private fun PulsingFab(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "fab-pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(animation = tween(1800), repeatMode = RepeatMode.Reverse),
        label = "fab-scale"
    )
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(58.dp)
            .scale(scale),
        containerColor = Color.Transparent,
        contentColor = Color.White,
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF7AA7FF), Color(0xFFB889FF), Color(0xFFFFD36F)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "新增物品", tint = Color.White)
        }
    }
}

@Composable
private fun ItemPhoto(uri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by rememberImageBitmap(context, uri)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap = bitmap!!, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun rememberImageBitmap(context: Context, uri: String?): State<ImageBitmap?> {
    return produceState<ImageBitmap?>(initialValue = null, uri) {
        value = if (uri.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }
}

@Composable
private fun GlassSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
        content = { content() }
    )
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyState(message: String = "还没有物品", onAddItem: (() -> Unit)?) {
    GlassSurface(modifier = Modifier.combinedClickable(onClick = { onAddItem?.invoke() })) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7AA7FF), Color(0xFFB889FF), Color(0xFFFFD36F)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "新增物品", tint = Color.White)
            }
            Text(message, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RatingStars(rating: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(5) { index ->
            Icon(
                Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFFFD36F) else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun SettingHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun useDarkTheme(themeMode: ThemeMode): Boolean {
    return when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

private fun appBackground(style: UiStylePreset, darkTheme: Boolean): Brush {
    return if (!darkTheme) {
        when (style) {
            UiStylePreset.SPACE -> Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEDEFF5), Color(0xFFF7F5EF)))
            UiStylePreset.PEARL -> Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF1F4F8), Color(0xFFE9EDF3)))
            UiStylePreset.AURORA -> Brush.linearGradient(listOf(Color(0xFFF8FAFF), Color(0xFFEAF0FF), Color(0xFFF7EEF9)))
            UiStylePreset.HELLO_KITTY -> Brush.verticalGradient(listOf(Color(0xFFFFF7FB), Color(0xFFFFE4EF), Color(0xFFFFF8E7)))
            UiStylePreset.NOTION -> Brush.verticalGradient(listOf(Color(0xFFFBFAF8), Color(0xFFF3F1EC), Color(0xFFEDEBE7)))
        }
    } else {
        when (style) {
            UiStylePreset.SPACE -> Brush.radialGradient(
                listOf(Color(0xFF313A4C), Color(0xFF0B0F17), Color(0xFF07090E)),
                radius = 1400f
            )
            UiStylePreset.PEARL -> Brush.verticalGradient(listOf(Color(0xFF1A1D24), Color(0xFF111318), Color(0xFF0B0D12)))
            UiStylePreset.AURORA -> Brush.linearGradient(listOf(Color(0xFF111827), Color(0xFF263E5A), Color(0xFF4A355F), Color(0xFF0F172A)))
            UiStylePreset.HELLO_KITTY -> Brush.verticalGradient(listOf(Color(0xFF281421), Color(0xFF1A1018), Color(0xFF120C12)))
            UiStylePreset.NOTION -> Brush.verticalGradient(listOf(Color(0xFF24231F), Color(0xFF141412), Color(0xFF0E0E0C)))
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "inventory")
    directory.mkdirs()
    val file = File(directory, "item_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun shareExport(context: Context, json: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_SUBJECT, "藏物志数据导出")
        putExtra(Intent.EXTRA_TEXT, json)
    }
    context.startActivity(Intent.createChooser(intent, "导出藏物志数据"))
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.CHINA).format(value)
}

private fun Double.toPlainString(): String {
    return if (this % 1.0 == 0.0) this.toLong().toString() else "%.2f".format(this).trimEnd('0').trimEnd('.')
}

private fun frequencyFromMonthlyUses(monthlyUses: Double?): UsageFrequency {
    if (monthlyUses == null) return UsageFrequency.MONTHLY
    return UsageFrequency.values().minBy { kotlin.math.abs(it.monthlyUses - monthlyUses) }
}

private fun parseLocalDateMillis(value: String): Long? {
    return runCatching {
        LocalDate.parse(value)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun millisToLocalDate(millis: Long): LocalDate {
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
}
