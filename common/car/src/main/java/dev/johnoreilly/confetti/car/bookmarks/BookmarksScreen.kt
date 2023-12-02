package dev.johnoreilly.confetti.car.bookmarks

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.car.sessions.details.SessionDetailsScreen
import dev.johnoreilly.confetti.car.utils.defaultComponentContext
import dev.johnoreilly.confetti.car.utils.formatDateTime
import dev.johnoreilly.confetti.decompose.BookmarksComponent
import dev.johnoreilly.confetti.decompose.DefaultBookmarksComponent
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent

class BookmarksScreen(
    carContext: CarContext,
    private val user: User?,
    private val conference: String,
) : Screen(carContext), KoinComponent {

    private val component =
        DefaultBookmarksComponent(
            componentContext = defaultComponentContext(),
            conference = conference,
            user = user,
            onSessionSelected = { id ->
                screenManager.push(
                    SessionDetailsScreen(
                        carContext,
                        conference,
                        user,
                        sessionId = id,
                    )
                )
            },
            onSignIn = { /* Unused */ },
        )

    private val bookmarksState: Value<BookmarksComponent.UiState> = component.uiState.map {
        invalidate()
        it
    }

    override fun onGetTemplate(): Template {

        val loading = component.uiState.value is BookmarksComponent.Loading
        return when (val bookmarks = bookmarksState.value) {
            BookmarksComponent.Error,
            BookmarksComponent.Loading -> ListTemplate.Builder().setSingleList(ItemList.Builder().build()).build() // not handled yet

            is BookmarksComponent.Success -> {
                val listBuilder = createBookmarksList(bookmarks.upcomingSessions)
                listBuilder.apply {
                    setTitle(carContext.getString(R.string.bookmarks))
                    setHeaderAction(Action.BACK)
                    setLoading(loading)

                    if (bookmarks.upcomingSessions.isEmpty()) {
                        setSingleList(ItemList.Builder().build())
                    }
                }.build()
            }
        }
    }

    private fun createBookmarksList(bookmarks: Map<LocalDateTime, List<SessionDetails>>?): ListTemplate.Builder {
        val listTemplate = ListTemplate.Builder()
        bookmarks?.forEach { (startTime, sessions) ->
            val listBuilder = ItemList.Builder()

            sessions.forEach { session ->
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(session.title)
                        .addText(session.speakers.map { it.speakerDetails.name }.toString())
                        .setOnClickListener {
                            component.onSessionClicked(id = session.id)
                        }
                        .build()
                )
            }

            listTemplate.addSectionedList(
                SectionedItemList.create(
                    listBuilder.build(),
                    formatDateTime(startTime)
                )
            )
        }

        return listTemplate
    }
}