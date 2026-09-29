/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.tincapp.activities.status.ui

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import org.graphper.parser.DotParser
import org.pacien.tincapp.R

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GraphView(
    dotString: String,
    onNodeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val svgString = remember(dotString) {
        if (dotString.isBlank()) return@remember null
        try {
            val patched = dotString.replace("style = \"filled\"", "style = \"bold\"")
            DotParser.parse(patched).toSvgStr()
        } catch (e: Exception) {
            null
        }
    }

    if (svgString == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (dotString.isBlank()) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.status_topology_render_error))
            }
        }
        return
    }

    val html = remember(svgString) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="
                width=device-width,
                initial-scale=1.0,
                maximum-scale=5.0,
                user-scalable=yes
            ">
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    user-select: none;
                }
                svg * {
                    font-family: system-ui, sans-serif;
                }
                .node {
                    cursor: pointer;
                    -webkit-tap-highlight-color: rgba(0,0,0,0.1);
                }
            </style>
        </head>
        <body>
            $svgString
            <script>
                const svg = document.querySelector('svg');
                svg.removeAttribute('width');
                svg.removeAttribute('height');

                document.querySelectorAll('.node').forEach(node => {
                    node.onclick = function() {
                        var nodeName = this.id;
                        if (nodeName) Android.onNodeClick(nodeName);
                    };
                });
            </script>
        </body>
        </html>
        """
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.useWideViewPort = false
                settings.loadWithOverviewMode = true
                
                webViewClient = WebViewClient()
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onNodeClick(nodeName: String) {
                        onNodeClick(nodeName)
                    }
                }, "Android")
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        },
        modifier = modifier.fillMaxSize()
    )
}
