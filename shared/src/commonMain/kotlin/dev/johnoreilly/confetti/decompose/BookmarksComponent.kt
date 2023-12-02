package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.createCurrentLocalDateTimeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface BookmarksComponent {

    val uiState: Value<UiState>

    val isLoggedIn: Boolean

    sealed interface UiState
    data object Loading : UiState

    data object Error : UiState
    class Success(
        val bookmarks: Set<String>,
        val pastSessions: DateSessionsMap,
        val upcomingSessions: DateSessionsMap
    ) : UiState

    fun addBookmark(sessionId: String)
    fun removeBookmark(sessionId: String)
    fun onSessionClicked(id: String)
    fun onSignInClicked()
}

class DefaultBookmarksComponent(
    componentContext: ComponentContext,
    private val conference: String,
    private val user: User?,
    private val onSessionSelected: (id: String) -> Unit,
    private val onSignIn: () -> Unit,
) : BookmarksComponent, KoinComponent, ComponentContext by componentContext {

    private val repository: ConfettiRepository by inject()
    private val dateService: DateService by inject()
    private val coroutineScope = coroutineScope()

    private val sessionsComponent =
        SessionsSimpleComponent(
            componentContext = childContext("Sessions"),
            conference = conference,
            user = user,
        )

    private val loadedSessions = sessionsComponent
        .uiState
        .filterIsInstance<SessionsUiState.Success>()

    private val bookmarks: Flow<Set<String>> = loadedSessions.map { state -> state.bookmarks }

    private val currentDateTimeFlow = dateService
        .createCurrentLocalDateTimeFlow()

    private val sessions = loadedSessions
        .map { state ->
            state
                .sessionsByStartTimeList
                .flatMap { sessions -> sessions.values }
                .flatten()
        }
        .combine(bookmarks) { sessions, bookmarks ->
            sessions.filter { session -> session.id in bookmarks }
        }

    private val upcomingSessions: Flow<Map<LocalDateTime, List<SessionDetails>>> = sessions
        .combine(currentDateTimeFlow) { sessions, now ->
            sessions.filter { session ->
                session.endsAt >= now
            }.groupBy { it.startsAt }
        }

    private val pastSessions: Flow<Map<LocalDateTime, List<SessionDetails>>> = sessions
        .combine(currentDateTimeFlow) { sessions, now ->
            sessions.filter { session ->
                session.endsAt < now
            }.groupBy { it.startsAt }
        }

    override val isLoggedIn: Boolean = user != null

    override val uiState: Value<BookmarksComponent.UiState> = combine(
        sessionsComponent.uiState,
        bookmarks,
        pastSessions,
        upcomingSessions
    ) { state, bookmarks, pastSessions, upcomingSessions ->
        when (state) {
            SessionsUiState.Loading -> BookmarksComponent.Loading
            is SessionsUiState.Success ->
                BookmarksComponent.Success(
                    bookmarks = bookmarks,
                    pastSessions = pastSessions,
                    upcomingSessions = upcomingSessions
                )

            else -> BookmarksComponent.Error
        }
    }.asValue(initialValue = BookmarksComponent.Loading, lifecycle = lifecycle)

    override fun addBookmark(sessionId: String) {
        coroutineScope.launch {
            repository.addBookmark(conference, user?.uid, user, sessionId)
        }
    }

    override fun removeBookmark(sessionId: String) {
        coroutineScope.launch {
            repository.removeBookmark(conference, user?.uid, user, sessionId)
        }
    }

    override fun onSessionClicked(id: String) {
        onSessionSelected(id)
    }

    override fun onSignInClicked() {
        onSignIn()
    }
}
