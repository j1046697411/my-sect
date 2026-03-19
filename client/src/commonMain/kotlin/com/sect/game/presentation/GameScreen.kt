package com.sect.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Resources
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.mvi.GameContainer
import com.sect.game.mvi.GameIntent
import com.sect.game.presentation.theme.SectColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    container: GameContainer,
    onDiscipleClick: (Disciple) -> Unit = {}
) {
    val state by container.state.collectAsState()

    LaunchedEffect(Unit) {
        container.processIntent(GameIntent.LoadGame)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.sectName,
                            style = MaterialTheme.typography.titleLarge
                        )
                        ResourcesBar(resources = state.resources)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val newDiscipleName = "弟子${state.disciples.size + 1}"
                    container.processIntent(GameIntent.CreateDisciple(newDiscipleName, Attributes.DEFAULT))
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }
                state.error != null -> {
                    ErrorContent(
                        message = state.error ?: "未知错误",
                        onRetry = { container.processIntent(GameIntent.LoadGame) }
                    )
                }
                state.disciples.isEmpty() -> {
                    EmptyContent()
                }
                else -> {
                    DiscipleList(
                        disciples = state.disciples,
                        onDiscipleClick = onDiscipleClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourcesBar(resources: Resources) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        ResourceItem(
            label = "灵石",
            value = resources.spiritStones.toString(),
            color = SectColors.Gold
        )
        ResourceItem(
            label = "药材",
            value = resources.herbs.toString(),
            color = SectColors.JadeGreen
        )
        ResourceItem(
            label = "丹药",
            value = resources.pills.toString(),
            color = SectColors.NascentSoul
        )
    }
}

@Composable
private fun ResourceItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun DiscipleList(
    disciples: List<Disciple>,
    onDiscipleClick: (Disciple) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = disciples,
            key = { it.id.value }
        ) { disciple ->
            DiscipleCard(
                disciple = disciple,
                onClick = { onDiscipleClick(disciple) }
            )
        }
    }
}

@Composable
private fun DiscipleCard(
    disciple: Disciple,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = disciple.name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disciple.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = disciple.realm.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatLabel(label = "修炼", value = "${disciple.cultivationProgress}%")
                    StatLabel(label = "疲劳", value = "${disciple.fatigue}%")
                    StatLabel(label = "健康", value = "${disciple.health}%")
                }
            }
        }
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Card(
                onClick = onRetry,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "重试",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "暂无弟子",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "点击右下角按钮招募新弟子",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
