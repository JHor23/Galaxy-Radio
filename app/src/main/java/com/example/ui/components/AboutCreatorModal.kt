package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyElectricBlue
import com.example.ui.theme.GalaxyMagenta
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyViolet
import com.example.ui.theme.GalaxyVoid
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PlusJakartaFontFamily

private object AboutCardPalette {
    val HeaderDarkTop = Color(0xFF060A18)
    val HeaderDarkBottom = Color(0xFF0E1733)
    val FrostSurface = Color(0xFFF3F7FE)
    val FrostGrid = Color(0xFF8AA8D6)
    val DeepInk = Color(0xFF091338)
    val BodyInk = Color(0xFF141E3B)
    val MutedInk = Color(0xFF475569)
    val KofiStart = Color(0xFF00B4A6)
    val KofiEnd = Color(0xFF007E8A)
    val KofiRim = Color(0xFF49F5FF)
    val GithubStart = Color(0xFF08142C)
    val GithubEnd = Color(0xFF0F254E)
    val GithubRim = Color(0xFF38BDF8)
    val ProjectsStart = Color(0xFF7C1FA2)
    val ProjectsEnd = Color(0xFF5612D6)
    val ProjectsRim = Color(0xFFE056FD)
    val HeartRed = Color(0xFFEF4444)
}

const val CREATOR_KOFI_URL = "https://ko-fi.com/jhor23"
const val CREATOR_SOURCE_URL = "https://github.com/JHor23/Galaxy-Radio"
const val CREATOR_PROJECTS_URL = "https://github.com/JHor23"

