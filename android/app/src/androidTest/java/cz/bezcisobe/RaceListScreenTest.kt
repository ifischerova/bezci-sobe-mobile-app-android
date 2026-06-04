package cz.bezcisobe

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import cz.bezcisobe.data.repository.Race
import cz.bezcisobe.ui.races.RaceListContent
import cz.bezcisobe.ui.races.RaceListUiState
import org.junit.Rule
import org.junit.Test

class RaceListScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun showsRaceName() {
        val race = Race("1", "Praha 10K", "Praha", "2026-07-01", "10:00", null, "10K", "Road", false)
        rule.setContent {
            RaceListContent(
                state = RaceListUiState.Success(listOf(race)),
                search = "",
                onSearch = {},
                onRetry = {},
                onRaceClick = {},
            )
        }
        rule.onNodeWithText("Praha 10K").assertIsDisplayed()
    }
}
