package com.kingzcheung.xime.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.util.SubcharHelper

@Composable
fun NumberKeyboardLayout(
    onKeyPress: (String) -> Unit,
    keyBackgroundColor: Color,
    keyTextColor: Color,
    specialKeyBackgroundColor: Color,
    keyboardBackgroundColor: Color = Color.Transparent,
    shadowEnabled: Boolean = true,
    shadowElevation: Dp = 1.dp,
    shadowShapeRadius: Dp = 8.dp,
    keyCornerRadius: Dp = 8.dp,
    modifier: Modifier = Modifier,
    onKeyPressDown: ((String) -> Unit)? = null,
    isFloatingMode: Boolean = false,
    specialKeyTextColor: Color = Color.White,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = !isFloatingMode && configuration.screenWidthDp > configuration.screenHeightDp
    val commonSymbols = listOf(
        "~", "!", "#", "$", "%", "^", "&", "?",
        "(", ")", "_", "=", "[", "]", "{", "}",
        "\\", "|", ";", ":", "'", "\"", "<", ">"
    )

    var swipeState by remember { mutableStateOf(SwipeState()) }
    var keyboardBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }
    var lastKeyBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }

    val isDarkTheme = keyTextColor == Color(0xFFE8EAED)

    val bubbleData = rememberSwipeBubbleDrawData(
        swipeState = swipeState,
        keyBounds = lastKeyBounds,
        keyBackgroundColor = keyBackgroundColor,
        keyTextColor = keyTextColor,
        accentColor = specialKeyTextColor,
        keyWidth = if (swipeState.isSwiping || swipeState.isPressed) lastKeyBounds.width else 0f,
        keyboardWidth = keyboardBounds.width
    )

    CompositionLocalProvider(LocalKeyCornerRadius provides keyCornerRadius) {
    Box(
        modifier = modifier
            .background(keyboardBackgroundColor)
            .onGloballyPositioned { coordinates ->
                keyboardBounds = coordinates.boundsInRoot()
            }
            .drawWithContent {
                drawContent()
                bubbleData?.let { drawSwipeBubble(it) }
            }) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp, horizontal = 50.dp),
            ) {
                Column(
                    modifier = Modifier.weight(0.42f).fillMaxHeight(),
                ) {
                    CompositionLocalProvider(
                        LocalKeyVisualPadding provides PaddingValues(horizontal = 1.dp, vertical = 2.dp)
                    ) {
                    commonSymbols.chunked(6).forEach { rowSymbols ->
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                        ) {
                            rowSymbols.forEach { sym ->
                                KeyButton(text = sym, onClick = { onKeyPress(sym) }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke(sym) }, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius, fontSize = 14.sp)
                            }
                            repeat(6 - rowSymbols.size) { Box(modifier = Modifier.weight(1f)) }
                        }
                    }
                    }
                }
                Spacer(modifier = Modifier.weight(0.16f))
                Box(modifier = Modifier.weight(0.42f).fillMaxHeight()) {
                    CompositionLocalProvider(
                        LocalKeyVisualPadding provides PaddingValues(horizontal = 1.dp, vertical = 2.dp)
                    ) {
                    NumberRowsContent(onKeyPress = onKeyPress, keyBackgroundColor = keyBackgroundColor, keyTextColor = keyTextColor, specialKeyBackgroundColor = specialKeyBackgroundColor, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius, onKeyPressDown = onKeyPressDown, compactMode = true, specialKeyTextColor = specialKeyTextColor, onSwipeStateChange = { state, bounds ->
                        val newState = if (state.isSwipeDown && state.swipeText != null) state.copy(charInfos = SubcharHelper.parseSwipeDownText(state.swipeText)) else state
                        swipeState = newState
                        lastKeyBounds = Rect(left = bounds.left - keyboardBounds.left, top = bounds.top - keyboardBounds.top, right = bounds.right - keyboardBounds.left, bottom = bounds.bottom - keyboardBounds.top)
                    })
                    }
                }
            }
        } else {
            CompositionLocalProvider(
                LocalKeyVisualPadding provides PaddingValues(2.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(keyboardBackgroundColor)
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
            ) {
            NumberRowsContent(onKeyPress = onKeyPress, keyBackgroundColor = keyBackgroundColor, keyTextColor = keyTextColor, specialKeyBackgroundColor = specialKeyBackgroundColor, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius, onKeyPressDown = onKeyPressDown, specialKeyTextColor = specialKeyTextColor, onSwipeStateChange = { state, bounds ->
                val newState = if (state.isSwipeDown && state.swipeText != null) state.copy(charInfos = SubcharHelper.parseSwipeDownText(state.swipeText)) else state
                swipeState = newState
                lastKeyBounds = Rect(left = bounds.left - keyboardBounds.left, top = bounds.top - keyboardBounds.top, right = bounds.right - keyboardBounds.left, bottom = bounds.bottom - keyboardBounds.top)
            })
            }
            }
        }
    }
    }
}

