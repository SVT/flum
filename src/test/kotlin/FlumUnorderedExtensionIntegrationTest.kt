// SPDX-FileCopyrightText: 2022 Sveriges Television AB
//
// SPDX-License-Identifier: Apache-2.0

import me.alexpanov.net.FreePortFinder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.svt.oss.flum.Flum
import se.svt.oss.flum.FlumPort
import se.svt.oss.flum.FlumUnorderedExtension

@ExtendWith(FlumUnorderedExtension::class)
class FlumUnorderedExtensionIntegrationTest {

    lateinit var flum: Flum

    @FlumPort
    val flumPort = FreePortFinder.findFreeLocalPort()

    @Test
    fun `flum field is set before execution`() {
        assertThat(flum).isNotNull
        assertThat(flum.server.port).isEqualTo(flumPort)
        assertThat(flum.port).isEqualTo(flumPort)
        assertThat(flum.matchRequestOrder).isFalse()
    }
}
