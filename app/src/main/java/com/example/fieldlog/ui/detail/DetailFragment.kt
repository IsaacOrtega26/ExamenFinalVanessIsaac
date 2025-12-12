package com.example.fieldlog.ui.detail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.fieldlog.data.Record
import com.example.fieldlog.databinding.FragmentDetailBinding
import com.example.fieldlog.ui.RecordViewModel
import com.example.fieldlog.ui.RecordViewModelFactory
import com.example.fieldlog.data.RecordRepository
import com.example.fieldlog.data.RecordDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel
    private var recordId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        setupViewModel()

        arguments?.let {
            recordId = it.getInt("recordId", -1)
            if (recordId != -1) {
                loadRecord(recordId)
            }
        }

        binding.btnShowOnMap.setOnClickListener {
            // Navegar al mapa con el registro seleccionado
            findNavController().navigate(
                com.example.fieldlog.R.id.navigation_map
            )
        }

        binding.btnEdit.setOnClickListener {
            // Navegar a editar registro
            val bundle = Bundle().apply {
                putInt("recordId", recordId)
            }
            findNavController().navigate(
                com.example.fieldlog.R.id.navigation_create,
                bundle
            )
        }

        return binding.root
    }

    private fun setupViewModel() {
        val database = RecordDatabase.getDatabase(requireContext())
        val repository = RecordRepository(database.recordDao())
        val factory = RecordViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]
    }

    private fun loadRecord(id: Int) {
        viewModel.getRecord(id).observe(viewLifecycleOwner) { record ->
            record?.let {
                displayRecord(it)
            }
        }
    }

    private fun displayRecord(record: Record) {
        binding.tvTitle.text = record.title
        binding.tvDescription.text = record.description

        val date = Date(record.date)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        binding.tvDate.text = "Fecha: ${dateFormat.format(date)}"

        binding.tvLocation.text = "Ubicación:\nLatitud: ${record.latitude}\nLongitud: ${record.longitude}"

        // Mostrar foto si existe
        if (!record.photoPath.isNullOrEmpty()) {
            val photoFile = File(record.photoPath)
            if (photoFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.ivPhoto.setImageBitmap(bitmap)
                binding.ivPhoto.visibility = View.VISIBLE
                binding.tvPhotoInfo.text = "Foto adjuntada: Sí"
                binding.tvPhotoInfo.visibility = View.GONE
            } else {
                binding.ivPhoto.visibility = View.GONE
                binding.tvPhotoInfo.text = "Foto adjuntada: Sí (archivo no encontrado)"
                binding.tvPhotoInfo.visibility = View.VISIBLE
            }
        } else {
            binding.ivPhoto.visibility = View.GONE
            binding.tvPhotoInfo.text = "Foto adjuntada: No"
            binding.tvPhotoInfo.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

