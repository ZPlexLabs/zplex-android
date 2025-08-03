package zechs.zplex.utils

import android.content.Context
import android.util.Log
import com.samsung.android.sdk.SsdkUnsupportedException
import com.samsung.android.sdk.penremote.SpenEventListener
import com.samsung.android.sdk.penremote.SpenRemote
import com.samsung.android.sdk.penremote.SpenUnitManager

object SpenRemoteHelper {
    private var spenUnitManager: SpenUnitManager? = null
    private var spenRemote: SpenRemote? = null

    fun initialize(
        context: Context,
        onConnected: () -> Unit = {},
        onDisconnected: (Int) -> Unit = {}
    ) {
        if (spenRemote == null) {
            spenRemote = SpenRemote.getInstance()
        }

        try {
            spenRemote?.let { spen ->
                if (!spen.isConnected) {
                    spen.connect(context, object : SpenRemote.ConnectionResultCallback {
                        override fun onSuccess(manager: SpenUnitManager?) {
                            Log.d("SpenRemote", "Connected")
                            spenUnitManager = manager
                            onConnected()
                        }

                        override fun onFailure(error: Int) {
                            Log.d("SpenRemote", "Disconnected")
                            onDisconnected(error)
                        }
                    })
                } else {
                    onConnected()
                }
            }
        } catch (e: SsdkUnsupportedException) {
            Log.e("SpenRemote", "Device not supported: ${e.message}")
        } catch (e: Exception) {
            Log.e("SpenRemote", "Error: ${e.message}")
        }
    }

    fun registerListener(spenUnit: Int, listener: SpenEventListener) {
        val button = spenUnitManager?.getUnit(spenUnit)
        spenUnitManager?.registerSpenEventListener(listener, button)
    }

    fun unregisterListener(spenUnit: Int) {
        val button = spenUnitManager?.getUnit(spenUnit)
        spenUnitManager?.unregisterSpenEventListener(button)
    }

    fun disconnect(context: Context) {
        spenRemote?.disconnect(context)
    }
}
