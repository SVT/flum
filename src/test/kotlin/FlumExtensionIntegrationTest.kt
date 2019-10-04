

import me.alexpanov.net.FreePortFinder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.svt.oss.flum.Flum
import se.svt.oss.flum.FlumExtension
import se.svt.oss.flum.FlumPort

@ExtendWith(FlumExtension::class)
class FlumExtensionIntegrationTest {

    lateinit var flum: Flum

    @FlumPort
    val flumPort = FreePortFinder.findFreeLocalPort()

    @Test
    fun `flum field is set before execution`() {
        assertThat(flum).isNotNull
        assertThat(flum.server.port).isEqualTo(flumPort)
    }
}