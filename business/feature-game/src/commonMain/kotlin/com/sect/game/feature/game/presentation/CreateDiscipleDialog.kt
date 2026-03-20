package com.sect.game.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.feature.game.container.GameContainer
import com.sect.game.feature.game.contract.GameIntent
import kotlin.random.Random

private val CHINESE_SURNAMES =
    listOf(
        "张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴",
        "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗",
        "梁", "宋", "郑", "谢", "韩", "唐", "冯", "于", "董", "萧",
    )

private val CHINESE_GIVEN_NAMES =
    listOf(
        "三", "四", "五", "六", "七", "八", "九", "十", "云", "风",
        "雷", "电", "雨", "雪", "霜", "龙", "虎", "鹤", "松", "柏",
        "天", "地", "玄", "黄", "宇", "宙", "洪", "荒", "日", "月",
    )

@Composable
fun CreateDiscipleDialog(
    container: GameContainer,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var spiritRoot by remember { mutableFloatStateOf(50f) }
    var talent by remember { mutableFloatStateOf(50f) }
    var luck by remember { mutableFloatStateOf(50f) }

    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "招募弟子",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("弟子姓名") },
                    placeholder = { Text("请输入弟子姓名") },
                    isError = nameError,
                    supportingText =
                        if (nameError) {
                            { Text("姓名不能为空") }
                        } else {
                            null
                        },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            onClick = { name = generateRandomName() },
                        ) {
                            Text("随机")
                        }
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                AttributeSlider(
                    label = "灵根",
                    value = spiritRoot,
                    onValueChange = { spiritRoot = it },
                    color = MaterialTheme.colorScheme.primary,
                )

                AttributeSlider(
                    label = "资质",
                    value = talent,
                    onValueChange = { talent = it },
                    color = MaterialTheme.colorScheme.secondary,
                )

                AttributeSlider(
                    label = "气运",
                    value = luck,
                    onValueChange = { luck = it },
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@TextButton
                    }
                    val attributes =
                        Attributes(
                            spiritRoot = spiritRoot.toInt().coerceIn(1, 100),
                            talent = talent.toInt().coerceIn(1, 100),
                            luck = luck.toInt().coerceIn(1, 100),
                        )
                    container.processIntent(GameIntent.CreateDisciple(name.trim(), attributes))
                    onDismiss()
                },
                enabled = isValid,
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun AttributeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${value.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                color = color,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..100f,
            steps = 98,
            colors =
                SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.3f),
                ),
        )
    }
}

private fun generateRandomName(): String {
    val surname = CHINESE_SURNAMES[Random.nextInt(CHINESE_SURNAMES.size)]
    val givenName = CHINESE_GIVEN_NAMES[Random.nextInt(CHINESE_GIVEN_NAMES.size)]
    return surname + givenName
}
