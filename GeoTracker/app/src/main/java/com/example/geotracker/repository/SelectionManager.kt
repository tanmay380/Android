package com.example.geotracker.repository

import android.util.Log
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.utils.Utils.logCaller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectionManager @Inject constructor(
) {
    private val _runningSessionId = MutableStateFlow<Long?>(null)
    val runningSessionId = _runningSessionId

    // backing mutable + public read-only selection flow
    private val _selectedSessionId = MutableStateFlow<Set<Long>>(emptySet())
    val selectedSessionId: StateFlow<Set<Long>> = _selectedSessionId.asStateFlow()

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun toggleSessionSelection(
        sessionId: Long,
        beforeCommit: suspend (Set<Long>) -> Unit = {}
    ) {
        val newSelection: Set<Long> = _selectedSessionId.value.toMutableSet().apply {
            if (contains(sessionId)) remove(sessionId) else add(sessionId)
        }.toSet()

        logCaller()
        Log.d(TAG, "toggleSessionSelection: $newSelection")

        scope.launch {
            beforeCommit(newSelection)            // 🔥 UI update first
            _selectedSessionId.value = newSelection // then commit
            Log.d(TAG, "toggleSessionSelection SM : ${_selectedSessionId.value}")

        }

    }

    fun toggleSessionSelection(sessionId: Long) = toggleSessionSelection(sessionId) {}

    fun setSelectedSession(set: Set<Long>) {
        _selectedSessionId.value = set
    }

    fun setRunningSession(id: Long?) {
        _runningSessionId.value = id
        Log.d(TAG, "setRunningSession: $runningSessionId")
    }

    /** Optional helper to clear selection */
    fun clearSelection() {
        _selectedSessionId.value = emptySet()
    }
}