package com.bacon.camerax.android.presentation.ui.fragments.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bacon.camerax.android.R
import com.bacon.camerax.android.databinding.FragmentCameraBinding
import com.bacon.camerax.android.presentation.base.BaseFragment
import com.bacon.camerax.android.presentation.extensions.*
import com.bacon.camerax.android.presentation.ui.fragments.bottomsheets.permissions.PermissionErrorBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class CameraFragment : BaseFragment<CameraViewModel, FragmentCameraBinding>(
    R.layout.fragment_camera
) {
    override val viewModel by viewModels<CameraViewModel>()
    override val binding by viewBinding(FragmentCameraBinding::bind)

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        when {
            isGranted -> {
                startCamera()
            }
            !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                findNavController().navigateSafely(
                    CameraFragmentDirections.actionCameraFragmentToPermissionErrorBottomSheet(
                        R.string.text_permission_camera
                    )
                )
            }
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overrideOnBackPressed {
            findNavController().navigateUp()
        }
    }

    override fun initialize() {
        setupPermissionCamera()
    }

    private fun setupPermissionCamera() {
        if (!hasPermissionCheckAndRequest(
                cameraPermissionRequest,
                Manifest.permission.CAMERA,
            )
        ) {
            visibleOrGoneCamera(false)
        } else {
            visibleOrGoneCamera(true)
            startCamera()
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun setupListeners() {
        clickBtnTakePhoto()
        clickReshoot()
        clickContinue()
    }


    private fun clickBtnTakePhoto() {
        binding.buttonCameraOutside.setOnClickListener {
            takePhoto()
        }
    }

    private fun clickReshoot() {
        binding.buttonReshoot.setOnClickListener {
            setupPhoto(true)
        }
    }

    private fun clickContinue() {
        binding.buttonCameraContinue.setOnClickListener {
            setupPhoto(true)
        }
    }

    override fun setupSubscribers() {
        subscribePermissionArgs()
    }

    private fun subscribePermissionArgs() {
        findNavController().getBackStackEntry(R.id.cameraFragment)
            .getAccurateResultLiveData<Boolean>(
                key = PermissionErrorBottomSheet.ARG_CLOSE
            ) {
                if (it == true) {
                    flowNavController(R.id.nav_host_fragment_container).navigateUp()
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder().build()


            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("anime", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS", Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("anime", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    setupPhoto(false, photoFile)
                }
            })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else requireActivity().filesDir
    }


    private fun setupPhoto(
        isActive: Boolean,
        file: File? = null,
    ) = with(binding) {
        buttonCameraOutside.isVisible = isActive
        buttonCameraInside.isVisible = isActive
        buttonReshoot.isVisible = !isActive
        buttonCameraContinue.isVisible = !isActive
        if (isActive) {
            imageCamera.setImageResource(0)
        } else {
            imageCamera.setImageURI(file?.toUri())
        }
    }

    private fun visibleOrGoneCamera(isVisible: Boolean) = with(binding) {
        cameraPreview.isVisible = isVisible
        buttonCameraInside.isVisible = isVisible
        buttonCameraOutside.isVisible = isVisible
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
