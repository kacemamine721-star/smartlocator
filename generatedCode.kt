import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Badges(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color(0xff9747ff)),
        modifier = modifier
                .clip(shape = RoundedCornerShape(5.dp))
        ) {
        Box(
            modifier = Modifier
                        .requiredWidth(width = 774.dp)
                        .requiredHeight(height = 344.dp)
            ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 20.dp,
                                                y = 20.dp)
                                .clip(shape = RoundedCornerShape(topStart = 42.dp, topEnd = 42.dp, bottomStart = 32.dp, bottomEnd = 32.dp))
                                .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(117f, 0f),
                    end = Offset(117f, 178.89f)))
                                .padding(all = 12.dp)
                ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.33.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(36.dp))
                                        .rotate(degrees = 90f)
                                        .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(93f, 104.63f),
                    end = Offset(0.74f, 104.63f)))
                                        .padding(all = 24.332443237304688.dp)
                    ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.33.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                                                .clip(shape = RoundedCornerShape(26.76568603515625.dp))
                                                .rotate(degrees = 135f)
                                                .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(-17.35f, 79.38f),
                    end = Offset(-17.35f, 114.08f)))
                                                .padding(all = 24.332443237304688.dp)
                        ) {
                        Box(
                            modifier = Modifier
                                                        .requiredSize(size = 65.dp)
                                                        .rotate(degrees = -45f)
                            ) {
                            Box(
                                modifier = Modifier
                                                                .requiredSize(size = 65.dp)
                                                                .clip(shape = RoundedCornerShape(14.599466323852539.dp))
                                                                .shadow(elevation = 34.2114143371582.dp,
                                                                                                shape = RoundedCornerShape(14.599466323852539.dp)))
                            Box(
                                modifier = Modifier
                                                                .align(alignment = Alignment.TopStart)
                                                                .offset(x = 7.01.dp,
                                                                                                y = 7.01.dp)
                                                                .requiredSize(size = 56.dp)
                                                                .clip(shape = RoundedCornerShape(9.732977867126465.dp))
                                                                .background(color = Color.Black.copy(alpha = 0.2f)))
                            Box(
                                modifier = Modifier
                                                                .align(alignment = Alignment.TopStart)
                                                                .offset(x = 17.9.dp,
                                                                                                y = 17.9.dp)
                                                                .requiredSize(size = 40.dp)
                                                                .clip(shape = RoundedCornerShape(7.2997331619262695.dp))
                                                                .shadow(elevation = 34.2114143371582.dp,
                                                                                                shape = RoundedCornerShape(7.2997331619262695.dp)))
                            }
                        }
                    }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Text(
                        text = "Astralite",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                                                fontSize = 27.sp,
                                                fontWeight = FontWeight.Bold,
                                                shadow = Shadow(color = Color(0xffcbdae6).copy(alpha = 0.5f), offset = Offset(0f, 1.264085292816162f), blurRadius = 21.09000015258789f)),
                        modifier = Modifier
                                                .shadow(elevation = 21.09000015258789.dp))
                    Text(
                        text = "LEVEL 5 - 10",
                        color = Color(0xff7a7a7a),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium))
                    }
                }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 270.dp,
                                                y = 20.dp)
                                .clip(shape = RoundedCornerShape(topStart = 42.dp, topEnd = 42.dp, bottomStart = 32.dp, bottomEnd = 32.dp))
                                .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(117f, 0f),
                    end = Offset(117f, 178.89f)))
                                .padding(all = 12.dp)
                ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.33.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(36.dp))
                                        .rotate(degrees = 90f)
                                        .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(93f, 104.63f),
                    end = Offset(0.74f, 104.63f)))
                                        .padding(all = 24.332443237304688.dp)
                    ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.33.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                                                .clip(shape = RoundedCornerShape(26.76568603515625.dp))
                                                .rotate(degrees = 135f)
                                                .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(-17.35f, 79.38f),
                    end = Offset(-17.35f, 114.08f)))
                                                .padding(all = 24.332443237304688.dp)
                        ) {
                        Box(
                            modifier = Modifier
                                                        .requiredSize(size = 65.dp)
                                                        .rotate(degrees = -45f)
                            ) {
                            Box(
                                modifier = Modifier
                                                                .requiredSize(size = 65.dp)
                                                                .clip(shape = RoundedCornerShape(14.599466323852539.dp))
                                                                .shadow(elevation = 34.2114143371582.dp,
                                                                                                shape = RoundedCornerShape(14.599466323852539.dp)))
                            Box(
                                modifier = Modifier
                                                                .align(alignment = Alignment.TopStart)
                                                                .offset(x = 7.01.dp,
                                                                                                y = 7.01.dp)
                                                                .requiredSize(size = 56.dp)
                                                                .clip(shape = RoundedCornerShape(9.732977867126465.dp))
                                                                .background(color = Color.Black.copy(alpha = 0.2f)))
                            Box(
                                modifier = Modifier
                                                                .align(alignment = Alignment.TopStart)
                                                                .offset(x = 17.9.dp,
                                                                                                y = 17.9.dp)
                                                                .requiredSize(size = 40.dp)
                                                                .clip(shape = RoundedCornerShape(7.2997331619262695.dp))
                                                                .background(brush = Brush.linearGradient(
                    0f to Color(0xff9e8976), 
0.19f to Color(0xff7a5e50), 
0.41f to Color(0xfff6d0ab), 
0.61f to Color(0xff9d774e), 
0.86f to Color(0xffc99b70), 
1f to Color(0xff795f52),
                    start = Offset(3.08f, 3.08f),
                    end = Offset(23.23f, 43.38f)))
                                                                .shadow(elevation = 34.2114143371582.dp,
                                                                                                shape = RoundedCornerShape(7.2997331619262695.dp)))
                            }
                        }
                    }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Text(
                        text = "Stellarion",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                                                fontSize = 27.sp,
                                                fontWeight = FontWeight.Bold,
                                                shadow = Shadow(color = Color(0xffcbdae6).copy(alpha = 0.5f), offset = Offset(0f, 1.264085292816162f), blurRadius = 21.09000015258789f)),
                        modifier = Modifier
                                                .shadow(elevation = 21.09000015258789.dp))
                    Text(
                        text = "LEVEL 10 - 100",
                        color = Color(0xff7a7a7a),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium))
                    }
                }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 520.dp,
                                                y = 20.dp)
                                .clip(shape = RoundedCornerShape(topStart = 42.dp, topEnd = 42.dp, bottomStart = 32.dp, bottomEnd = 32.dp))
                                .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(117f, 0f),
                    end = Offset(117f, 178.89f)))
                                .padding(all = 12.dp)
                ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.33.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(36.dp))
                                        .rotate(degrees = 90f)
                                        .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(93f, 104.63f),
                    end = Offset(0.74f, 104.63f)))
                                        .padding(all = 24.332443237304688.dp)
                    ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.33.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                                                .clip(shape = RoundedCornerShape(26.76568603515625.dp))
                                                .rotate(degrees = 135f)
                                                .background(brush = Brush.linearGradient(
                    0f to Color.White, 
1f to Color.White,
                    start = Offset(-17.35f, 79.38f),
                    end = Offset(-17.35f, 114.08f)))
                                                .padding(all = 24.332443237304688.dp)
                        ) {
                        Box(
                            modifier = Modifier
                                                        .requiredSize(size = 65.dp)
                                                        .rotate(degrees = -45f)
                            ) {
                            Box(
                                modifier = Modifier
                                                                .requiredSize(size = 65.dp)
                                                                .clip(shape = RoundedCornerShape(14.599466323852539.dp))
                                                                .shadow(elevation = 34.2114143371582.dp,
                                                                                                shape = RoundedCornerShape(14.599466323852539.dp)))
                            Box(
                                modifier = Modifier
                                                                .align(alignment = Alignment.TopStart)
                                                                .offset(x = 7.01.dp,
                                                                                                y = 7.01.dp)
                                                                .requiredSize(size = 56.dp)
                                                                .clip(shape = RoundedCornerShape(9.732977867126465.dp))
                                                                .background(color = Color.Black.copy(alpha = 0.2f)))
                            Box(
                                modifier = Modifier
                                                                .align(alignment = Alignment.TopStart)
                                                                .offset(x = 17.9.dp,
                                                                                                y = 17.9.dp)
                                                                .requiredSize(size = 40.dp)
                                                                .clip(shape = RoundedCornerShape(7.2997331619262695.dp))
                                                                .shadow(elevation = 34.2114143371582.dp,
                                                                                                shape = RoundedCornerShape(7.2997331619262695.dp)))
                            }
                        }
                    }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Text(
                        text = "Nebulaflare",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                                                fontSize = 27.sp,
                                                fontWeight = FontWeight.Bold,
                                                shadow = Shadow(color = Color(0xffcbdae6).copy(alpha = 0.5f), offset = Offset(0f, 1.264085292816162f), blurRadius = 21.09000015258789f)),
                        modifier = Modifier
                                                .shadow(elevation = 21.09000015258789.dp))
                    Text(
                        text = "LEVEL +100",
                        color = Color(0xff7a7a7a),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium))
                    }
                }
            }
        }
 }

@Preview(widthDp = 774, heightDp = 344)
@Composable
private fun BadgesPreview() {
    Badges(Modifier)
 }