@Composable
fun AboutCreatorModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler(onBack = onDismiss)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        GalaxyStarfield(modifier = Modifier.fillMaxSize(), starCount = 45)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with Back / Close controls (48dp touch targets)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("about_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.about_close_cd),
                        tint = GalaxyCyan
                    )
                }

                Text(
                    text = "CREATOR PROFILE",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = GalaxyTextSecondary
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("about_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.about_close_cd),
                        tint = GalaxyTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Neon-Bordered Creator Card
            val outerGradient = Brush.linearGradient(
                colors = listOf(GalaxyCyan, GalaxyElectricBlue, GalaxyViolet, GalaxyMagenta)
            )
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AboutCardPalette.FrostSurface,
                border = BorderStroke(2.5.dp, outerGradient),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .testTag("about_creator_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Subtle cyber lattice & bottom corner neon trims on card body
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val step = 28.dp.toPx()
                        var y = 140.dp.toPx()
                        while (y < size.height) {
                            var x = 0f
                            while (x < size.width) {
                                drawCircle(
                                    color = AboutCardPalette.FrostGrid.copy(alpha = 0.14f),
                                    radius = 1.2.dp.toPx(),
                                    center = Offset(x, y)
                                )
                                drawLine(
                                    color = AboutCardPalette.FrostGrid.copy(alpha = 0.06f),
                                    start = Offset(x, y),
                                    end = Offset(x + step * 0.5f, y + step * 0.5f),
                                    strokeWidth = 0.8.dp.toPx()
                                )
                                x += step
                            }
                            y += step
                        }

                        // Bottom-left cyan cyber corner accent
                        val cornerW = 54.dp.toPx()
                        val cornerH = 38.dp.toPx()
                        val leftCorner = Path().apply {
                            moveTo(0f, size.height - cornerH)
                            lineTo(cornerW, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(leftCorner, color = GalaxyCyan.copy(alpha = 0.25f))
                        drawLine(
                            color = GalaxyCyan,
                            start = Offset(0f, size.height - cornerH),
                            end = Offset(cornerW, size.height),
                            strokeWidth = 2.5.dp.toPx()
                        )

                        // Bottom-right violet cyber corner accent
                        val rightCorner = Path().apply {
                            moveTo(size.width, size.height - cornerH)
                            lineTo(size.width - cornerW, size.height)
                            lineTo(size.width, size.height)
                            close()
                        }
                        drawPath(rightCorner, color = GalaxyViolet.copy(alpha = 0.25f))
                        drawLine(
                            color = GalaxyMagenta,
                            start = Offset(size.width, size.height - cornerH),
                            end = Offset(size.width - cornerW, size.height),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Cybernetic Banner + Overlapping Neon Avatar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(192.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Cybernetic dark top header canvas with notched transition
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(142.dp)
                            ) {
                                val w = size.width
                                val h = size.height
                                val notchDrop = 26.dp.toPx()

                                val bannerPath = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(w, 0f)
                                    lineTo(w, h - notchDrop)
                                    lineTo(w * 0.80f, h)
                                    lineTo(w * 0.20f, h)
                                    lineTo(0f, h - notchDrop)
                                    close()
                                }
                                drawPath(
                                    path = bannerPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            AboutCardPalette.HeaderDarkTop,
                                            AboutCardPalette.HeaderDarkBottom
                                        )
                                    )
                                )

                                // Left cyan cyber chevron beams
                                drawLine(
                                    color = GalaxyCyan.copy(alpha = 0.65f),
                                    start = Offset(-10f, h * 0.15f),
                                    end = Offset(w * 0.28f, h * 0.85f),
                                    strokeWidth = 8.dp.toPx()
                                )
                                drawLine(
                                    color = GalaxyElectricBlue.copy(alpha = 0.45f),
                                    start = Offset(w * 0.06f, 0f),
                                    end = Offset(w * 0.34f, h * 0.70f),
                                    strokeWidth = 3.dp.toPx()
                                )

                                // Right magenta/violet cyber chevron beams
                                drawLine(
                                    color = GalaxyMagenta.copy(alpha = 0.60f),
                                    start = Offset(w + 10f, h * 0.15f),
                                    end = Offset(w * 0.72f, h * 0.85f),
                                    strokeWidth = 8.dp.toPx()
                                )
                                drawLine(
                                    color = GalaxyViolet.copy(alpha = 0.50f),
                                    start = Offset(w * 0.94f, 0f),
                                    end = Offset(w * 0.66f, h * 0.70f),
                                    strokeWidth = 3.dp.toPx()
                                )

                                // Neon accent edge along notched cut
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(GalaxyCyan, GalaxyElectricBlue, GalaxyViolet, GalaxyMagenta)
                                    ),
                                    start = Offset(0f, h - notchDrop),
                                    end = Offset(w * 0.20f, h),
                                    strokeWidth = 2.dp.toPx()
                                )
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(GalaxyViolet, GalaxyMagenta)
                                    ),
                                    start = Offset(w * 0.80f, h),
                                    end = Offset(w, h - notchDrop),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }

                            // Glowing Circular Avatar with Tron / Daft Punk Helmet Mask
                            val avatarRingBrush = Brush.horizontalGradient(
                                colors = listOf(
                                    GalaxyCyan,
                                    GalaxyElectricBlue,
                                    GalaxyViolet,
                                    GalaxyMagenta
                                )
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(top = 24.dp)
                                    .size(160.dp)
                                    .drawBehind {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    GalaxyCyan.copy(alpha = 0.45f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(size.width * 0.38f, size.height * 0.5f),
                                                radius = size.width * 0.55f
                                            )
                                        )
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    GalaxyMagenta.copy(alpha = 0.40f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(size.width * 0.62f, size.height * 0.5f),
                                                radius = size.width * 0.55f
                                            )
                                        )
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(148.dp)
                                        .clip(CircleShape)
                                        .background(AboutCardPalette.HeaderDarkTop)
                                        .border(4.dp, avatarRingBrush, CircleShape)
                                        .padding(3.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_jhor_avatar),
                                        contentDescription = stringResource(R.string.about_avatar_cd),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .testTag("about_avatar_image")
                                    )
                                }
                            }
                        }

                        // Creator Content Body
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp)
                                .padding(top = 2.dp, bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // "JHor" flanked by neon gradient bars
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color.Transparent, GalaxyCyan, GalaxyElectricBlue)
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = stringResource(R.string.about_creator_handle),
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = AboutCardPalette.DeepInk,
                                    modifier = Modifier.testTag("about_creator_handle")
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(GalaxyViolet, GalaxyMagenta, Color.Transparent)
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Center Ring Node Cyber Divider
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color.Transparent, GalaxyCyan, GalaxyElectricBlue)
                                            )
                                        )
                                )
                                Canvas(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .size(16.dp)
                                ) {
                                    drawCircle(
                                        color = AboutCardPalette.DeepInk,
                                        radius = size.minDimension / 2f - 2.dp.toPx(),
                                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawCircle(
                                        color = GalaxyElectricBlue,
                                        radius = 2.5.dp.toPx()
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(GalaxyViolet, GalaxyMagenta, Color.Transparent)
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // "ABOUT ME" Title
                            Text(
                                text = stringResource(R.string.about_me_heading),
                                fontFamily = OrbitronFontFamily,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.5.sp,
                                color = AboutCardPalette.DeepInk
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bio Paragraphs
                            Text(
                                text = stringResource(R.string.about_bio_line_1),
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 21.sp,
                                color = AboutCardPalette.BodyInk,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = stringResource(R.string.about_bio_line_2),
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 21.sp,
                                color = AboutCardPalette.BodyInk,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // 3 Direct Link Pill Buttons
                            AboutPillButton(
                                label = stringResource(R.string.about_btn_kofi),
                                gradientStart = AboutCardPalette.KofiStart,
                                gradientEnd = AboutCardPalette.KofiEnd,
                                borderColor = AboutCardPalette.KofiRim,
                                icon = Icons.Filled.LocalCafe,
                                showHeartBadge = true,
                                testTag = "about_kofi_button",
                                onClick = { openExternalUrl(context, CREATOR_KOFI_URL) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            AboutPillButton(
                                label = stringResource(R.string.about_btn_github),
                                gradientStart = AboutCardPalette.GithubStart,
                                gradientEnd = AboutCardPalette.GithubEnd,
                                borderColor = AboutCardPalette.GithubRim,
                                icon = Icons.Filled.Code,
                                showHeartBadge = false,
                                testTag = "about_github_button",
                                onClick = { openExternalUrl(context, CREATOR_SOURCE_URL) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            AboutPillButton(
                                label = stringResource(R.string.about_btn_projects),
                                gradientStart = AboutCardPalette.ProjectsStart,
                                gradientEnd = AboutCardPalette.ProjectsEnd,
                                borderColor = AboutCardPalette.ProjectsRim,
                                icon = Icons.Filled.Folder,
                                showHeartBadge = false,
                                testTag = "about_projects_button",
                                onClick = { openExternalUrl(context, CREATOR_PROJECTS_URL) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Version Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .height(2.dp)
                                        .background(GalaxyCyan)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME} | ${stringResource(R.string.about_footer_thanks)}",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AboutCardPalette.MutedInk
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .height(2.dp)
                                        .background(GalaxyViolet)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AboutPillButton(
    label: String,
    gradientStart: Color,
    gradientEnd: Color,
    borderColor: Color,
    icon: ImageVector,
    showHeartBadge: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(gradientStart, gradientEnd)
                )
            )
            .border(1.5.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = gradientEnd,
                    modifier = Modifier.size(16.dp)
                )
                if (showHeartBadge) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = AboutCardPalette.HeartRed,
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                    )
                }
            }
        }
    }
}

private fun openExternalUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No browser available to open link", Toast.LENGTH_SHORT).show()
    }
}
