package com.example.geotracker.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.model.SessionSummary
import com.example.geotracker.repository.LocationRepository
import com.example.geotracker.repository.SelectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.remove

@HiltViewModel
class SharedViewModel @Inject constructor(private val selectionManager: SelectionManager,
    private val repo: LocationRepository
) : ViewModel() {

    // backing mutable + public read-only selection flow
//    private val _selectedSessionId = MutableStateFlow<Set<Long?>>(emptySet())
    val selectedSessionId: StateFlow<Set<Long?>> = selectionManager.selectedSessionId

    val sessionSummary: StateFlow<List<SessionSummary>> =
        repo.getAllSessionSummariesFlow().map {
            it
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    /** Toggle selection: select id if not selected, otherwise clear selection. */
    fun toggleSessionSelection(sessionId: Long) {
        selectionManager.toggleSessionSelection(sessionId)
    }

    fun toggleSessionSelection(
        sessionId: Long,
        beforeCommit: suspend (Set<Long>) -> Unit
    ) {
        selectionManager.toggleSessionSelection(sessionId, beforeCommit)
    }
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repo.deleteSession(sessionId)
        }
    }
    /** Optional helper to clear selection */
    fun clearSelection() {
        selectionManager.clearSelection()
    }
}