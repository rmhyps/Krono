package com.krono.platform

import com.krono.platform.bluetooth.BluetoothEventHandler
import com.krono.platform.location.GeofenceArea
import com.krono.platform.location.GeofenceEventHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class PlatformLogicTest {

    @Test
    fun volumePercentageMappingComputesCorrectIndex() {
        val minVolume = 0
        val maxVolume = 15

        val target20 = (minVolume + (20 / 100.0) * (maxVolume - minVolume)).roundToInt()
        assertEquals(3, target20)

        val target50 = (minVolume + (50 / 100.0) * (maxVolume - minVolume)).roundToInt()
        assertEquals(8, target50)

        val target100 = (minVolume + (100 / 100.0) * (maxVolume - minVolume)).roundToInt()
        assertEquals(15, target100)

        val target0 = (minVolume + (0 / 100.0) * (maxVolume - minVolume)).roundToInt()
        assertEquals(0, target0)
    }

    @Test
    fun geofenceAreaPreservesCoordinatesAndRadius() {
        val area = GeofenceArea(
            areaId = "campus-a",
            latitude = -7.894303,
            longitude = 112.595458,
            radiusM = 150.0
        )
        assertEquals("campus-a", area.areaId)
        assertEquals(-7.894303, area.latitude, 0.000001)
        assertEquals(150.0, area.radiusM, 0.01)
    }

    @Test
    fun bluetoothEventHandlerDispatchesCorrectly() {
        var observedAddress: String? = null
        var observedConnected: Boolean? = null

        BluetoothEventHandler.listener = { address, isConnected, _, _ ->
            observedAddress = address
            observedConnected = isConnected
        }

        BluetoothEventHandler.onConnected("00:11:22:33:44:55", 1000L, 1000L)
        assertEquals("00:11:22:33:44:55", observedAddress)
        assertTrue(observedConnected == true)

        BluetoothEventHandler.onDisconnected("00:11:22:33:44:55", 2000L, 2000L)
        assertTrue(observedConnected == false)
    }

    @Test
    fun geofenceEventHandlerDispatchesCorrectly() {
        var observedArea: String? = null
        var observedInside: Boolean? = null

        GeofenceEventHandler.listener = { areaId, isInside, _, _ ->
            observedArea = areaId
            observedInside = isInside
        }

        GeofenceEventHandler.onAreaEntered("area-campus-a", 1000L, 1000L)
        assertEquals("area-campus-a", observedArea)
        assertTrue(observedInside == true)

        GeofenceEventHandler.onAreaExited("area-campus-a", 2000L, 2000L)
        assertTrue(observedInside == false)
    }
}
