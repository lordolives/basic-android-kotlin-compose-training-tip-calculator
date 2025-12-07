/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat

class MainActivity : ComponentActivity() {

    // onCreate is part of the Android Lifecycle which is called when the application is created
    override fun onCreate(savedInstanceState: Bundle?) {

        // Utilize the entire display area of a device
        enableEdgeToEdge()

        // Execute onCreate from parent class.
        super.onCreate(savedInstanceState)

        // Defines the layout
        setContent {

            // Applies the project theme
            TipTimeTheme {

                // Construct for displaying UI elements
                Surface(modifier = Modifier.fillMaxSize()) {

                    // Call the TipTimeLayout Composable function.
                    TipTimeLayout()
                }
            }
        }
    }
}

/**
 * Displays the layout for our TipTime UI.
 */
@Composable
fun TipTimeLayout() {

    // Maintain the state for the entered bill amount
    var amountInput by remember { mutableStateOf("") }

    // Maintain the state for the entered tip percentage amount
    var tipInput by remember { mutableStateOf("") }

    // Maintain the state for the round up switch.
    var roundUp by remember { mutableStateOf(false) }

    // If the amount is null it will set 0.0
    val amount = amountInput.toDoubleOrNull() ?: 0.0

    // If the tip percent is null it will set 0.0
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0

    // Calculate the tip by calling the calculateTip function
    val tip = calculateTip(amount, tipPercent, roundUp)

    // Access to the focus manager, used to control user focus for the UI
    val focusManager = LocalFocusManager.current

    // Display the layout as a column
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Calculate Tip
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start)
        )
        // Bill amount
        EditNumberField(
            label = R.string.bill_amount,
            // Defines the virtual keyboard type
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next  // Set the action button to a next button
            ),
            // Defines the virtual keyboard action button
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
            ,
            value = amountInput,
            onValueChanged = {
                amountInput = it    // Updates our remember variable
            },
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
        )
        // Tip %
        EditNumberField(
            label = R.string.how_was_the_service,
            // Defines the virtual keyboard type
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done  // Set the action button to a checkbox button
            ),
            // Defines the virtual keyboard action button
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            value = tipInput,
            onValueChanged = {
                tipInput = it   // Updates our remember variable
            }
        )
        // Round up tip switch
        RoundTheTipRow(
            roundUp = roundUp,
            onRoundUpChanged = {
                roundUp = it
            }
        )
        // Tip Amount
        Text(
            text = stringResource(R.string.tip_amount, tip),
            style = MaterialTheme.typography.displaySmall
        )
        // Add space to bottom of UI
        Spacer(modifier = Modifier.height(150.dp))
    }
}

/**
 * Draws a number field
 */
@Composable
fun EditNumberField(
    @StringRes label: Int,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        singleLine = true,
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

/**
 * Draw a text field and switch on the same row.
 */
@Composable
fun RoundTheTipRow(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .size(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Round up tip?
        Text(text = stringResource(R.string.round_up_tip))

        // Toggle switch
        Switch(
            checked = roundUp,
            colors = SwitchDefaults.colors(
                // This is for the purpose of the tutorial and it was mentioned this should be
                // avoided, because hard coded colors, may not work in all situations.
                uncheckedThumbColor = Color.DarkGray
            ),
            onCheckedChange = onRoundUpChanged,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
        )
    }
}

/**
 * Calculates the tip based on the user input and format the tip amount
 * according to the local currency.
 * Example would be "$10.00".
 */
private fun calculateTip(
    amount: Double,
    tipPercent: Double = 15.0,
    roundUp: Boolean
): String {
    var tip = tipPercent / 100 * amount
    if(roundUp)
        tip = kotlin.math.ceil(tip)
    return NumberFormat.getCurrencyInstance().format(tip)
}

/**
 * Allows the IDE to render the UI without the need for an emulator.
 */
@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        TipTimeLayout()
    }
}

