package it.baka.cameraapichecker

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import it.baka.cameraapichecker.ui.theme.CameraAPICheckerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        setContent {
            CameraAPICheckerTheme {
                MainPage(cameraManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(cameraManager: CameraManager) {

    var text by remember { mutableStateOf("") }

    val focusManager: FocusManager = LocalFocusManager.current

    Scaffold(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = 10.dp, bottom = 10.dp, end = 10.dp, start = 10.dp
            ),

        ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        }
                    )
                }
        ) {
            SearchBar(
                query = text,
                onQueryChange = {
                    text = it
                },
                active = false,
                onActiveChange = {},
                onSearch = {},
                placeholder = {
                    Text("Search by key")
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search Icon"
                    )
                },
                trailingIcon = {
                    if (text != "") {
                        IconButton(
                            onClick = {
                                text = ""
                            },
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Clear Text"
                            )
                        }
                    }
                }
            ) {}

            Box(
                Modifier.height(30.dp)
            )

            for (id in cameraManager.cameraIdList) {
                Text(
                    "Camera $id",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                val camera = cameraManager.getCameraCharacteristics(id)
                var keys = camera.keys.toMutableList()

//              ---------------
//              Adding them manually cuz for some reason they're not there already
                keys.add(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL)
                keys.add(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL)
//              ---------------


                for (key in keys.sortedBy {
                    it.name
                }.filter {
                    it.name.contains(text)
                }) {
                    TileTile(camera, key)

                    Box(
                        Modifier.height(10.dp)
                    )
                }

                Box(
                    Modifier.height(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TileTile(camera: CameraCharacteristics, key: CameraCharacteristics.Key<*>) {
    val haptics = LocalHapticFeedback.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val focusManager: FocusManager = LocalFocusManager.current


    var expanded by remember { mutableStateOf(false) }
    var value = camera.get(key)

    value = when (value) {
        is Array<*> -> value.joinToString(separator = ", ")
        is ArrayList<*> -> value.joinToString(separator = ", ")
        is IntArray -> value.joinToString(separator = ", ")
        is FloatArray -> value.joinToString(separator = ", ")
        is BooleanArray -> value.joinToString(separator = ", ")
        else -> value.toString()
    }

    Box {
        Column(
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        focusManager.clearFocus()
                    },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        expanded = true
                    },
                )
        ) {
            Text(
                key.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(value)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Copy") },
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                    expanded = false
                }
            )
        }
    }
}

