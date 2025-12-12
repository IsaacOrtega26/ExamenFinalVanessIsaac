package com.example.fieldlog.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fieldlog.data.Record
import com.example.fieldlog.databinding.FragmentMapBinding
import com.example.fieldlog.ui.RecordViewModel
import com.example.fieldlog.ui.RecordViewModelFactory
import com.example.fieldlog.data.RecordRepository
import com.example.fieldlog.data.RecordDatabase
import com.example.fieldlog.utils.LocationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {
    
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var locationHelper: LocationHelper
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        
        setupViewModel()
        locationHelper = LocationHelper(requireContext())
        
        // Verificar API key
        val apiKey = getGoogleMapsApiKey()
        if (apiKey == null || apiKey == "TU_API_KEY_AQUI" || apiKey.isEmpty()) {
            binding.tvMapError.visibility = View.VISIBLE
            binding.tvMapError.text = "⚠️ Mapa no disponible\n\nNecesitas configurar una API key de Google Maps en AndroidManifest.xml\n\nRevisa MAPS_API_KEY_INSTRUCTIONS.md para más información"
            return binding.root
        }
        
        // Configurar el mapa
        try {
            val mapFragment = childFragmentManager.findFragmentById(com.example.fieldlog.R.id.map) as? SupportMapFragment
            if (mapFragment != null) {
                mapFragment.getMapAsync(this)
            } else {
                binding.tvMapError.visibility = View.VISIBLE
                binding.tvMapError.text = "Error: No se pudo inicializar el mapa"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvMapError.visibility = View.VISIBLE
            binding.tvMapError.text = "Error al inicializar el mapa: ${e.message}"
        }
        
        return binding.root
    }
    
    private fun getGoogleMapsApiKey(): String? {
        return try {
            val ai = requireContext().packageManager.getApplicationInfo(
                requireContext().packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            val bundle = ai.metaData
            bundle?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }
    }
    
    private fun setupViewModel() {
        val database = RecordDatabase.getDatabase(requireContext())
        val repository = RecordRepository(database.recordDao())
        val factory = RecordViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        try {
            // Ocultar mensaje de error si el mapa se carga correctamente
            binding.tvMapError.visibility = View.GONE
            
            // Configurar mapa
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            
            // Solicitar permisos de ubicación para el mapa
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap.isMyLocationEnabled = true
            }
            
            // Observar registros y agregar marcadores
            viewModel.records.observe(viewLifecycleOwner) { records ->
                addMarkersToMap(records)
                
                // Centrar en el primer registro o en ubicación actual o ubicación por defecto
                if (records.isNotEmpty()) {
                    val firstRecord = records.first()
                    val latLng = LatLng(firstRecord.latitude, firstRecord.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                } else {
                    // Si no hay registros, intentar centrar en ubicación actual o usar ubicación por defecto
                    getCurrentLocationForMap()
                }
            }
            
            Log.d("MapFragment", "Mapa cargado correctamente")
        } catch (e: Exception) {
            // Si hay error, mostrar mensaje
            Log.e("MapFragment", "Error al configurar el mapa", e)
            e.printStackTrace()
            binding.tvMapError.visibility = View.VISIBLE
            binding.tvMapError.text = "Error al configurar el mapa:\n${e.message}\n\nVerifica que la API key esté configurada correctamente"
            
            // Intentar mostrar ubicación por defecto
            try {
                val defaultLocation = LatLng(19.4326, -99.1332) // Ciudad de México
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
            } catch (ex: Exception) {
                Log.e("MapFragment", "Error al mover cámara", ex)
            }
        }
    }
    
    private fun addMarkersToMap(records: List<Record>) {
        googleMap.clear()
        
        records.forEach { record ->
            val position = LatLng(record.latitude, record.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(record.title)
                    .snippet(record.description.take(50))
            )
        }
    }
    
    private fun getCurrentLocationForMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                locationHelper.getCurrentLocation()?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                } ?: run {
                    // Si no se puede obtener ubicación, usar ubicación por defecto
                    val defaultLocation = LatLng(19.4326, -99.1332) // Ciudad de México
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
                }
            }
        } else {
            // Sin permisos, usar ubicación por defecto
            val defaultLocation = LatLng(19.4326, -99.1332) // Ciudad de México
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

