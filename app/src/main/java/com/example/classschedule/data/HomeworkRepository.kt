package com.example.classschedule.data

import kotlinx.coroutines.flow.Flow

/**
 * لایه Repository تکالیف: واسط بین ViewModel و منبع داده (Room)
 */
class HomeworkRepository(private val homeworkDao: HomeworkDao) {

    val allHomework: Flow<List<Homework>> = homeworkDao.getAllHomework()

    suspend fun insert(homework: Homework) = homeworkDao.insert(homework)

    suspend fun update(homework: Homework) = homeworkDao.update(homework)

    suspend fun delete(homework: Homework) = homeworkDao.delete(homework)
}
