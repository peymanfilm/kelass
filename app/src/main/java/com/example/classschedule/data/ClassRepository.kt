package com.example.classschedule.data

import kotlinx.coroutines.flow.Flow

/**
 * لایه Repository: واسط بین ViewModel و منبع داده (Room)
 */
class ClassRepository(private val classDao: ClassDao) {

    val allClasses: Flow<List<ClassSchedule>> = classDao.getAllClasses()

    suspend fun insert(classSchedule: ClassSchedule) = classDao.insert(classSchedule)

    suspend fun update(classSchedule: ClassSchedule) = classDao.update(classSchedule)

    suspend fun delete(classSchedule: ClassSchedule) = classDao.delete(classSchedule)
}
