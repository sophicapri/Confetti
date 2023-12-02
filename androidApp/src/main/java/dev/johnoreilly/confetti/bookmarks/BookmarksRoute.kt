package dev.johnoreilly.confetti.bookmarks

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.decompose.BookmarksComponent
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.ui.HomeScaffold

@Composable
fun BookmarksRoute(
    component: BookmarksComponent,
    windowSizeClass: WindowSizeClass,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    val uiState by component
        .uiState
        .subscribeAsState()

    HomeScaffold(
        title = stringResource(R.string.bookmarks),
        windowSizeClass = windowSizeClass,
        topBarActions = topBarActions,
    ) {
        BookmarksView(
            navigateToSession = component::onSessionClicked,
            onSignIn = component::onSignInClicked,
            addBookmark = component::addBookmark,
            removeBookmark = component::removeBookmark,
            uiState = uiState,
            isLoggedIn = component.isLoggedIn,
        )
    }
}
