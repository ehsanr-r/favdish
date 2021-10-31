package com.example.favdish.view.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.ActivityAddUpdateDishBinding
import com.example.favdish.databinding.DialogCustomImageSelectionBinding
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.adapters.CustomListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddUpdateDishBinding

    private val favDishViewModel : FavDishViewModel by viewModels{
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    private var mFavDishDetails: FavDish? = null

    companion object {
        private const val CAMERA = 1
        private const val GALLERY = 2
        private const val IMAGE_DIRECTORY = "FavDishImage"
    }

    private var mImagePath: String = ""

    private lateinit var mCustomListDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.EXTRA_DISH_DETAILS)) {
            mFavDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setActionBar()

        mFavDishDetails?.let {
            if (it.id != 0) {
                mImagePath = it.image

                // Load the dish image in the ImageView.
                Glide.with(this@AddUpdateDishActivity)
                    .load(mImagePath)
                    .centerCrop()
                    .into(binding.ivDishImage)

                binding.etTitle.setText(it.title)
                binding.etType.setText(it.type)
                binding.etCategory.setText(it.category)
                binding.etIngredients.setText(it.ingredients)
                binding.etCookingTime.setText(it.cooking_time)
                binding.etDirectionToCook.setText(it.direction_to_cook)

                binding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }

        binding.ivAddDishImage.setOnClickListener(this)

        binding.etType.setOnClickListener(this@AddUpdateDishActivity)
        binding.etCategory.setOnClickListener(this@AddUpdateDishActivity)
        binding.etCookingTime.setOnClickListener(this@AddUpdateDishActivity)

        binding.btnAddDish.setOnClickListener(this@AddUpdateDishActivity)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA) {

                data?.extras?.let {
                    val thumbnail: Bitmap =
                        data.extras!!.get("data") as Bitmap // Bitmap from camera

                    // Set Capture Image bitmap to the imageView using Glide
                    Glide.with(this@AddUpdateDishActivity)
                        .load(thumbnail)
                        .centerCrop()
                        .into(binding.ivDishImage)

                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("ImagePath", mImagePath)


                    // Replace the add icon with edit icon once the image is loaded.
                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@AddUpdateDishActivity,
                            R.drawable.ic_vector_edit
                        )
                    )
                }
            } else if (requestCode == GALLERY) {

                data?.let {
                    // Here we will get the select image URI.
                    val selectedPhotoUri = data.data


                    // Set Selected Image URI to the imageView using Glide
                    Glide.with(this@AddUpdateDishActivity)
                        .load(selectedPhotoUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {


                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let {
                                    val bitmap: Bitmap = resource.toBitmap()

                                    mImagePath = saveImageToInternalStorage(bitmap)
                                    Log.i("ImagePath", mImagePath)

                                }
                                return false
                            }

                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                // log exception
                                Log.e("TAG", "Error loading image", e)
                                return false // important to return false so the error placeholder can be placed
                            }
                        })
                        .into(binding.ivDishImage)


                    // Replace the add icon with edit icon once the image is selected.
                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@AddUpdateDishActivity,
                            R.drawable.ic_vector_edit
                        )
                    )
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {

        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)

        // Initializing a new file
        // The bellow line return a directory in internal storage
        /**
         * The Mode Private here is
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        // Mention a file name to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }

        // Return the saved image absolute path
        return file.absolutePath
    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.iv_add_dish_image -> {

                customImageSelectionDialog()
                return
            }

            R.id.et_type -> {
                customItemsListDialog(
                    resources.getString(R.string.title_select_dish_type),
                    Constants.dishTypes(),
                    Constants.DISH_TYPE
                )
                return
            }

            R.id.et_category -> {
                customItemsListDialog(
                    resources.getString(R.string.title_select_dish_category),
                    Constants.dishCategories(),
                    Constants.DISH_CATEGORY
                )
                return
            }

            R.id.et_cooking_time -> {

                customItemsListDialog(
                    resources.getString(R.string.title_select_dish_cooking_time),
                    Constants.dishCookTime(),
                    Constants.DISH_COOKING_TIME
                )
                return
            }

            // TODO Step 6: Perform the action on button click.
            // START
            R.id.btn_add_dish -> {

                // Define the local variables and get the EditText values.
                // For Dish Image we have the global variable defined already.

                val title = binding.etTitle.text.toString().trim { it <= ' ' }
                val type = binding.etType.text.toString().trim { it <= ' ' }
                val category = binding.etCategory.text.toString().trim { it <= ' ' }
                val ingredients = binding.etIngredients.text.toString().trim { it <= ' ' }
                val cookingTimeInMinutes = binding.etCookingTime.text.toString().trim { it <= ' ' }
                val cookingDirection = binding.etDirectionToCook.text.toString().trim { it <= ' ' }

                when {

                    TextUtils.isEmpty(mImagePath) -> {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            resources.getString(R.string.err_msg_select_dish_image),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(title) -> {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            resources.getString(R.string.err_msg_enter_dish_title),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(type) -> {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            resources.getString(R.string.err_msg_select_dish_type),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(category) -> {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            resources.getString(R.string.err_msg_select_dish_category),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(ingredients) -> {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            resources.getString(R.string.err_msg_enter_dish_ingredients),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(cookingTimeInMinutes) -> {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            resources.getString(R.string.err_msg_select_dish_cooking_time),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(cookingDirection) -> {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        var dishID = 0
                        var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                        var favoriteDish = false

                        mFavDishDetails?.let {
                            if (it.id != 0) {
                                dishID = it.id
                                imageSource = it.imageSource
                                favoriteDish = it.favorite_dish
                            }
                        }

                        val favDishDetails: FavDish = FavDish(
                            mImagePath,
                            imageSource,
                            title,
                            type,
                            category,
                            ingredients,
                            cookingTimeInMinutes,
                            cookingDirection,
                            favoriteDish,
                            dishID
                        )

                        if(dishID == 0) {
                            favDishViewModel.insert(favDishDetails)

                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                "You successfully added your favorite dish details.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // You even print the log if Toast is not displayed on emulator
                            Log.e("Insertion", "Success")
                        }else{
                            favDishViewModel.update(favDishDetails)

                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                "You successfully updated your favorite dish details.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // You even print the log if Toast is not displayed on emulator
                            Log.e("Updating", "Success")
                        }
                        finish()
                    }
                }
            }
            // END
        }
    }

    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String) {

        mCustomListDialog = Dialog(this@AddUpdateDishActivity)

        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mCustomListDialog.setContentView(binding.root)

        binding.tvTitle.text = title

        // Set the LayoutManager that this RecyclerView will use.
        binding.rvList.layoutManager = LinearLayoutManager(this@AddUpdateDishActivity)
        // Adapter class is initialized and list is passed in the param.
        val adapter = CustomListItemAdapter(this@AddUpdateDishActivity, null ,itemsList, selection)
        // adapter instance is set to the recyclerview to inflate the items.
        binding.rvList.adapter = adapter
        //Start the dialog and display it on screen.
        mCustomListDialog.show()
    }

    fun selectedListItem(item: String, selection: String) {

        when (selection) {

            Constants.DISH_TYPE -> {
                mCustomListDialog.dismiss()
                binding.etType.setText(item)
            }

            Constants.DISH_CATEGORY -> {
                mCustomListDialog.dismiss()
                binding.etCategory.setText(item)
            }
            else -> {
                mCustomListDialog.dismiss()
                binding.etCookingTime.setText(item)
            }
        }
    }


    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {

            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
//                            Toast.makeText(this@AddUpdateDishActivity, "Granted", Toast.LENGTH_LONG)
//                                .show()
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAMERA)
                        } else {
                            showRationalDialogForPermissions()
                        }
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()

            dialog.dismiss()
        }
        binding.tvGallery.setOnClickListener {

            Dexter.withContext(this).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).withListener(object : PermissionListener {

                override fun onPermissionGranted(report: PermissionGrantedResponse?) {

//                    Toast.makeText(this@AddUpdateDishActivity, "Granted", Toast.LENGTH_LONG)
//                        .show()
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )

                    startActivityForResult(galleryIntent, GALLERY)

                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@AddUpdateDishActivity, "Denied", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("Not Granted")
            .setPositiveButton("Go To Settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}