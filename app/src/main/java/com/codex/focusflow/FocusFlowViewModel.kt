package com.codex.focusflow

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.max

enum class AppTab(val title: String) {
    ITEMS("物品"),
    CATEGORIES("分类"),
    RANKING("排行"),
    SETTINGS("设置")
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class UiStylePreset(val title: String) {
    SPACE("深空玻璃"),
    PEARL("银白极简"),
    AURORA("流光渐变"),
    HELLO_KITTY("HelloKitty"),
    NOTION("Notion")
}

enum class UsageFrequency(val label: String, val monthlyUses: Double) {
    DAILY("每天", 30.0),
    WEEKLY("每周", 4.0),
    MONTHLY("每月", 1.0),
    QUARTERLY("每季", 0.33),
    YEARLY("每年", 0.08),
    RARELY("很少", 0.03)
}

fun usageLabel(monthlyUses: Double): String {
    return when {
        monthlyUses >= 29.0 -> "每天"
        monthlyUses in 3.5..4.5 -> "每周"
        monthlyUses in 0.8..1.2 -> "每月"
        monthlyUses in 0.25..0.45 -> "每季"
        monthlyUses in 0.05..0.12 -> "每年"
        monthlyUses <= 0.04 -> "很少"
        monthlyUses % 1.0 == 0.0 -> "每月 ${monthlyUses.toInt()} 次"
        else -> "每月 ${"%.1f".format(monthlyUses)} 次"
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val uiStylePreset: UiStylePreset = UiStylePreset.SPACE
)

data class Category(
    val id: String,
    val name: String,
    val sortOrder: Int = 0
)

data class InventoryItem(
    val id: String,
    val name: String,
    val photoUri: String? = null,
    val price: Double = 0.0,
    val purchaseDate: LocalDate = LocalDate.now(),
    val expectedMonthlyUses: Double = UsageFrequency.MONTHLY.monthlyUses,
    val expiryDate: LocalDate? = null,
    val note: String = "",
    val categoryId: String,
    val room: String = "",
    val storageSpot: String = "",
    val rating: Int = 3,
    val invalid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CategoryStats(
    val category: Category,
    val itemCount: Int,
    val totalValue: Double,
    val totalDailyCost: Double
)

data class FocusFlowState(
    val currentTab: AppTab = AppTab.ITEMS,
    val settings: AppSettings = AppSettings(),
    val items: List<InventoryItem> = emptyList(),
    val categories: List<Category> = defaultCategories(),
    val searchQuery: String = "",
    val categoryFilterId: String? = null,
    val roomFilter: String? = null
) {
    val filteredItems: List<InventoryItem>
        get() = items.filter { item ->
            val query = searchQuery.trim()
            val matchesQuery = query.isEmpty() ||
                item.name.contains(query, ignoreCase = true) ||
                item.note.contains(query, ignoreCase = true) ||
                item.room.contains(query, ignoreCase = true) ||
                item.storageSpot.contains(query, ignoreCase = true) ||
                categoryName(item.categoryId).contains(query, ignoreCase = true)
            val matchesCategory = categoryFilterId == null || item.categoryId == categoryFilterId
            val matchesRoom = roomFilter == null || item.room == roomFilter
            matchesQuery && matchesCategory && matchesRoom
        }.sortedByDescending { it.updatedAt }

    val rooms: List<String>
        get() = items.map { it.room.trim() }.filter { it.isNotEmpty() }.distinct().sorted()

    val storageSpots: List<String>
        get() = items.map { it.storageSpot.trim() }.filter { it.isNotEmpty() }.distinct().sorted()

    val totalValue: Double
        get() = items.sumOf { it.price }

    val totalDailyCost: Double
        get() = items.sumOf { dailyCost(it) }

    fun categoryName(categoryId: String): String {
        return categories.firstOrNull { it.id == categoryId }?.name ?: "未分类"
    }

    fun categoryStats(): List<CategoryStats> {
        return categories.mapNotNull { category ->
            val categoryItems = items.filter { it.categoryId == category.id }
            if (categoryItems.isEmpty()) {
                null
            } else {
                CategoryStats(
                    category = category,
                    itemCount = categoryItems.size,
                    totalValue = categoryItems.sumOf { it.price },
                    totalDailyCost = categoryItems.sumOf { dailyCost(it) }
                )
            }
        }.sortedByDescending { it.totalValue }
    }

    fun rankedItems(): List<InventoryItem> {
        return items.sortedByDescending { practicalityScore(it) }
    }
}

class FocusFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(FocusFlowState())
    val state: StateFlow<FocusFlowState> = _state.asStateFlow()

    init {
        loadPersistedState()
    }

    fun setCurrentTab(tab: AppTab) {
        _state.update { it.copy(currentTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun updateCategoryFilter(categoryId: String?) {
        _state.update { it.copy(categoryFilterId = categoryId) }
    }

    fun updateRoomFilter(room: String?) {
        _state.update { it.copy(roomFilter = room) }
    }

    fun clearFilters() {
        _state.update { it.copy(searchQuery = "", categoryFilterId = null, roomFilter = null) }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        _state.update { it.copy(settings = it.settings.copy(themeMode = themeMode)) }
        persistState()
    }

    fun updateUiStyle(stylePreset: UiStylePreset) {
        _state.update { it.copy(settings = it.settings.copy(uiStylePreset = stylePreset)) }
        persistState()
    }

    fun saveItem(
        id: String?,
        name: String,
        photoUri: String?,
        priceText: String,
        purchaseDateText: String,
        usageFrequency: UsageFrequency,
        monthlyUsesText: String,
        expiryDateText: String,
        note: String,
        categoryName: String,
        room: String,
        storageSpot: String,
        rating: Int
    ): Boolean {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) return false

        val price = priceText.trim().toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val purchaseDate = parseDateOrNull(purchaseDateText) ?: LocalDate.now()
        val expiryDate = parseDateOrNull(expiryDateText)
        val monthlyUses = monthlyUsesText.trim().toDoubleOrNull()
            ?.coerceAtLeast(0.01)
            ?: usageFrequency.monthlyUses
        val now = System.currentTimeMillis()

        _state.update { current ->
            val isInvalid = categoryName.trim() == INVALID_CATEGORY_NAME
            val categoryResult = ensureCategory(current.categories, if (isInvalid) INVALID_CATEGORY_NAME else categoryName)
            val category = categoryResult.category
            val existing = current.items.firstOrNull { it.id == id }
            val item = InventoryItem(
                id = existing?.id ?: UUID.randomUUID().toString(),
                name = normalizedName,
                photoUri = photoUri?.takeIf { it.isNotBlank() },
                price = price,
                purchaseDate = purchaseDate,
                expectedMonthlyUses = monthlyUses,
                expiryDate = expiryDate,
                note = note.trim(),
                categoryId = category.id,
                room = room.trim(),
                storageSpot = storageSpot.trim(),
                rating = rating.coerceIn(1, 5),
                invalid = isInvalid || existing?.invalid == true,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
            current.copy(
                categories = categoryResult.categories,
                items = if (existing == null) {
                    current.items + item
                } else {
                    current.items.map { if (it.id == existing.id) item else it }
                }
            )
        }
        persistState()
        return true
    }

    fun deleteItem(itemId: String) {
        _state.update { current ->
            current.copy(items = current.items.filterNot { it.id == itemId })
        }
        persistState()
    }

    fun deleteItems(itemIds: Set<String>) {
        if (itemIds.isEmpty()) return
        _state.update { current ->
            current.copy(items = current.items.filterNot { it.id in itemIds })
        }
        persistState()
    }

    fun markItemInvalid(itemId: String) {
        _state.update { current ->
            val categoryResult = ensureCategory(current.categories, INVALID_CATEGORY_NAME)
            current.copy(
                categories = categoryResult.categories,
                items = current.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(
                            categoryId = categoryResult.category.id,
                            invalid = true,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else {
                        item
                    }
                }
            )
        }
        persistState()
    }

    fun markItemsInvalid(itemIds: Set<String>) {
        if (itemIds.isEmpty()) return
        _state.update { current ->
            val categoryResult = ensureCategory(current.categories, INVALID_CATEGORY_NAME)
            current.copy(
                categories = categoryResult.categories,
                items = current.items.map { item ->
                    if (item.id in itemIds) {
                        item.copy(
                            categoryId = categoryResult.category.id,
                            invalid = true,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else {
                        item
                    }
                }
            )
        }
        persistState()
    }

    fun updateItemsCategory(itemIds: Set<String>, categoryName: String) {
        if (itemIds.isEmpty()) return
        _state.update { current ->
            val categoryResult = ensureCategory(current.categories, categoryName)
            current.copy(
                categories = categoryResult.categories,
                items = current.items.map { item ->
                    if (item.id in itemIds) {
                        item.copy(categoryId = categoryResult.category.id, updatedAt = System.currentTimeMillis())
                    } else {
                        item
                    }
                }
            )
        }
        persistState()
    }

    fun exportDataJson(): String {
        val snapshot = _state.value
        return JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("settings", settingsToJson(snapshot.settings))
            put("categories", JSONArray(snapshot.categories.map(::categoryToJson)))
            put("items", JSONArray(snapshot.items.map(::itemToJson)))
        }.toString(2)
    }

    fun restoreDataJson(json: String): Boolean {
        return runCatching {
            val data = JSONObject(json)
            val categories = categoriesFromJson(data.getJSONArray("categories").toString())
            val previousCategories = _state.value.categories
            _state.update { current ->
                current.copy(
                    settings = data.optJSONObject("settings")?.let { settingsFromJson(it.toString()) } ?: current.settings,
                    categories = categories.ifEmpty { previousCategories },
                    items = itemsFromJson(data.getJSONArray("items").toString())
                )
            }
            persistState()
        }.isSuccess
    }

    private fun loadPersistedState() {
        val itemsJson = preferences.getString(KEY_ITEMS, null)
        val categoriesJson = preferences.getString(KEY_CATEGORIES, null)
        val settingsJson = preferences.getString(KEY_SETTINGS, null)

        _state.update { current ->
            current.copy(
                settings = settingsJson?.let(::settingsFromJson) ?: current.settings,
                categories = categoriesJson?.let(::categoriesFromJson)?.ifEmpty { defaultCategories() } ?: current.categories,
                items = itemsJson?.let(::itemsFromJson) ?: current.items
            )
        }
    }

    private fun persistState() {
        val snapshot = _state.value
        preferences.edit()
            .putString(KEY_SETTINGS, settingsToJson(snapshot.settings).toString())
            .putString(KEY_CATEGORIES, JSONArray(snapshot.categories.map(::categoryToJson)).toString())
            .putString(KEY_ITEMS, JSONArray(snapshot.items.map(::itemToJson)).toString())
            .apply()
    }

    private fun ensureCategory(categories: List<Category>, categoryName: String): CategoryResult {
        val normalizedName = categoryName.trim().ifEmpty { "未分类" }
        val existing = categories.firstOrNull { it.name.equals(normalizedName, ignoreCase = true) }
        if (existing != null) return CategoryResult(existing, categories)

        val category = Category(
            id = "category_${UUID.randomUUID()}",
            name = normalizedName,
            sortOrder = categories.size
        )
        return CategoryResult(category, categories + category)
    }

    private fun settingsToJson(settings: AppSettings): JSONObject = JSONObject().apply {
        put("themeMode", settings.themeMode.name)
        put("uiStylePreset", settings.uiStylePreset.name)
    }

    private fun settingsFromJson(json: String): AppSettings {
        val data = JSONObject(json)
        return AppSettings(
            themeMode = enumValueOrDefault(data.optString("themeMode"), ThemeMode.SYSTEM),
            uiStylePreset = enumValueOrDefault(data.optString("uiStylePreset"), UiStylePreset.SPACE)
        )
    }

    private fun categoryToJson(category: Category): JSONObject = JSONObject().apply {
        put("id", category.id)
        put("name", category.name)
        put("sortOrder", category.sortOrder)
    }

    private fun categoriesFromJson(json: String): List<Category> {
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            Category(
                id = item.optString("id"),
                name = item.optString("name", "未分类"),
                sortOrder = item.optInt("sortOrder", index)
            )
        }.filter { it.id.isNotBlank() }.sortedBy { it.sortOrder }
    }

    private fun itemToJson(item: InventoryItem): JSONObject = JSONObject().apply {
        put("id", item.id)
        put("name", item.name)
        put("photoUri", item.photoUri)
        put("price", item.price)
        put("purchaseDate", item.purchaseDate.toString())
        put("expectedMonthlyUses", item.expectedMonthlyUses)
        put("expiryDate", item.expiryDate?.toString())
        put("note", item.note)
        put("categoryId", item.categoryId)
        put("room", item.room)
        put("storageSpot", item.storageSpot)
        put("rating", item.rating)
        put("invalid", item.invalid)
        put("createdAt", item.createdAt)
        put("updatedAt", item.updatedAt)
    }

    private fun itemsFromJson(json: String): List<InventoryItem> {
        val array = JSONArray(json)
        val fallbackCategory = _state.value.categories.firstOrNull()?.id ?: defaultCategories().first().id
        return List(array.length()) { index ->
            val data = array.getJSONObject(index)
            InventoryItem(
                id = data.optString("id", UUID.randomUUID().toString()),
                name = data.optString("name", "未命名物品"),
                photoUri = data.optString("photoUri").takeIf { it.isNotBlank() && it != "null" },
                price = data.optDouble("price", 0.0),
                purchaseDate = parseDateOrNull(data.optString("purchaseDate")) ?: LocalDate.now(),
                expectedMonthlyUses = if (data.has("expectedMonthlyUses")) {
                    data.optDouble("expectedMonthlyUses", UsageFrequency.MONTHLY.monthlyUses)
                } else {
                    enumValueOrDefault(
                        data.optString("expectedUsageFrequency"),
                        UsageFrequency.MONTHLY
                    ).monthlyUses
                },
                expiryDate = parseDateOrNull(data.optString("expiryDate")),
                note = data.optString("note"),
                categoryId = data.optString("categoryId", fallbackCategory).ifBlank { fallbackCategory },
                room = data.optString("room"),
                storageSpot = data.optString("storageSpot"),
                rating = data.optInt("rating", 3).coerceIn(1, 5),
                invalid = data.optBoolean("invalid", false),
                createdAt = data.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = data.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    }

    private data class CategoryResult(
        val category: Category,
        val categories: List<Category>
    )

    companion object {
        private const val PREFS_NAME = "home_inventory_prefs"
        private const val KEY_SETTINGS = "settings"
        private const val KEY_CATEGORIES = "categories"
        private const val KEY_ITEMS = "items"
        const val INVALID_CATEGORY_NAME = "失效物品"
    }
}

fun holdingDays(item: InventoryItem, today: LocalDate = LocalDate.now()): Long {
    return max(1L, ChronoUnit.DAYS.between(item.purchaseDate, today))
}

fun dailyCost(item: InventoryItem, today: LocalDate = LocalDate.now()): Double {
    return item.price / holdingDays(item, today).toDouble()
}

fun practicalityScore(item: InventoryItem): Double {
    val frequencyPenalty = 30.0 / item.expectedMonthlyUses.coerceAtLeast(0.03)
    val ratingPenalty = (6 - item.rating).coerceAtLeast(1)
    return item.price * frequencyPenalty * ratingPenalty
}

fun parseDateOrNull(value: String): LocalDate? {
    val normalized = value.trim()
    if (normalized.isEmpty() || normalized == "null") return null
    return runCatching { LocalDate.parse(normalized) }.getOrNull()
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T {
    return runCatching { enumValueOf<T>(value) }.getOrDefault(default)
}

private fun defaultCategories(): List<Category> {
    return listOf(
        Category(id = "category_uncategorized", name = "未分类", sortOrder = 0),
        Category(id = "category_digital", name = "数码", sortOrder = 1),
        Category(id = "category_clothing", name = "衣物", sortOrder = 2),
        Category(id = "category_home", name = "家居", sortOrder = 3),
        Category(id = "category_books", name = "书籍", sortOrder = 4),
        Category(id = "category_tools", name = "工具", sortOrder = 5),
        Category(id = "category_invalid", name = FocusFlowViewModel.INVALID_CATEGORY_NAME, sortOrder = 6)
    )
}
