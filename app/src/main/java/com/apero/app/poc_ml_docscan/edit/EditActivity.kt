package com.apero.app.poc_ml_docscan.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.apero.app.poc_ml_docscan.R
import com.apero.app.poc_ml_docscan.databinding.ActivityEditBinding
import com.apero.app.poc_ml_docscan.edit.adapter.EditPagePreviewAdapter
import com.apero.app.poc_ml_docscan.scan.common.model.InternalImage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.ArrayList

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private val viewModel: EditViewModel by viewModels()
    private val pagePreviewAdapter: EditPagePreviewAdapter by lazy {
        EditPagePreviewAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateUI()
    }

    private fun updateUI() {
        viewModel.fillPagePreview(
            intent.getParcelableArrayListExtra(ARG_LIST_IMAGE) ?: emptyList()
        )
        initView()
        handleObserver()
    }

    private fun initView() {
        with(binding.vpPreviewEdit) {
            isUserInputEnabled = true
            adapter = pagePreviewAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.updateCurrentPage(position)
                }
            })
        }
    }

    private fun handleObserver() {
        viewModel.editUiState
            .map { it.listPagePreview }
            .onEach {
                pagePreviewAdapter.submitList(it)
            }.launchIn(lifecycleScope)
    }

    companion object {
        private const val ARG_LIST_IMAGE = "ARG_LIST_IMAGE"
        fun start(context: Context, list: List<InternalImage>) {
            val intent = Intent(context, EditActivity::class.java)
            intent.putParcelableArrayListExtra(ARG_LIST_IMAGE, ArrayList(list))
            context.startActivity(intent)
        }
    }
}