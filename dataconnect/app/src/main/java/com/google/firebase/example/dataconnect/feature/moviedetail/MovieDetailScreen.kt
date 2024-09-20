package com.google.firebase.example.dataconnect.feature.moviedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.dataconnect.movies.GetMovieByIdQuery
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.ui.components.Actor
import com.google.firebase.example.dataconnect.ui.components.ActorsList
import com.google.firebase.example.dataconnect.ui.components.ErrorCard
import com.google.firebase.example.dataconnect.ui.components.LoadingScreen
import com.google.firebase.example.dataconnect.ui.components.ReviewCard
import com.google.firebase.example.dataconnect.ui.components.ToggleButton

@Composable
fun MovieDetailScreen(
    movieId: String,
    onActorClicked: (actorId: String) -> Unit,
    movieDetailViewModel: MovieDetailViewModel = viewModel()
) {
    movieDetailViewModel.setMovieId(movieId)
    val uiState by movieDetailViewModel.uiState.collectAsState()
    Scaffold { padding ->
        when (uiState) {
            is MovieDetailUIState.Error -> {
                ErrorCard((uiState as MovieDetailUIState.Error).errorMessage)
            }

            MovieDetailUIState.Loading -> LoadingScreen()

            is MovieDetailUIState.Success -> {
                val ui = uiState as MovieDetailUIState.Success
                val movie = ui.movie
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    MovieInformation(
                        modifier = Modifier.padding(padding),
                        movie = movie,
                        isMovieWatched = ui.isWatched,
                        isMovieFavorite = ui.isFavorite,
                        onFavoriteToggled = { newValue ->
                            movieDetailViewModel.toggleFavorite(newValue)
                        },
                        onWatchToggled = { newValue ->
                            movieDetailViewModel.toggleWatched(newValue)
                        }
                    )
                    // Main Actors list
                    ActorsList(
                        listTitle = stringResource(R.string.title_main_actors),
                        actors = movie?.mainActors?.mapNotNull {
                            Actor(it.id.toString(), it.name, it.imageUrl)
                        }.orEmpty(),
                        onActorClicked = { onActorClicked(it) }
                    )
                    // Supporting Actors list
                    ActorsList(
                        listTitle = stringResource(R.string.title_supporting_actors),
                        actors = movie?.supportingActors?.mapNotNull {
                            Actor(it.id.toString(), it.name, it.imageUrl)
                        }.orEmpty(),
                        onActorClicked = { onActorClicked(it) }
                    )
                    UserReviews(
                        onReviewSubmitted = { rating, text ->
                            movieDetailViewModel.addRating(rating, text)
                        },
                        movie?.reviews ?: emptyList()
                    )
                }

            }
        }
    }
}

@Composable
fun MovieInformation(
    modifier: Modifier = Modifier,
    movie: GetMovieByIdQuery.Data.Movie?,
    isMovieWatched: Boolean,
    isMovieFavorite: Boolean,
    onWatchToggled: (newValue: Boolean) -> Unit,
    onFavoriteToggled: (newValue: Boolean) -> Unit
) {
    if (movie == null) {
        ErrorCard(stringResource(R.string.error_movie_not_found))
    } else {
        Column(
            modifier = modifier
                .padding(16.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineLarge
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = movie.releaseYear.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Outlined.Star, "Favorite")
                Text(
                    text = movie.rating?.toString() ?: "0.0",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            Row {
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(150.dp)
                        .aspectRatio(9f / 16f)
                        .padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row {
                        movie.tags?.let { movieTags ->
                            movieTags.filterNotNull().forEach { tag ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(tag) },
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = movie.description ?: stringResource(R.string.description_not_available),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                ToggleButton(
                    iconEnabled = Icons.Filled.CheckCircle,
                    iconDisabled = Icons.Outlined.Check,
                    textEnabled = stringResource(R.string.button_unmark_watched),
                    textDisabled = stringResource(R.string.button_mark_watched),
                    isEnabled = isMovieWatched,
                    onToggle = onWatchToggled
                )
                Spacer(modifier = Modifier.width(8.dp))
                ToggleButton(
                    iconEnabled = Icons.Filled.Favorite,
                    iconDisabled = Icons.Outlined.FavoriteBorder,
                    textEnabled = stringResource(R.string.button_remove_favorite),
                    textDisabled = stringResource(R.string.button_favorite),
                    isEnabled = isMovieFavorite,
                    onToggle = onFavoriteToggled
                )
            }
        }
    }
}

@Composable
fun UserReviews(
    onReviewSubmitted: (rating: Float, text: String) -> Unit,
    reviews: List<GetMovieByIdQuery.Data.Movie.ReviewsItem>
) {
    var reviewText by remember { mutableStateOf("") }
    Text(
        text = "User Reviews",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var rating by remember { mutableFloatStateOf(3f) }
        Text("Rating: ${rating}")
        Slider(
            value = rating,
            // Round the value to the nearest 0.5
            onValueChange = { rating = (Math.round(it * 2) / 2.0).toFloat() },
            steps = 9,
            valueRange = 1f..5f
        )
        TextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text(stringResource(R.string.hint_write_review)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onReviewSubmitted(rating, reviewText)
                reviewText = ""
            }
        ) {
            Text(stringResource(R.string.button_submit_review))
        }
    }
    Column {
        // TODO(thatfiredev): Handle cases where the list is too long to display
        reviews.forEach {
            ReviewCard(
                userName = it.user.username,
                date = it.reviewDate,
                rating = it.rating?.toDouble() ?: 0.0,
                text = it.reviewText ?: ""
            )
        }
    }
}
