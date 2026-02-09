package fr.unica.fetheddine.lahjaily.vibechef.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FiltersSection(
    restrictionOptions: List<String>,
    selectedFilters: Set<String>,
    onFilterToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Restrictions",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            restrictionOptions.forEach { opt ->
                val selected = opt in selectedFilters
                FilterChip(
                    selected = selected,
                    onClick = { onFilterToggle(opt) },
                    label = { Text(opt) },
                    leadingIcon = if (selected) {
                        { Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun VibeSelector(
    vibes: List<String>,
    selectedVibe: String,
    onVibeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        vibes.forEach { vibe ->
            FilterChip(
                selected = selectedVibe == vibe,
                onClick = { onVibeSelected(vibe) },
                label = { Text(vibe) }
            )
        }
    }
}
