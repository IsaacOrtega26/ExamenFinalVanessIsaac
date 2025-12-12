package com.example.fieldlog.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fieldlog.data.Record
import com.example.fieldlog.databinding.FragmentHomeBinding
import com.example.fieldlog.ui.RecordAdapter
import com.example.fieldlog.ui.RecordViewModel
import com.example.fieldlog.ui.RecordViewModelFactory
import com.example.fieldlog.data.RecordRepository
import com.example.fieldlog.data.RecordDatabase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel
    private lateinit var adapter: RecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupViewModel()
        setupRecyclerView()
        observeRecords()

        return binding.root
    }

    private fun setupViewModel() {
        val database = RecordDatabase.getDatabase(requireContext())
        val repository = RecordRepository(database.recordDao())
        val factory = RecordViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = RecordAdapter(
            onItemClick = { record ->
                // Navegar al detalle del registro
                val bundle = Bundle().apply {
                    putInt("recordId", record.id)
                }
                findNavController().navigate(
                    com.example.fieldlog.R.id.navigation_detail,
                    bundle
                )
            },
            onDeleteClick = { record ->
                viewModel.deleteRecord(record)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeRecords() {
        viewModel.records.observe(viewLifecycleOwner) { records ->
            adapter.submitList(records)
            if (records.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

