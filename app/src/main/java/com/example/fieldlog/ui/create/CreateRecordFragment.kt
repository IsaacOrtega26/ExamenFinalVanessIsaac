package com.example.fieldlog.ui.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fieldlog.data.Record
import com.example.fieldlog.databinding.FragmentCreateRecordBinding
import com.example.fieldlog.ui.RecordViewModel
import com.example.fieldlog.ui.RecordViewModelFactory
import com.example.fieldlog.data.RecordRepository
import com.example.fieldlog.data.RecordDatabase
import com.example.fieldlog.utils.CameraHelper
import com.example.fieldlog.utils.LocationHelper
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateRecordFragment : Fragment() {

    private var _binding: FragmentCreateRecordBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel
    private lateinit var locationHelper: LocationHelper
    private lateinit var cameraHelper: CameraHelper

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var photoPath: String? = null
    private var recordId: Int = -1
    private var isEditing = false
    private var originalDate: Long = System.currentTimeMillis()

    private lateinit var takePictureLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Verificar que el archivo existe
                val path = cameraHelper.getCurrentPhotoPath()
                if (path != null) {
                    val photoFile = java.io.File(path)
                    if (photoFile.exists() && photoFile.length() > 0) {
                        photoPath = path
                        binding.tvPhotoStatus.text = "Foto: Tomada ✓"
                        Toast.makeText(requireContext(), "Foto tomada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.tvPhotoStatus.text = getString(com.example.fieldlog.R.string.photo_not_taken)
                        Toast.makeText(requireContext(), "Error: El archivo de la foto no se guardó correctamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.tvPhotoStatus.text = getString(com.example.fieldlog.R.string.photo_not_taken)
                    Toast.makeText(requireContext(), "Error al obtener la ruta de la foto", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.tvPhotoStatus.text = getString(com.example.fieldlog.R.string.photo_not_taken)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecordBinding.inflate(inflater, container, false)

        setupViewModel()
        locationHelper = LocationHelper(requireContext())
        cameraHelper = CameraHelper(requireContext())

        // Verificar si estamos editando un registro
        arguments?.let {
            recordId = it.getInt("recordId", -1)
            if (recordId != -1) {
                isEditing = true
                loadRecordForEdit(recordId)
            }
        }

        setupListeners()

        return binding.root
    }

    private fun setupViewModel() {
        val database = RecordDatabase.getDatabase(requireContext())
        val repository = RecordRepository(database.recordDao())
        val factory = RecordViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]
    }

    private fun loadRecordForEdit(id: Int) {
        viewModel.getRecord(id).observe(viewLifecycleOwner) { record ->
            record?.let {
                binding.etTitle.setText(it.title)
                binding.etDescription.setText(it.description)
                currentLatitude = it.latitude
                currentLongitude = it.longitude
                val locationText = "Ubicación:\nLat: ${String.format("%.6f", it.latitude)}\nLng: ${String.format("%.6f", it.longitude)}"
                binding.tvLocation.text = locationText
                photoPath = it.photoPath
                originalDate = it.date
                
                if (!it.photoPath.isNullOrEmpty()) {
                    val photoFile = java.io.File(it.photoPath)
                    if (photoFile.exists()) {
                        binding.tvPhotoStatus.text = "Foto: Existente ✓"
                    } else {
                        binding.tvPhotoStatus.text = getString(com.example.fieldlog.R.string.photo_not_taken)
                    }
                } else {
                    binding.tvPhotoStatus.text = getString(com.example.fieldlog.R.string.photo_not_taken)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnGetLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnTakePhoto.setOnClickListener {
            if (checkCameraPermission()) {
                val intent = cameraHelper.getTakePictureIntent(requireContext())
                if (intent != null) {
                    takePictureLauncher.launch(intent)
                } else {
                    Toast.makeText(requireContext(), "Error al preparar la cámara", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestCameraPermission()
            }
        }

        binding.btnSaveRecord.setOnClickListener {
            if (isEditing) {
                updateRecord()
            } else {
                saveRecord()
            }
        }
    }

    private fun getCurrentLocation() {
        binding.progressLocation.visibility = View.VISIBLE
        binding.btnGetLocation.isEnabled = false
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val location = locationHelper.getCurrentLocation()
                location?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude

                    val locationText = "Ubicación obtenida:\nLat: ${String.format("%.6f", it.latitude)}\nLng: ${String.format("%.6f", it.longitude)}"
                    binding.tvLocation.text = locationText
                    Toast.makeText(requireContext(), "Ubicación obtenida", Toast.LENGTH_SHORT).show()
                } ?: run {
                    binding.tvLocation.text = getString(com.example.fieldlog.R.string.location_not_obtained)
                    Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.tvLocation.text = getString(com.example.fieldlog.R.string.location_not_obtained)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressLocation.visibility = View.GONE
                binding.btnGetLocation.isEnabled = true
            }
        }
    }

    private fun saveRecord() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentLatitude == null || currentLongitude == null) {
            Toast.makeText(requireContext(), "Obtenga la ubicación primero", Toast.LENGTH_SHORT).show()
            return
        }

        val record = Record(
            title = title,
            description = description,
            latitude = currentLatitude!!,
            longitude = currentLongitude!!,
            photoPath = photoPath
        )

        viewModel.insertRecord(record)

        Toast.makeText(requireContext(), "Registro guardado", Toast.LENGTH_SHORT).show()

        // Limpiar campos
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.tvLocation.text = getString(com.example.fieldlog.R.string.location_not_obtained)
        currentLatitude = null
        currentLongitude = null
        photoPath = null
        binding.tvPhotoStatus.text = getString(com.example.fieldlog.R.string.photo_not_taken)
        
        // Navegar de vuelta a home
        findNavController().navigateUp()
    }

    private fun updateRecord() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentLatitude == null || currentLongitude == null) {
            Toast.makeText(requireContext(), "Obtenga la ubicación primero", Toast.LENGTH_SHORT).show()
            return
        }

        val record = Record(
            id = recordId,
            title = title,
            description = description,
            latitude = currentLatitude!!,
            longitude = currentLongitude!!,
            photoPath = photoPath,
            date = originalDate
        )

        viewModel.updateRecord(record)

        Toast.makeText(requireContext(), "Registro actualizado", Toast.LENGTH_SHORT).show()
        
        // Navegar de vuelta al detalle
        findNavController().navigateUp()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }


    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

