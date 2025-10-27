@file:Suppress("AssignedValueIsNeverRead")

package com.jimandreas.opengl

import android.app.Activity
import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.widget.AdapterView.OnItemClickListener
import android.widget.SimpleAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jimandreas.opengl.displayobjects.ActivityDisplayObjects
import com.jimandreas.opengl.displayobjfile.ActivityDisplayObjFile
import com.jimandreas.opengl.displayscaled.ActivityDisplayScaled

import timber.log.Timber
import java.util.*
import androidx.core.net.toUri

class TableOfContents : ListActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null && BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setTitle(R.string.toc)
        setContentView(R.layout.table_of_contents)

        // Initialize data
        val data = ArrayList<Map<String, Any>>()
        val activityMapping = SparseArray<Class<out Activity>>()

        var i = 0

        run {
            val item = HashMap<String, Any>()
            item[ITEM_IMAGE] = R.drawable.display_objs
            item[ITEM_TITLE] = getText(R.string.objects_title)
            item[ITEM_SUBTITLE] = getText(R.string.objects_subtitle)
            data.add(item)
            activityMapping.put(i++, ActivityDisplayObjects::class.java)
        }

        run {
            val item = HashMap<String, Any>()
            item[ITEM_IMAGE] = R.drawable.cow
            item[ITEM_TITLE] = getText(R.string.activity_load_obj_file_title)
            item[ITEM_SUBTITLE] = getText(R.string.activity_load_obj_file_subtitle)
            data.add(item)
            activityMapping.put(i++, ActivityDisplayObjFile::class.java)
        }

        run {
            val item = HashMap<String, Any>()
            item[ITEM_IMAGE] = R.drawable.more_tris
            item[ITEM_TITLE] = getText(R.string.objects_multiple_title)
            item[ITEM_SUBTITLE] = getText(R.string.objects_multiple_subtitle)
            data.add(item)
            activityMapping.put(i++, ActivityDisplayScaled::class.java)
        }

        val dataAdapter = SimpleAdapter(
                this,
                data,
                R.layout.toc_item,
                arrayOf(ITEM_IMAGE, ITEM_TITLE, ITEM_SUBTITLE),
                intArrayOf(R.id.Image, R.id.Title, R.id.SubTitle))

        listAdapter = dataAdapter

        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val activityToLaunch = activityMapping.get(position)

            if (activityToLaunch != null) {
                val launchIntent = Intent(this, activityToLaunch)
                startActivity(launchIntent)
            }
        }

        /*
         * FAB button (floating action button = FAB) to get more information
         */
        val fab : FloatingActionButton = findViewById(R.id.info_fab)
        fab.setOnClickListener {
            Timber.i("FAB clicked")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = "https://github.com/jimandreas/OpenGLES-sandbox".toUri()
            startActivity(intent)
        }
    }

    companion object {
        private const val ITEM_IMAGE = "item_image"
        private const val ITEM_TITLE = "item_title"
        private const val ITEM_SUBTITLE = "item_subtitle"
    }
}
