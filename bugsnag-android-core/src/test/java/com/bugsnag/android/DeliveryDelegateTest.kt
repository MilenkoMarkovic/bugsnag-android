package com.bugsnag.android

import com.bugsnag.android.BugsnagTestUtils.generateImmutableConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
internal class DeliveryDelegateTest {

    @Mock
    lateinit var eventStore: EventStore

    val config = generateImmutableConfig()
    val breadcrumbState = BreadcrumbState(50, CallbackState(), NoopLogger)
    private val logger = InterceptingLogger()
    lateinit var deliveryDelegate: DeliveryDelegate
    val handledState = HandledState.newInstance(HandledState.REASON_UNHANDLED_EXCEPTION)
    val event = Event(RuntimeException("Whoops!"), config, handledState)

    @Before
    fun setUp() {
        deliveryDelegate =
            DeliveryDelegate(logger, eventStore, config, breadcrumbState)
        event.session = Session("123", Date(), User(null, null, null), false)
    }

    @Test
    fun generateUnhandledReport() {
        var msg: StateEvent.NotifyUnhandled? = null
        deliveryDelegate.addObserver { _, arg ->
            msg = arg as StateEvent.NotifyUnhandled
        }
        deliveryDelegate.deliver(event)

        // verify message sent
        assertNotNull(msg)

        // check session count incremented
        assertEquals(1, event.session!!.unhandledCount)
        assertEquals(0, event.session!!.handledCount)
    }

    @Test
    fun generateHandledReport() {
        val state = HandledState.newInstance(HandledState.REASON_HANDLED_EXCEPTION)
        val event = Event(RuntimeException("Whoops!"), config, state)
        event.session = Session("123", Date(), User(null, null, null), false)

        var msg: StateEvent.NotifyHandled? = null
        deliveryDelegate.addObserver { _, arg ->
            msg = arg as StateEvent.NotifyHandled
        }
        deliveryDelegate.deliver(event)

        // verify message sent
        assertNotNull(msg)

        // check session count incremented
        assertEquals(0, event.session!!.unhandledCount)
        assertEquals(1, event.session!!.handledCount)
    }

    @Test
    fun generateEmptyReport() {
        val state = HandledState.newInstance(HandledState.REASON_HANDLED_EXCEPTION)
        val event = Event(RuntimeException("Whoops!"), config, state)
        event.errors = emptyList()

        var msg: StateEvent.NotifyHandled? = null
        deliveryDelegate.addObserver { _, arg ->
            msg = arg as StateEvent.NotifyHandled
        }
        deliveryDelegate.deliver(event)

        // verify no payload was sent for an Event with no errors
        assertNull(msg)
    }

    @Test
    fun deliverReport() {
        val status = deliveryDelegate.deliverReportInternal(Report("api-key", event), event)
        assertEquals(DeliveryStatus.DELIVERED, status)
        assertEquals("Sent 1 new event to Bugsnag", logger.msg)

        val breadcrumb = breadcrumbState.store.peek()
        assertEquals(BreadcrumbType.ERROR, breadcrumb.type)
        assertEquals("java.lang.RuntimeException", breadcrumb.message)
        assertEquals("java.lang.RuntimeException", breadcrumb.metadata["errorClass"])
        assertEquals("Whoops!", breadcrumb.metadata["message"])
        assertEquals("true", breadcrumb.metadata["unhandled"])
        assertEquals("ERROR", breadcrumb.metadata["severity"])
    }

    private class InterceptingLogger : Logger {
        var msg: String? = null
        override fun i(msg: String) {
            this.msg = msg
        }
    }
}