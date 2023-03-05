package com.besaba.anvarov.orentsd.activity

//import com.sembozdemir.permissionskt.askPermissions
import android.Manifest
import android.content.*
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.DocListAdapter
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.databinding.ActivityMainBinding
import com.besaba.anvarov.orentsd.extensions.isExternalStorageReadable
import com.besaba.anvarov.orentsd.room.DocumentData
import com.besaba.anvarov.orentsd.room.NomenData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kotlinpermissions.KotlinPermissions
import net.gotev.uploadservice.ftp.FTPUploadRequest
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var fCamera: String? = ""
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val docRecyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val docListAdapter = DocListAdapter(this)
        docRecyclerView.adapter = docListAdapter
        docRecyclerView.layoutManager = LinearLayoutManager(this)

        val onDocClickListener = object : DocListAdapter.OnDocClickListener {
            override fun onDocClick(docs: DocumentData, del: Boolean) = when {
                del -> {
                    mAllViewModel.deleteDoc(docs.numDoc)
                    Toast.makeText(this@MainActivity, "delete " + docs.dateTime, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val intent = Intent(this@MainActivity, DocumentActivity::class.java)
                    intent.putExtra("documentNumber", docs.numDoc)
                    startActivity(intent)
                }
            }
        }
        docListAdapter.docAdapter(onDocClickListener)

        // беру новую или сущ. ViewModel
        mAllViewModel =  ViewModelProvider(this).get(AllViewModel::class.java)

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mAllViewModel.mAllDocs.observe(this, { docs ->
            // Update the cached copy of the words in the adapter.
            docs?.let { docListAdapter.setDocs(it) }
        })

        KotlinPermissions.with(this) // where this is an FragmentActivity instance
            .permissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onAccepted { permissions ->
                //List of accepted permissions
            }
            .onDenied { permissions ->
                //List of denied permissions
            }
            .onForeverDenied { permissions ->
                //List of forever denied permissions
            }
            .ask()
//        askPermissions( Manifest.permission.CAMERA,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE){
//            onGranted {
//            }
//            onDenied {
//            }
//            onShowRationale{
//            }
//            onNeverAskAgain{
//            }
//        }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { onDocument() }
        val ftp = findViewById<FloatingActionButton>(R.id.ftp)
        ftp.setOnClickListener { onUploadFTP() }
        val count = mAllViewModel.countNomen().toString()
        binding.countNomen.text = count
    }

    override fun onResume() {
        super.onResume()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fCamera = prefs.getString("reply", "0")
        if (fCamera!!.toInt() == 2) {
            binding.fab.hide()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_load -> {
                onLoad()
                true
            }
            R.id.action_settings -> {
                onSettings()
                true
            }
            R.id.action_about -> {
                onAbout()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun onSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun onLoad() {
        val json: String?
        lateinit var mCurrentNomen: NomenData

        if (isExternalStorageReadable()) {
            try {
                mAllViewModel.delNomen()
                if (mAllViewModel.countNomen() == 0) {
                    @Suppress("DEPRECATION") val path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    val fileNomen = "Nomenklatura.json"
                    val fileRead = File(path, fileNomen)
                    val inputStream: InputStream = fileRead.inputStream()
                    json = inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(json)
                    for (i in 0 until jsonArray.length()) {
                        val jsonobj = jsonArray.getJSONObject(i)
                        mCurrentNomen = NomenData(
                            jsonobj.getString("Barcode"),
                            jsonobj.getString("Name"),
                            jsonobj.getString("EI"),
                            jsonobj.getInt("MZOO")
                        )
                        mAllViewModel.insertNomen(mCurrentNomen)
                    }
                    Toast.makeText(
                        applicationContext,
                        "Номенклатура загружена в справочник!",
                        Toast.LENGTH_LONG
                    )
                        .show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Ошибка удаления файла списка номенклатур",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            } catch (e: IOException) {
                Toast.makeText(
                    applicationContext,
                    "Ошибка чтения файла списка номенклатур",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    private fun onDocument() {
        val intent = Intent(this@MainActivity, DocumentActivity::class.java)
        val numDoc = mAllViewModel.getNumberDocument()
        intent.putExtra("documentNumber", numDoc)
        startActivity(intent)
    }

    private fun onUploadFTP() {
        @Suppress("DEPRECATION") val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "deleted.json"
        val fileWrite = File(path, fileName)
        try {
            FTPUploadRequest(this@MainActivity, "ftp1.oas-orb.ru", 21)
                .setUsernameAndPassword("00000000118334", "T08FZVqk")
                .addFileToUpload(fileWrite.toString(), "/real")
                .startUpload()
        } catch (exc: Exception) {
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    // LeftScan = 27  Scan = 301  RightScan = 80
    // Esc = 111                  Ent = 66
    // F1 = 131                   F2 = 132
    // BS = 67                    ., = 56
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 66){
            onDocument()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

}
