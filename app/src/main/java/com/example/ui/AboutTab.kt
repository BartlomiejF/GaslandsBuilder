package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun AboutTab() {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(WastelandBlack, Color(0xFF0C0E12))))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .testTag("about_main_card"),
            colors = CardDefaults.cardColors(containerColor = CarbonDark),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, RustOrange.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Iconic Header Accent
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(RustOrange.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Builder Icon",
                        tint = RustOrange,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // App Name
                Text(
                    text = stringResource(R.string.app_name).uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    ),
                    color = TextLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("about_app_name")
                )

                // Customizable Description
                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("about_app_description")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // GitHub Action Button
                Button(
                    onClick = {
                        uriHandler.openUri("https://github.com/BartlomiejF/GaslandsBuilder")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RustOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("about_github_button")
                ) {
                    Text(
                        text = "View on GitHub",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
