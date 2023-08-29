package ui.component

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import util.LanyardApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun LanyardCard() {
    val activity = LanyardApi.activity

    LaunchedEffect(Unit) {
        LanyardApi.connectWebSocket()
    }

    if (activity != null) {
        Container(
            gap = 1.3.cssRem
        ) {
            if (activity.assets?.largeImage != null) {
                PresenceIcon(
                    applicationId = activity.applicationId,
                    largeImage = activity.assets.largeImage,
                    smallImage = activity.assets.smallImage
                )
            }

            Column(
                gap = 0.2.cssRem
            ) {
                H2 {
                    Text(activity.name)
                }

                H4 {
                    var elapsedTime by remember { mutableStateOf<String?>(null) }

                    // this sucks, but I don't know how else to do it

                    if (elapsedTime != null) {
                        if (activity.timestamps != null) {
                            LaunchedEffect(activity) {
                                while (isActive) {
                                    elapsedTime =
                                        (Clock.System.now().epochSeconds.seconds - activity.timestamps.start.milliseconds).toComponents { hours, minutes, seconds, _ ->
                                            buildString {
                                                if (hours > 0) {
                                                    append(hours.toString().padStart(2, '0'))
                                                    append(':')
                                                }

                                                append(minutes.toString().padStart(2, '0'))
                                                append(':')
                                                append(seconds.toString().padStart(2, '0'))
                                            }
                                        }
                                    delay(500)
                                }
                            }
                        }

                        activity.details?.let {
                            Text(it)
                            Br()
                        }

                        activity.state?.let {
                            Text(it)
                            Br()
                        }

                        Text("$elapsedTime elapsed")
                    }
                }
            }
        }
    }
}

@Composable
fun PresenceIcon(
    applicationId: String?,
    largeImage: String,
    smallImage: String? = null,
) {
    Div(
        attrs = {
            style {
                position(Position.Relative)
            }
        }
    ) {
        val src = remember {
            if (largeImage.startsWith("spotify:")) {
                "https://i.scdn.co/image/${largeImage.removePrefix("spotify:")}"
            } else {
                LanyardApi.getAssetImage(applicationId!!, largeImage)
            }
        }

        Img(
            attrs = {
                style {
                    display(DisplayStyle.Block)
                    width(7.cssRem)
                    height(7.cssRem)
                    borderRadius(1.2.cssRem)

                    if (smallImage != null) {
                        property("mask", "url(activity-mask.svg)")
                        property("mask-size", "cover")
                    }
                }
            },
            src = src
        )

        if (smallImage != null) {
            Img(
                attrs = {
                    style {
                        width(2.cssRem)
                        height(2.cssRem)
                        borderRadius(50.percent)

                        position(Position.Absolute)
                        bottom((-4).px)
                        right((-4).px)
                    }
                },
                src = LanyardApi.getAssetImage(applicationId!!, smallImage)
            )
        }
    }
}