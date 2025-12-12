package com.example.fieldlog.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
        
        // Configurar el mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        return binding.root
    }
    
    private fun setupViewModel() {
        val database = RecordDatabase.getDatabase(requireContext())
        val repository = RecordRepository(database.recordDao())
        val factory = RecordViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Configurar mapa
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        
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
            
            // Centrar en el primer registro o en ubicación actual
            if (records.isNotEmpty()) {
                val firstRecord = records.first()
                val latLng = LatLng(firstRecord.latitude, firstRecord.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
            } else {
                // Si no hay registros, intentar centrar en ubicación actual
                getCurrentLocationForMap()
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
            locationHelper.getCurrentLocation()?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}