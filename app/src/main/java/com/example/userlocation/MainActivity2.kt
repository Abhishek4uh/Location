package com.example.userlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.userlocation.databinding.ActivityMain2Binding
import com.example.userlocation.factory.IdfyViewmodelFactory
import com.example.userlocation.model.Config
import com.example.userlocation.model.Data
import com.example.userlocation.model.IdfyRequest
import com.example.userlocation.model.IdfyResponse
import com.example.userlocation.network.RetrofitHelper
import com.example.userlocation.repository.IdfyRepository
import com.example.userlocation.services.ApiServices
import com.example.userlocation.viewmodel.IdfyViewmodel
import java.io.File


class MainActivity2 : AppCompatActivity() {

    private lateinit var mBinding: ActivityMain2Binding
    private lateinit var viewmodel: IdfyViewmodel
    private var value:  ValueCallback<Array<Uri>>? = null
    private var imageURI: Uri? = null
    private lateinit var sharedPreferences:SharedPreferences

    companion object {
        private const val REFRENCE = "bb995a63455-033323c-sdkjfhjs481e-a7fe-13955d7dc34b"
        private const val CAMERA_PERMISSION_REQUEST_CODE  =  101
        private const val STORAGE_PERMISSION_REQUEST_CODE =  102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(mBinding.root)

        sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)


        //initialization......
        val apiServices= RetrofitHelper.getInstance().create(ApiServices::class.java)
        val repo=IdfyRepository(apiServices)
        viewmodel= ViewModelProvider(this,IdfyViewmodelFactory(repo))[IdfyViewmodel::class.java]


        mBinding.editText.setText(REFRENCE)
        var newRef=mBinding.editText.addTextChangedListener().toString()

        checkAndRequestPermissions()

        mBinding.btnStartLocationTracking.setOnClickListener {
            if(REFRENCE!=newRef){
                apiCall(newRef)
                initObserver()
            }
            else{
                Toast.makeText(this,"Change Credentials",Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val editor = sharedPreferences.edit()
        editor.putString("LastActivity", "MAINACTIVITY2")
        editor.apply()
    }

    private fun apiCall(refId: String) {
        Log.d("APPDATA_6",refId.toString())
        val request = IdfyRequest(
            config = Config("390c88da-8fde-4bc8-ba94-007791269e86"),
            data = Data(),
            referenceId = refId
        )
        mBinding.editText.visibility=View.GONE
        viewmodel.call(request)
    }

    private fun initObserver() {

        viewmodel.response.observe(this, Observer { res->
            setViews(res!!)
        })

        viewmodel.errorMsg.observe(this, Observer { res->

            if(res){
                mBinding.progress.visibility=View.GONE
                mBinding.wbView.visibility=View.GONE
                mBinding.infoText.visibility=View.VISIBLE
                mBinding.infoText.text="Something Went Wrong"
            }
        })

        viewmodel.loader.observe(this, Observer { res->
            if(res){
                mBinding.progress.visibility=View.VISIBLE
            }
            else{
                mBinding.progress.visibility=View.GONE
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setViews(res: IdfyResponse){
        mBinding.wbView.visibility=View.VISIBLE
        mBinding.editText.visibility=View.GONE

        mBinding.wbView.settings.javaScriptEnabled = true
        mBinding.wbView.settings.mediaPlaybackRequiresUserGesture = false
        mBinding.wbView.settings.allowFileAccess= true
        mBinding.wbView.settings.domStorageEnabled= true


        mBinding.wbView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                mBinding.progress.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                mBinding.progress.visibility = View.GONE
            }

            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                view?.loadUrl(res.captureLink.toString())
                return true
            }
        }
        mBinding.wbView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("APPDATA", consoleMessage?.message().toString())
                return false
            }
        }

        initializeWebView()
        mBinding.wbView.loadUrl(res.captureLink.toString())

    }

    private fun checkAndRequestPermissions(){
        //Camera permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }

        //Storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission granted, do your camera-related operations here
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission granted, do your storage-related operations here
                }
            }
        }
    }

    private fun initializeWebView() {
        mBinding.wbView.webChromeClient = object : WebChromeClient() {

            override fun onPermissionRequest(request: PermissionRequest?) {
                val requestedResources = request?.resources
                if (requestedResources != null) {
                    for (r in requestedResources) {
                        if (r == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                            request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                            break
                        }
                    }
                }
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                super.onPermissionRequestCanceled(request)
            }

            // Override onShowFileChooser to handle file upload
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?): Boolean {

                value = filePathCallback

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    requestPermissions(arrayOf( Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),CAMERA_PERMISSION_REQUEST_CODE)
                    return false
                }

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)


                val mediaStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    ), "IMG_FOLDER"
                )

                try {
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d("APPDATA","not able to create")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                imageURI = Uri.fromFile(
                    File(
                        mediaStorageDir.path + File.separator +
                                "profile_img.jpg"
                    )
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, 12)
                return true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            12 -> if (resultCode == RESULT_OK) {

                imageURI?.let { uri->
                    value?.onReceiveValue(arrayOf(uri))
                }
            }
        }
    }

}