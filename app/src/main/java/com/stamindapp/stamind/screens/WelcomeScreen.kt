package com.stamindapp.stamind.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stamindapp.stamind.R
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StamindColors.BackgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dekoratif Arka Plan (welcome_decor) - Ekranın %40'ını kaplar
        Image(
            painter = painterResource(id = R.drawable.welcome_decor),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            alpha = 0.8f // Görünürlüğü artırıldı
        )

        Spacer(modifier = Modifier.height(16.dp)) // Dalga ile başlık arası tam 16dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Stamind'e Hoş Geldin",
                style = LexendTypography.Bold3,
                color = StamindColors.Green900,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Duygularını keşfet, zihnini dinlendir ve her güne daha bilinçli bir adım at.",
                style = LexendTypography.Regular7,
                color = StamindColors.TextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Butonları en alta itmek için
        Spacer(modifier = Modifier.weight(1f))

        // Alt Butonlar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StamindColors.Green600),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(
                    text = "Hemen Başla",
                    style = LexendTypography.Bold6,
                    color = StamindColors.White,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.5.dp, StamindColors.Green600)
            ) {
                Text(
                    text = "Giriş Yap",
                    style = LexendTypography.Bold6,
                    color = StamindColors.Green600,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onGetStartedClick = {},
        onLoginClick = {}
    )
}
