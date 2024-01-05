package zechs.zplex.utils.util

class DriveApiQueryBuilder {

    private val conditions = mutableListOf<String>()

    fun nameEquals(name: String): DriveApiQueryBuilder {
        addCondition("name", name)
        return this
    }

    fun mimeTypeEquals(mimeType: String): DriveApiQueryBuilder {
        addCondition("mimeType =", mimeType)
        return this
    }

    fun mimeTypeNotEquals(mimeType: String): DriveApiQueryBuilder {
        addCondition("mimeType !=", mimeType)
        return this
    }

    fun inParents(parentId: String): DriveApiQueryBuilder {
        addCondition("'$parentId' in parents")
        return this
    }

    fun trashed(isTrashed: Boolean): DriveApiQueryBuilder {
        addCondition("trashed", isTrashed)
        return this
    }

    fun build(): String {
        return conditions.joinToString(separator = " and ")
    }

    private fun addCondition(field: String, value: Any? = null) {
        val condition =
            "$field${if (value != null) "${if (field.endsWith("=")) "" else "="} ${formatValue(value)}" else ""}"
        conditions.add(condition)
    }

    private fun formatValue(value: Any): String {
        return when (value) {
            is String -> "'$value'"
            else -> value.toString()
        }
    }
}

