package com.example.plotter.data.repository

import com.example.plotter.data.auth.AuthManager
import com.example.plotter.domain.model.SavedGraph
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Репозиторий для работы с сохранёнными графиками в Firestore.
 * Все операции выполняются от имени текущего авторизованного пользователя.
 */
object GraphRepository {
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION = "graphs"

    /** Сохраняет новый график или обновляет существующий */
    suspend fun saveGraph(graph: SavedGraph): Result<String> = try {
        val ownerId = AuthManager.currentUser?.uid
            ?: return Result.failure(Exception("Not authenticated"))
        val docRef = if (graph.id.isEmpty()) {
            db.collection(COLLECTION).document()
        } else {
            db.collection(COLLECTION).document(graph.id)
        }
        val data = graph.copy(
            ownerId = ownerId,
            updatedAt = System.currentTimeMillis(),
            id = docRef.id
        )
        docRef.set(data).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Возвращает список графиков текущего пользователя */
    suspend fun getUserGraphs(): Result<List<SavedGraph>> = try {
        val ownerId = AuthManager.currentUser?.uid
            ?: return Result.failure(Exception("Not authenticated"))
        val snapshot = db.collection(COLLECTION)
            .whereEqualTo("ownerId", ownerId)
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        val graphs = snapshot.documents.mapNotNull { doc ->
            doc.toObject(SavedGraph::class.java)?.copy(id = doc.id)
        }
        Result.success(graphs)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Загружает график по ID */
    suspend fun loadGraph(graphId: String): Result<SavedGraph> = try {
        val doc = db.collection(COLLECTION).document(graphId).get().await()
        val graph = doc.toObject(SavedGraph::class.java)?.copy(id = doc.id)
        if (graph != null) Result.success(graph)
        else Result.failure(Exception("Graph not found"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Удаляет график по ID */
    suspend fun deleteGraph(graphId: String): Result<Unit> = try {
        db.collection(COLLECTION).document(graphId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Переименовывает график и обновляет timestamp */
    suspend fun renameGraph(graphId: String, newName: String): Result<Unit> = try {
        db.collection(COLLECTION).document(graphId).update(
            mapOf(
                "name" to newName,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}