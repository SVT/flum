package se.svt.oss.flum

import me.alexpanov.net.FreePortFinder
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.ReflectionSupport
import java.lang.reflect.Field

class FlumExtension(val port: Int? = null) : ParameterResolver, AfterEachCallback, TestInstancePostProcessor {

    val FLUM_SERVER = "flum server"

    override fun postProcessTestInstance(testInstance: Any?, context: ExtensionContext?) {
        testInstance!!.javaClass.declaredFields
            .firstOrNull { it.type == Flum::class.java }?.run {
                if (canAccess(testInstance)) {
                    set(testInstance, createFlum(testInstance, context))
                } else {
                    isAccessible = true
                    set(testInstance, createFlum(testInstance, context))
                    isAccessible = false
                }
            }
    }

    override fun supportsParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?) =
        parameterContext?.parameter?.type?.isAssignableFrom(Flum::class.java) ?: false

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {
        return getStore(extensionContext).get(FLUM_SERVER) ?: createFlum(testInstance(extensionContext), extensionContext)
    }

    private fun createFlum(testInstance: Any, extensionContext: ExtensionContext?): Flum {
        val serverPort = port ?: findFlumPort(testInstance)
        val flum = Flum(serverPort)
        flum.start()

        getStore(extensionContext).put(FLUM_SERVER, flum)
        return flum
    }

    private fun testInstance(extensionContext: ExtensionContext?) =
        extensionContext!!.testInstance
            .orElseThrow { RuntimeException("No test instance available.") }

    private fun findFlumPort(testInstance: Any): Int {
        return flumPortFromAnnotatedField(testInstance)
            ?: flumPortFromAnnotatedMethod(testInstance)
            ?: flumPortFromNamedMethod(testInstance)
            ?: FreePortFinder.findFreeLocalPort()
    }

    private fun flumPortFromAnnotatedField(testInstance: Any) =
        testInstance
            .javaClass.declaredFields
            .firstOrNull { it.getAnnotation(FlumPort::class.java) != null }
            ?.let { intValue(testInstance, it) }

    private fun flumPortFromNamedMethod(testInstance: Any) =
        ReflectionSupport.findMethod(testInstance.javaClass, "getFlumPort")
            .map { it.invoke(testInstance) as Int }
            .orElse(null)

    private fun flumPortFromAnnotatedMethod(testInstance: Any) =
        testInstance
            .javaClass.declaredMethods
            .firstOrNull { it.getAnnotation(FlumPort::class.java) != null }
            ?.let { it.invoke(testInstance) as Int }

    private fun intValue(instance: Any?, field: Field) =
        if (field.canAccess(instance)) field.getInt(instance)
        else {
            field.run {
                isAccessible = true
                field.getInt(instance).also {
                    isAccessible = false
                }
            }
        }

    override fun afterEach(context: ExtensionContext?) {
        val flumServer =
            getStore(context).remove(FLUM_SERVER, Flum::class.java)
                ?: getStore(context?.parent?.orElse(null)).remove(FLUM_SERVER, Flum::class.java)
        flumServer?.run {
            try {
                verify()
            } finally {
                shutdown()
            }
        }
    }

    fun getStore(context: ExtensionContext?) =
        context!!.getStore(ExtensionContext.Namespace.create(this.toString()))
}