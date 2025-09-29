package vcmsa.projects.prog7314.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkManager {
    private const val TAG = "NetworkManager"

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Initialize network monitoring
     */
    fun initialize(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check initial connectivity state
        updateConnectionStatus()

        // Register network callback to monitor changes
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                _isOnline.value = false
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                val isConnected = hasInternet && isValidated
                Log.d(TAG, "Network capabilities changed: Internet=$hasInternet, Validated=$isValidated, Connected=$isConnected")

                _isOnline.value = isConnected
            }
        }

        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
        Log.d(TAG, "Network monitoring initialized")
    }

    /**
     * Check current connectivity status
     */
    private fun updateConnectionStatus() {
        val isConnected = isNetworkAvailable()
        _isOnline.value = isConnected
        Log.d(TAG, "Initial network status: $isConnected")
    }

    /**
     * Check if network is currently available
     */
    fun isNetworkAvailable(): Boolean {
        connectivityManager?.let { cm ->
            val activeNetwork = cm.activeNetwork
            val networkCapabilities = cm.getNetworkCapabilities(activeNetwork)

            return networkCapabilities?.let { capabilities ->
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } ?: false
        }
        return false
    }

    /**
     * Get connection type (WiFi, Cellular, etc.)
     */
    fun getConnectionType(): ConnectionType {
        connectivityManager?.let { cm ->
            val activeNetwork = cm.activeNetwork
            val networkCapabilities = cm.getNetworkCapabilities(activeNetwork)

            networkCapabilities?.let { capabilities ->
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                    else -> ConnectionType.OTHER
                }
            }
        }
        return ConnectionType.NONE
    }

    /**
     * Cleanup network monitoring
     */
    fun cleanup() {
        networkCallback?.let { callback ->
            connectivityManager?.unregisterNetworkCallback(callback)
        }
        networkCallback = null
        Log.d(TAG, "Network monitoring cleaned up")
    }

    /**
     * Get readable connection status
     */
    fun getConnectionStatus(): String {
        return if (isOnline.value) {
            "Connected (${getConnectionType().name})"
        } else {
            "Offline"
        }
    }
}

enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER,
    NONE
}