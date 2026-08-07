package org.pacien.tincapp.activities.configure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.pacien.tincapp.R
import org.pacien.tincapp.storageprovider.FilesDocumentsProvider
import org.pacien.tincapp.ui.components.ListBlock
import org.pacien.tincapp.ui.components.SectionTitle
import org.pacien.tincapp.ui.components.TincDivider
import org.pacien.tincapp.ui.theme.HeaderPrimary
import org.pacien.tincapp.ui.theme.TextTitle
import org.pacien.tincapp.ui.theme.TincTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureScreen(
    onOpenDirectory: (String) -> Unit,
    onGenerateConfig: () -> Unit,
    onJoinNetwork: () -> Unit,
    onEncryptDecryptKeys: () -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.start_activity_menu_configure)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.generic_action_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderPrimary,
                    titleContentColor = TextTitle,
                    navigationIconContentColor = TextTitle
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SectionTitle(text = stringResource(R.string.configure_activity_title_directories))
            
            ListBlock(
                title = stringResource(R.string.configure_browse_directories_action_open_networks_config),
                onClick = { onOpenDirectory(FilesDocumentsProvider.VIRTUAL_ROOT_NETWORKS) }
            )
            TincDivider()
            ListBlock(
                title = stringResource(R.string.configure_browse_directories_action_open_logs),
                onClick = { onOpenDirectory(FilesDocumentsProvider.VIRTUAL_ROOT_LOGS) }
            )

            SectionTitle(text = stringResource(R.string.configure_activity_title_tools))

            ListBlock(
                title = stringResource(R.string.configure_tools_action_generate_node_configuration_and_keys),
                onClick = onGenerateConfig
            )
            TincDivider()
            ListBlock(
                title = stringResource(R.string.configure_tools_action_join_network_via_invitation_url_qr_code),
                onClick = onJoinNetwork
            )
            TincDivider()
            ListBlock(
                title = stringResource(R.string.configure_tools_action_encrypt_decrypt_private_keys),
                onClick = onEncryptDecryptKeys
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigureScreenPreview() {
    TincTheme {
        ConfigureScreen(
            onOpenDirectory = {},
            onGenerateConfig = {},
            onJoinNetwork = {},
            onEncryptDecryptKeys = {},
            onNavigateUp = {}
        )
    }
}
