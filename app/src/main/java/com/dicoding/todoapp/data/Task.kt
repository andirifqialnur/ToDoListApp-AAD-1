package com.dicoding.todoapp.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dicoding.todoapp.utils.TABLE_NAME
import kotlinx.parcelize.Parcelize

//TODO 1 : Define a local database table using the schema in app/schema/tasks.json

@Parcelize
@Entity(tableName = TABLE_NAME)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val dueDateMillis: Long,
    @ColumnInfo(name = "completed")
    val isCompleted: Boolean = false
) : Parcelable