@Composable
private fun NumberRowsContent(
    onKeyPress: (String) -> Unit,
    keyBackgroundColor: Color,
    keyTextColor: Color,
    specialKeyBackgroundColor: Color,
    shadowEnabled: Boolean,
    shadowElevation: Dp,
    shadowShapeRadius: Dp,
    onKeyPressDown: ((String) -> Unit)?,
    onSwipeStateChange: ((SwipeState, Rect) -> Unit)?,
    compactMode: Boolean = false,
    specialKeyTextColor: Color,
) {
    val keyFontSize = if (compactMode) 15.sp else androidx.compose.ui.unit.TextUnit.Unspecified
    val symFontSize = if (compactMode) 11.sp else 13.sp
    val ctrlFontSize = if (compactMode) 11.sp else androidx.compose.ui.unit.TextUnit.Unspecified
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.weight(0.85f).fillMaxHeight()) {
            listOf("/", "-", "+").forEach { sym ->
                KeyButton(text = sym, onClick = { onKeyPress(sym) }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke(sym) }, modifier = Modifier.weight(1f), fontSize = symFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            }
            KeyButton(text = "符", onClick = { onKeyPress("symbol") }, backgroundColor = specialKeyBackgroundColor, textColor = specialKeyTextColor, onPress = { onKeyPressDown?.invoke("symbol") }, modifier = Modifier.weight(1f), fontSize = ctrlFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
        }
        Column(Modifier.weight(1.1f).fillMaxHeight()) {
            KeyButton(text = "1", onClick = { onKeyPress("1") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("1") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "4", onClick = { onKeyPress("4") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("4") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "7", onClick = { onKeyPress("7") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("7") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            IconKeyButton(icon = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack), onClick = { onKeyPress("abc") }, backgroundColor = specialKeyBackgroundColor, iconColor = specialKeyTextColor, modifier = Modifier.weight(1f), onPress = { onKeyPressDown?.invoke("abc") }, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
        }
        Column(Modifier.weight(1.1f).fillMaxHeight()) {
            KeyButton(text = "2", onClick = { onKeyPress("2") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("2") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "5", onClick = { onKeyPress("5") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("5") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "8", onClick = { onKeyPress("8") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("8") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "0", onClick = { onKeyPress("0") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("0") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
        }
        Column(Modifier.weight(1.1f).fillMaxHeight()) {
            KeyButton(text = "3", onClick = { onKeyPress("3") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("3") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "6", onClick = { onKeyPress("6") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("6") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "9", onClick = { onKeyPress("9") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("9") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "空格", onClick = { onKeyPress("space") }, backgroundColor = specialKeyBackgroundColor, textColor = specialKeyTextColor, onPress = { onKeyPressDown?.invoke("space") }, modifier = Modifier.weight(1f), fontSize = ctrlFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
        }
        Column(Modifier.weight(0.85f).fillMaxHeight()) {
            SwipeableIconKeyButton(icon = rememberVectorPainter(Icons.AutoMirrored.Filled.Backspace), onClick = { onKeyPress("delete") }, backgroundColor = specialKeyBackgroundColor, iconColor = specialKeyTextColor, modifier = Modifier.weight(1f), swipeText = "清空", onSwipe = { onKeyPress("clear_composition") }, onLongClick = { onKeyPress("delete") }, onPress = { onKeyPressDown?.invoke("delete") }, swipeUpLabel = "清空", swipeDownLabel = "撤回", onSwipeUp = { onKeyPress("clear_all") }, onSwipeDown = { onKeyPress("undo_clear") }, onSwipeLeft = { onKeyPress("clear_composition") }, onSwipeStateChange = onSwipeStateChange, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = ".", onClick = { onKeyPress(".") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke(".") }, modifier = Modifier.weight(1f), fontSize = keyFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "@", onClick = { onKeyPress("@") }, backgroundColor = keyBackgroundColor, textColor = keyTextColor, onPress = { onKeyPressDown?.invoke("@") }, modifier = Modifier.weight(1f), fontSize = ctrlFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
            KeyButton(text = "确定", onClick = { onKeyPress("enter") }, backgroundColor = specialKeyBackgroundColor, textColor = specialKeyTextColor, onPress = { onKeyPressDown?.invoke("enter") }, modifier = Modifier.weight(1f), fontSize = ctrlFontSize, shadowEnabled = shadowEnabled, shadowElevation = shadowElevation, shadowShapeRadius = shadowShapeRadius)
        }
    }
}
