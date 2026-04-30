package com.example.motsic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.motsic.databinding.DialogAddEpisodeBinding
import com.example.motsic.databinding.FragmentLogBinding
import com.google.android.material.chip.Chip

class LogFragment : Fragment() {

    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!
    private lateinit var store: EpisodeStore
    private lateinit var adapter: EpisodeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        store = EpisodeStore(requireContext())
        adapter = EpisodeAdapter(store.all())
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter
        refreshEmptyState()

        binding.btnAdd.setOnClickListener { showAddDialog() }
    }

    private fun refreshEmptyState() {
        val empty = adapter.itemCount == 0
        binding.empty.visibility = if (empty) View.VISIBLE else View.GONE
        binding.list.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun showAddDialog() {
        val dlg = DialogAddEpisodeBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.log_episode)
            .setView(dlg.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val trigger = readChipText(dlg.triggerGroup.checkedChipId, dlg.root)
                    ?: getString(R.string.trigger_other)
                val severity = readChipText(dlg.severityGroup.checkedChipId, dlg.root)
                    ?.toIntOrNull()?.coerceIn(1, 5) ?: 3
                val helped = dlg.helped.text?.toString()?.trim().orEmpty()
                store.add(Episode(System.currentTimeMillis(), trigger, severity, helped))
                adapter.update(store.all())
                refreshEmptyState()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun readChipText(checkedId: Int, root: View): String? {
        if (checkedId == View.NO_ID) return null
        return root.findViewById<Chip>(checkedId)?.text?.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
