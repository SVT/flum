import me.alexpanov.net.FreePortFinder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.svt.oss.flum.Flum
import se.svt.oss.flum.FlumExtension
import se.svt.oss.flum.FlumPort

open class BaseTestClass {
    var flum: Flum? = null

    @FlumPort
    val flumPort = FreePortFinder.findFreeLocalPort()
}

@ExtendWith(FlumExtension::class)
class FlumExtensionInheritanceTest : BaseTestClass() {

    @Test
    fun `flum field is set before execution`() {
        assertThat(flum).isNotNull
        assertThat(flum?.server?.port).isEqualTo(flumPort)
    }
}