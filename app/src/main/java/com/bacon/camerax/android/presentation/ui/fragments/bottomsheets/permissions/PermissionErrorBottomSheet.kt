package com.bacon.camerax.android.presentation.ui.fragments.bottomsheets.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bacon.camerax.android.R
import com.bacon.camerax.android.databinding.BottomSheetPermissionErrorBinding
import com.bacon.camerax.android.presentation.base.BaseBottomSheet
import com.bacon.camerax.android.presentation.extensions.setNavigationResult
import com.bacon.camerax.android.presentation.extensions.setTextRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionErrorBottomSheet : BaseBottomSheet<BottomSheetPermissionErrorBinding>(
    R.layout.bottom_sheet_permission_error
) {
    override val binding by viewBinding(BottomSheetPermissionErrorBinding::bind)
    private val args by navArgs<PermissionErrorBottomSheetArgs>()


    override fun setupArgs() {
        binding.textPermissionsTitle.setTextRes(args.title)
    }

    override fun setupListeners() {
        clickAllow()
        clickReject()
    }

    private fun clickAllow() {
        binding.buttonPermissionsAllow.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                    Uri.parse("package:${requireActivity().packageName}")
                )
            )
            findNavController().navigateUp()
        }
    }

    private fun clickReject() {
        binding.buttonPermissionsReject.setOnClickListener {
            with(findNavController()) {
                setNavigationResult(ARG_CLOSE, true)
                navigateUp()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        findNavController().setNavigationResult(ARG_CLOSE, true)
    }

    companion object {
        const val ARG_CLOSE = "close"
    }
}