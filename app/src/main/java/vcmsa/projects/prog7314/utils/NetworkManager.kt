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

/**
 * Utility object for monitoring network connectivity status.
 * Provides real-time updates when the device connects or disconnects from the internet.
 * Useful for showing offline indicators and preventing API calls when there's no connection.
 */
object NetworkManager {
    private const val TAG = "NetworkManager"

    // Observable network status that UI components can watch
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Android system service for checking network status
    private var connectivityManager: ConnectivityManager? = null

    // Listener that gets notified when network status changes
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Sets up network monitoring for the app.
     * Should be called once when the app starts, typically in Application.onCreate().
     * Registers a listener to receive network status updates automatically.
     */
    fun initialize(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check if we're online right now
        updateConnectionStatus()

        // Set up requirements for the networks we want to monitor
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // Has internet access
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) // Internet is actually working
            .build()

        // Create listener that responds to network changes
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            /**
             * Called when a new network becomes available
             */
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                _isOnline.value = true
            }

            /**
             * Called when the network connection is lost
             */
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                _isOnline.value = false
            }

            /**
             * Called when network capabilities change (e.g., WiFi strength, data type)
             * This is more reliable than just onAvailable because it checks if internet actually works
             */
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                // Check if network has internet capability
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                // Check if the internet connection is actually working (not just connected)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                val isConnected = hasInternet && isValidated
                Log.d(TAG, "Network capabilities changed: Internet=$hasInternet, Validated=$isValidated, Connected=$isConnected")

                _isOnline.value = isConnected
            }
        }

        // Start listening for network changes
        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
        Log.d(TAG, "Network monitoring initialized")
    }

    /**
     * Checks the current network status and updates the isOnline state.
     * Called once during initialization to set the initial state.
     */
    private fun updateConnectionStatus() {
        val isConnected = isNetworkAvailable()
        _isOnline.value = isConnected
        Log.d(TAG, "Initial network status: $isConnected")
    }

    /**
     * Checks if the device currently has a working internet connection.
     * Returns true only if internet is both available and validated (actually working).
     * This is more reliable than just checking if WiFi/data is turned on.
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
     * Determines what type of network connection is currently active.
     * Returns the connection type (WiFi, cellular data, ethernet, etc.).
     * Useful for deciding whether to download large files or only small data.
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
     * Stops monitoring network changes and releases resources.
     * Should be called when the app is shutting down to prevent memory leaks.
     */
    fun cleanup() {
        networkCallback?.let { callback ->
            connectivityManager?.unregisterNetworkCallback(callback)
        }
        networkCallback = null
        Log.d(TAG, "Network monitoring cleaned up")
    }

    /**
     * Returns a human-readable description of the current connection status.
     * Example outputs: "Connected (WIFI)", "Connected (CELLULAR)", or "Offline"
     * Useful for displaying network status to the user.
     */
    fun getConnectionStatus(): String {
        return if (isOnline.value) {
            "Connected (${getConnectionType().name})"
        } else {
            "Offline"
        }
    }
}

/**
 * Enum representing the different types of network connections.
 * Used to identify how the device is connected to the internet.
 */
enum class ConnectionType {
    WIFI,       // Connected via WiFi
    CELLULAR,   // Connected via mobile data (3G/4G/5G)
    ETHERNET,   // Connected via ethernet cable (rare on mobile devices)
    OTHER,      // Some other connection type
    NONE        // No connection
}