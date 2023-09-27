package com.example.myapplication

//import android.R
//import com.davemorrissey.labs.subscaleview.ImageSource

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils.LABToColor
import androidx.core.graphics.ColorUtils.blendLAB
import androidx.core.graphics.ColorUtils.colorToLAB
import androidx.core.graphics.ColorUtils.distanceEuclidean
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.myapplication.databinding.SlideshowBinding
import jp.wasabeef.glide.transformations.BlurTransformation
import pl.droidsonroids.gif.GifImageView
import java.io.InputStream
import java.lang.Float.max


val DISPLAY_IMAGES_KEY = booleanPreferencesKey("display_images")
val DISPLAY_GIFS_KEY = booleanPreferencesKey("display_gifs")
val DISPLAY_VIDEOS_KEY = booleanPreferencesKey("display_videos")
val ONLY_PIXEL_ART_KEY = booleanPreferencesKey("only_pixel_art")
val DISPLAY_IMAGES_BAD_FOR_ORIENTATION_KEY = booleanPreferencesKey("display_images_bad_for_orientation")
val ORIENTATION_KEY = stringPreferencesKey("orientation")
val DISPLAY_LONG_IMAGES_KEY = booleanPreferencesKey("display_long_images")
val SLIDE_DURATION_KEY = longPreferencesKey("slide_duration")
val DEBUG_KEY = booleanPreferencesKey("debug")

class MainActivity : FragmentActivity() {

    public val timeoutHandler: Handler = Handler(Looper.getMainLooper())
    public var actionTimeout: Runnable? = null
    var slideshowIndex = 0
    lateinit var viewPager: ViewPager2
    lateinit var imageView: SubsamplingScaleImageView
    var totalItems: Int = 0
    lateinit var allSlideshowOrder: List<String>
    lateinit var menuView: View
    lateinit var settingsView: View
    lateinit var galleryView: View
    var menuVisible = false
    var viewLocked = false

    lateinit var binding: SlideshowBinding

    var displayImages = true
    var displayGifs = true
    var displayVideos = true
    var onlyPixelArt = false
    var orientation = "landscape"
    var displayImagesBadForOrientation = true
    var displayLongImages = true
    val slideDuration: Long = 10000
    var debug = false
//
//    var tempDisplayImages = true
//    var tempDisplayGifs = true
//    var tempDisplayVideos = true
//    var tempOnlyPixelArt = false
//    var tempOrientation = "landscape"
//    var tempDisplayImagesBadForOrientation = true
//    var tempDisplayLongImages = true

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    val pixelArtList = listOf(
        "child0.png",
        "child1.png",
        "MakDeetsMuch1645881808448536576.png",
        "zaebucca1609107296000286720.png",
        "ioruko1445779815252979716.jpg",
        "kianamosser1639029096918446081.jpg",
        "illufinch1404708429168463874.png",
        "MakDeetsMuch1535917608754892801.png",
        "KadaburaDraws1566865598193319938.png",
        "schultz14171299369958174945282.png",
        "illufinch1423042067857506304.png",
        "illufinch1408215520780767234.png",
        "illufinch1363758247669207041.png",
        "16pxl1649815205503807488.jpg",
        "kianamosser1639029047685681152.jpg",
        "kryssalian1343165024051859456.jpg",
        "illufinch1641719021589639169.png",
        "kryssalian1343165024064442371.jpg",
        "16pxl1649815205516304386.jpg",
        "KadaburaDraws1445494627881013257.png",
        "Anima_nel1643373804079529986.png",
        "KadaburaDraws1294764612995424257.png",
        "kianamosser1630099699284992001.jpg",
        "MakDeetsMuch1650512837373050880.png",
        "emimonserrate998598093586116609.jpg",
        "16pxl1640398606057488386.png",
        "illufinch1394234662797471745.png",
        "illufinch1408215725278253059.jpg",
        "butterberrycafe1608882937285738497.png",
        "MakDeetsMuch1652390993927491584.png",
        "kavast1646923563553759232.png",
        "hectNishi1120661792454135808.png",
        "kianamosser1639029047685685248.jpg",
        "hectNishi1120661813266243584.jpg",
        "hectNishi1120661801291538432.jpg",
        "kryssalian1343165023997325312.jpg",
        "illufinch1411945263636254723.png",
        "MakDeetsMuch1587012578659930112.jpg",
        "KadaburaDraws1421171986026409984.png",
        "KadaburaDraws1133491205168353283.png",
        "ThrillHoGaming1599467925223579648.png",
        "zaebucca1640818838789816320.png",
        "001_31_1368235654887272452.png",
        "zaebucca1579928212259364878.png",
        "MakDeetsMuch1650512940217298945.png",
        "thisislux1395769754703601665.png",
        "16pxl1649815205512192000.jpg",
        "illufinch1432998452070273030.png",
        "zaebucca1646975709259112449.png",
        "16pxl1640398587384446976.png",
        "MakDeetsMuch1551594605996429312.png",
    )

//    public val menuTimeoutHandler: Handler = Handler(Looper.getMainLooper())
    public var menuActionTimeout: Runnable? = null

    //    lateinit var adapter: ViewPagerFragmentAdapter
    lateinit var adapter: MainActivity.ViewPagerFragmentAdapter3

    //
    val proceedSlideshow = object : Runnable {
        override fun run() {
            Log.v("proceeeding", "proceeding")
            if (slideshowIndex == totalItems) {
                Log.v("proceeeding", "in here")
                slideshowIndex = 0;
            }
            viewPager.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT
            viewPager.setCurrentItem(slideshowIndex++, true);
            Log.v("slideshow index new", slideshowIndex.toString())
            val resetOffscreenPageLimit = object : Runnable {
                override fun run() {
                    viewPager.offscreenPageLimit = 2
                }
            }
            timeoutHandler.postDelayed(resetOffscreenPageLimit, 2000)

            actionTimeout = this
            timeoutHandler.postDelayed(actionTimeout!!, slideDuration)
        }
    }

    val closeMenu = Runnable {
        menuView.animate()
            .alpha(0f)
            .setDuration(600.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    menuView.visibility = View.GONE
                }
            })
        menuVisible = false;
    }

    fun imageNotFiltered(filepath: String): Boolean {
//        if (filepath == "POISONP1NK1391051013075898369.jpg") {
//            return true;
//        } else {
//            return false;
//        }
//        if (filepath == "Kaneblob994393461775187968.jpg") {
//            return true;
//        } else {
//            return false;
//        }
//
//        if (filepath == "ed73f372d71d2b52c92e265eebdebf7d3c59c65f.png") {
//            return true;
//        } else {
//            return false;
//        }

//        if (listOf(
//                "POISONP1NK1391051013075898369.jpg",
//                "Kaneblob994393461775187968.jpg",
//            "udaqueness1004158631993053184.jpg",
//            "ohr_cn1353314738793398272.jpg",
//            "ohr_cn1351103285026050055.jpg",
//            "onemegawatt1590430556235321344.jpg").contains(filepath)) {
//            return true;
//        } else {
//            return false;
//        }

        return if (!displayImages && !(filepath.endsWith(".gif") || filepath.endsWith(".mp4"))) {
            false
        } else if (!displayGifs && filepath.endsWith(".gif")) {
            false
        } else if (!displayVideos && filepath.endsWith(".mp4")) {
            false
        } else if (onlyPixelArt && !pixelArtList.contains(filepath)) {
            false
        } else {
            true
        }
    }

    private fun getAllImagePaths(context: Context): List<String> {
        val assetsList = assets.list("")
        return if (assetsList != null) {
//            var displayImages

            assetsList.filter { s -> s != "images" && s != "webkit" && imageNotFiltered(s) }
//                assetsList.filter { s -> s != "images" && s != "webkit" && s.endsWith(".gif") }
//            assetsList.filter { s -> s != "images" && s != "webkit" && (s.endsWith(".gif") || s.endsWith(".mp4")) }
            //            assetsList.filter { s -> s != "images" && s != "webkit" && s == "cannonbreed1603175701049327616_1.gif" }
        } else {
            listOf()
        }
    }

    fun toggleLock(v: View) {
        Log.v("toggle lock", "here")
        if (viewLocked) {
            // need to have action potentially zoom out
            actionTimeout = proceedSlideshow
            timeoutHandler.postDelayed(actionTimeout!!, slideDuration / 2)
            (v as ImageButton).setImageResource(R.drawable.baseline_lock_open_96);
        } else {
            if (actionTimeout != null) {
                timeoutHandler.removeCallbacks(actionTimeout!!)
                actionTimeout = null
            }
            (v as ImageButton).setImageResource(R.drawable.baseline_lock_96);
        }
        viewLocked = !viewLocked
    }

    fun applySettings(v: View) {
        displayImages = binding.slideshowsettings!!.displayImages
        displayGifs = binding.slideshowsettings!!.displayGifs
        displayVideos = binding.slideshowsettings!!.displayVideos
        onlyPixelArt = binding.slideshowsettings!!.onlyPixelArt
        orientation = binding.slideshowsettings!!.orientation
        displayImagesBadForOrientation = binding.slideshowsettings!!.displayImagesBadForOrientation
        displayLongImages = binding.slideshowsettings!!.displayLongImages
        debug = binding.slideshowsettings!!.debug

//        // save to data store
//        dataStore.edit { preferences ->
//            preferences[DISPLAY_IMAGES_KEY] = displayImages
//            preferences[DISPLAY_GIFS_KEY] = displayGifs
//            preferences[DISPLAY_VIDEOS_KEY] = displayVideos
//            preferences[ONLY_PIXEL_ART_KEY] = onlyPixelArt
//            preferences[DISPLAY_IMAGES_BAD_FOR_ORIENTATION_KEY] = displayImagesBadForOrientation
//            preferences[ORIENTATION_KEY] = orientation
//            preferences[DISPLAY_LONG_IMAGES_KEY] = displayLongImages
////            preferences[SLIDE_DURATION_KEY] = showCompleted
//        }
        // reshuffle and close settings
        allSlideshowOrder = getSlideshowOrder(this)
        adapter = ViewPagerFragmentAdapter3(this, allSlideshowOrder)
        viewPager.adapter = adapter
        closeSettings(v)
    }

    fun openSettings(v: View) {
        binding.slideshowsettings!!.displayImages = displayImages
        binding.slideshowsettings!!.displayGifs = displayGifs
        binding.slideshowsettings!!.displayVideos = displayVideos
        binding.slideshowsettings!!.onlyPixelArt = onlyPixelArt
        binding.slideshowsettings!!.orientation = orientation
        binding.slideshowsettings!!.displayImagesBadForOrientation = displayImagesBadForOrientation
        binding.slideshowsettings!!.displayLongImages = displayLongImages
        binding.slideshowsettings!!.debug = debug
        settingsView.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(200.toLong())
                .setListener(null)
        }
        if (actionTimeout != null) {
            timeoutHandler.removeCallbacks(actionTimeout!!)
            actionTimeout = null
        }
        if (menuActionTimeout != null) {
            timeoutHandler.removeCallbacks(menuActionTimeout!!)
            menuActionTimeout = null
        }
    }

    fun openGallery(v: View) {
        galleryView.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(200.toLong())
                .setListener(null)
        }
        if (actionTimeout != null) {
            timeoutHandler.removeCallbacks(actionTimeout!!)
            actionTimeout = null
        }
        if (menuActionTimeout != null) {
            timeoutHandler.removeCallbacks(menuActionTimeout!!)
            menuActionTimeout = null
        }
    }

    fun closeSettings(v: View) {
        settingsView.animate()
            .alpha(0f)
            .setDuration(200.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    settingsView.visibility = View.GONE
                }
            })
        actionTimeout = proceedSlideshow
        timeoutHandler.postDelayed(actionTimeout!!, slideDuration / 2)
        menuActionTimeout = closeMenu
        timeoutHandler.postDelayed(menuActionTimeout!!, 6000)
//        tempDisplayImages = displayImages
//        tempDisplayGifs = displayGifs
//        tempDisplayVideos = displayVideos
//        tempOnlyPixelArt = onlyPixelArt
//        tempOrientation = orientation
//        tempDisplayImagesBadForOrientation = displayImagesBadForOrientation
//        tempDisplayLongImages = displayLongImages
    }

    fun closeGallery(v: View) {
        galleryView.animate()
            .alpha(0f)
            .setDuration(200.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    galleryView.visibility = View.GONE
                }
            })
        actionTimeout = proceedSlideshow
        timeoutHandler.postDelayed(actionTimeout!!, slideDuration / 2)
        menuActionTimeout = closeMenu
        timeoutHandler.postDelayed(menuActionTimeout!!, 6000)
    }

    fun toggleMenu() {
        Log.v("inner toggle", "here")
        if (menuVisible) {
            menuView.animate()
                .alpha(0f)
                .setDuration(600.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        menuView.visibility = View.GONE
                    }
                })
            if (menuActionTimeout != null) {
                timeoutHandler.removeCallbacks(menuActionTimeout!!)
                menuActionTimeout = null
            }

            // clear timeout if it's set
        } else {
            menuView.apply {
                // Set the content view to 0% opacity but visible, so that it is visible
                // (but fully transparent) during the animation.
                alpha = 0f
                visibility = View.VISIBLE

                // Animate the content view to 100% opacity, and clear any animation
                // listener set on the view.
                animate()
                    .alpha(1f)
                    .setDuration(600.toLong())
                    .setListener(null)
            }
            // set timeout to hide it
            menuActionTimeout = closeMenu
            timeoutHandler.postDelayed(menuActionTimeout!!, 6000)
        }
        menuVisible = !menuVisible;
    }


    private fun getSlideshowOrder(context: Context): List<String> {
        val allImagePaths = getAllImagePaths(context)
        totalItems = allImagePaths.size
        return allImagePaths.shuffled();
    }

//    suspend fun Context.getValueByKey(key: Preferences.Key<*>): Any? {
//        val value = dataStore.data
//            .map {
//                it[key]
//            }
//        return value.firstOrNull()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        StrictMode.setThreadPolicy(
//            ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork() // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .build()
//        )
//        StrictMode.setVmPolicy(
//            VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .penaltyDeath()
//                .build()
//        )
//        super.onCreate(savedInstanceState)
        Log.d("here15", "here15")
        allSlideshowOrder = getSlideshowOrder(this)
//
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.slideshow)
//
//        val displayImagesFlow: Flow<Boolean> = dataStore.data
//            .map { preferences ->
//                // No type safety.
//                preferences[DISPLAY_IMAGES_KEY] ?: displayImages
//            }
//        val displayGifsFlow: Flow<Boolean> = dataStore.data
//            .map { preferences ->
//                // No type safety.
//                preferences[DISPLAY_GIFS_KEY] ?: displayGifs
//            }
//        val displayVideosFlow: Flow<Boolean> = dataStore.data
//            .map { preferences ->
//                // No type safety.
//                preferences[DISPLAY_VIDEOS_KEY] ?: displayVideos
//            }
//        val onlyPixelArtFlow: Flow<Boolean> = dataStore.data
//            .map { preferences ->
//                // No type safety.
//                preferences[ONLY_PIXEL_ART_KEY] ?: onlyPixelArt
//            }
//        val debugFlow: Flow<Boolean> = dataStore.data
//            .map { preferences ->
//                // No type safety.
//                preferences[DEBUG_KEY] ?: debug
//            }
//
//        runBlocking(Dispatchers.IO) {
//            displayImages = displayImagesFlow.first()
//            displayGifs = displayGifsFlow.first()
//            displayVideos = displayVideosFlow.first()
//            onlyPixelArt = onlyPixelArtFlow.first()
//            debug = debugFlow.first()
//        }

//        mapUserPreferences(dataStore.data.first().toPreferences())

        binding = DataBindingUtil.setContentView(
            this, R.layout.slideshow)

//        binding.slideshowSettings = Settings(this)
//        binding.test = "test"
//        val viewmodel = Settings(this, [])
        binding.slideshowsettings = Settings()
        binding.slideshowsettings!!.displayImages = displayImages
        binding.slideshowsettings!!.displayGifs = displayGifs
        binding.slideshowsettings!!.displayVideos = displayVideos
        binding.slideshowsettings!!.onlyPixelArt = onlyPixelArt
        binding.slideshowsettings!!.orientation = orientation
        binding.slideshowsettings!!.displayImagesBadForOrientation = displayImagesBadForOrientation
        binding.slideshowsettings!!.displayLongImages = displayLongImages
        binding.slideshowsettings!!.debug = debug

        Log.v("binding settings", binding.slideshowsettings!!.displayImages.toString())

//        try {
//            val recyclerViewField: Field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
//            recyclerViewField.isAccessible = true
//            val recyclerView = recyclerViewField.get(viewPager)
//            val touchSlopField: Field = RecyclerView::class.java.getDeclaredField("mTouchSlop")
//            touchSlopField.isAccessible = true
//            touchSlopField.set(
//                recyclerView,
//                touchSlopField.get(recyclerView) * 10
//            ) //6 is empirical value
//        } catch (ignore: java.lang.Exception) {
//        }

        menuView = findViewById(R.id.menu)
//        menuView.visibility = View.GONE
//
        settingsView = findViewById(R.id.settings)
//        settingsView.visibility = View.GONE
//
        galleryView = findViewById(R.id.gallery)
//        galleryView.visibility = View.GONE

//        findViewById<CheckBox>(R.id.displayImageCheckbox).apply {
//            checked = tempDisplayImages
//        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
//
//        val listener = object : GestureDetector.SimpleOnGestureListener() {
//            override fun onSingleTapConfirmed (e: MotionEvent): Boolean {
//                // do whatever
////                return super.onSingleTapConfirmed(e)
//                Log.v("listener", "here3")
//                toggleMenu()
//                return true
//            }
//        }
//
//        val detector = GestureDetectorCompat(this, listener)
//
//        val touchListener = OnTouchListener { v, event -> // pass the events to the gesture detector
//            // a return value of true means the detector is handling it
//            // a return value of false means the detector didn't
//            // recognize the event
//            Log.v("touch listener", "here2")
//            detector.onTouchEvent(event)
//        }


        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager)
        viewPager.offscreenPageLimit = 2
//        viewPager.setOffscreenPageLimit(1)

//        viewPager.setOnTouchListener(touchListener);

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
//                if (allSlideshowOrder[position].endsWith(".mp4")) {
////                    viewPager.getItemDecorationAt(position).seekTo(0)
//
//                } else if (allSlideshowOrder[position].endsWith(".gif")) {
//
//                }
                slideshowIndex = position + 1
                if (binding.slideshowsettings!!.debug) {
                    binding.slideshowsettings!!.debugFilepath = allSlideshowOrder[position]
                    binding.slideshowsettings!!.memoryInfo = getAvailableMemory()
                }

                Log.v("slideshow item", position.toString())
//                fragmentManager.findFragmentById(viewPager.getItemId(index).toInt())
                super.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == SCROLL_STATE_IDLE) {
//                    if (allSlideshowOrder[viewPager.currentItem].endsWith(".mp4")) {
//                        val myFragment: VideoFragment = supportFragmentManager.findFragmentByTag("f" + viewPager.currentItem) as VideoFragment
////                        val startVideo = object : Runnable {
////                            override fun run() {
////                                myFragment.videoView!!.start()
////                            }
////                        }
////
////                        timeoutHandler.postDelayed(startVideo!!, 500)
//                        myFragment.videoView!!.start()
//                    }
                        // set max offscreen
//                    viewPager.offscreenPageLimit = 1
                    viewPager.offscreenPageLimit = 2
                } else if (state == SCROLL_STATE_DRAGGING) {
                    viewPager.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT
                }
            }
        })

        // The pager adapter, which provides the pages to the view pager widget.
        Log.v("assets", allSlideshowOrder!!.joinToString(" "))
        adapter = ViewPagerFragmentAdapter3(this, allSlideshowOrder)
        viewPager.adapter = adapter

        actionTimeout = proceedSlideshow
        timeoutHandler.postDelayed(actionTimeout!!, slideDuration)

    }

    private fun getAvailableMemory(): ActivityManager.MemoryInfo {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
        }
    }

    inner class ViewPagerFragmentAdapter3(
        fragment: FragmentActivity,
        slideshowOrder: List<String>
    ) : FragmentStateAdapter(fragment) {

        val innerAllSlideshowOrder = slideshowOrder

        override fun getItemCount(): Int = totalItems

        override fun createFragment(position: Int): Fragment {
            // Return a NEW fragment instance in createFragment(int)
            val filepath = innerAllSlideshowOrder[position]
            Log.v("filepath", filepath)
            return if (filepath.endsWith(".mp4")) {
                VideoFragment(filepath)
//                PlayerFragment(filepath)
            } else if (filepath.endsWith(".gif")) {
                GifFragment(filepath)
            } else {
                ImageFragment(filepath)
            }

        }

    }
//
//    inner class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
//
//        @Override
//        public boolean onDown(MotionEvent event) {
//            Log.d("TAG","onDown: ");
//
//            // don't return false here or else none of the other
//            // gestures will work
//            return true;
//        }
//
//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent e) {
//            Log.i("TAG", "onSingleTapConfirmed: ");
//            return true;
//        }
//
//        @Override
//        public void onLongPress(MotionEvent e) {
//            Log.i("TAG", "onLongPress: ");
//        }
//
//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            Log.i("TAG", "onDoubleTap: ");
//            return true;
//        }
//
//        @Override
//        public boolean onScroll(MotionEvent e1, MotionEvent e2,
//            float distanceX, float distanceY) {
//            Log.i("TAG", "onScroll: ");
//            return true;
//        }
//
//        @Override
//        public boolean onFling(MotionEvent event1, MotionEvent event2,
//            float velocityX, float velocityY) {
//            Log.d("TAG", "onFling: ");
//            return true;
//        }
//    }
}

//class Settings(): ViewModel(), Observable {
////    var displayImages = true
////    var displayGifs = true
////    var displayVideos = true
////    var onlyPixelArt = false
////    var orientation = "landscape"
////    var displayImagesBadForOrientation = true
////    var displayLongImages = true
//
//    var displayImages = true
//    var displayGifs = true
//    var displayVideos = true
//    var onlyPixelArt = false
//    var orientation = "landscape"
//    var displayImagesBadForOrientation = true
//    var displayLongImages = true
//
//    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
//
//    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
//        callbacks.add(callback)
//    }
//
//    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
//        callbacks.remove(callback)
//    }
//}

class Settings(): BaseObservable() {
//    var displayImages = true
//    var displayGifs = true
//    var displayVideos = true
//    var onlyPixelArt = false
//    var orientation = "landscape"
//    var displayImagesBadForOrientation = true
//    var displayLongImages = true

//    var displayImages = true
    @get:Bindable
    var displayImages: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.displayImages)
        }
    @get:Bindable
    var displayGifs: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.displayGifs)
        }
    @get:Bindable
    var displayVideos: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.displayVideos)
        }
    @get:Bindable
    var onlyPixelArt: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.onlyPixelArt)
        }
    @get:Bindable
    var debug: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.debug)
        }
    @get:Bindable
    var debugFilepath: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.debugFilepath)
        }
    @get:Bindable
    var memoryInfo: ActivityManager.MemoryInfo? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.memoryInfo)
        }
    var orientation = "landscape"
    var displayImagesBadForOrientation = true
    var displayLongImages = true

//    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
//
//    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
//        callbacks.add(callback)
//    }
//
//    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
//        callbacks.remove(callback)
//    }
}

class CustomSubsamplingScaleImageView(context: Context, attributeSet: AttributeSet): CustomSubsamplingScaleImageViewJava(context, attributeSet), View.OnTouchListener {
    var initialScale: Float? = null
    var initialCenter: PointF? = null
    var main: MainActivity? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (this.isReady && !main!!.viewLocked) {

            Log.v("here1", "here1")
            if (main!!.actionTimeout != null) {
                main!!.timeoutHandler.removeCallbacks(main!!.actionTimeout!!)
            }

//            Log.v("scale", imageView.scale.toString())

            val resetZoom = Runnable {
                Log.v("reset", "reset")
//                                imageView.resetScaleAndCenter()
                this.animateScaleAndCenter(initialScale!!, initialCenter!!)!!
                    .withDuration(main!!.slideDuration / 4)
                    .withEasing(SubsamplingScaleImageView.EASE_IN_OUT_QUAD)
                    .start()
                main!!.actionTimeout = main!!.proceedSlideshow
                main!!.timeoutHandler.postDelayed(main!!.actionTimeout!!, main!!.slideDuration / 2)
            }

            main!!.actionTimeout = resetZoom
            main!!.timeoutHandler.postDelayed(main!!.actionTimeout!!, main!!.slideDuration / 2)
        }
        return super.onTouchEvent(event);
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event !== null) {
            return super.onTouchEvent(event);
        }
        return false
    }
}

class ImageFragment(filepath: String) : Fragment() {

    val innerFilepath = filepath

//    fun blur(image: Bitmap?): Bitmap? {
//        if (null == image) return null
//        val outputBitmap = Bitmap.createBitmap(image)
//        val renderScript = RenderScript.create(this)
//        val tmpIn = Allocation.createFromBitmap(renderScript, image)
//        val tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap)
//
//        //Intrinsic Gausian blur filter
//        val theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
//        theIntrinsic.setRadius(BLUR_RADIUS)
//        theIntrinsic.setInput(tmpIn)
//        theIntrinsic.forEach(tmpOut)
//        tmpOut.copyTo(outputBitmap)
//        return outputBitmap
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun blendCloseColors(pixel1: Int, pixel2: Int, pixel3: Int, pixel4: Int): Int? {
        val a1 = Color.alpha(pixel1)
        val a2 = Color.alpha(pixel2)
        val a3 = Color.alpha(pixel3)
        val a4 = Color.alpha(pixel4)


        if (a1 == 0 && a2 == 0 && a3 == 0 && a4 == 0) {
            return pixel1;
        }

        val lab1 = DoubleArray(3)
        val lab2 = DoubleArray(3)
        val lab3 = DoubleArray(3)
        val lab4 = DoubleArray(3)
        colorToLAB(pixel1, lab1)
        colorToLAB(pixel2, lab2)
        colorToLAB(pixel3, lab3)
        colorToLAB(pixel4, lab4)
        Log.v("color distance:", distanceEuclidean(lab1, lab2).toString())
        return if (distanceEuclidean(lab1, lab2) <= 7 && distanceEuclidean(lab1, lab3) <= 7 && distanceEuclidean(lab1, lab4) <= 7 && distanceEuclidean(lab3, lab4) <= 7) {
            val resultLab = DoubleArray(3)
            blendLAB(lab1, lab2, 0.5, resultLab)
            LABToColor(resultLab[0], resultLab[1], resultLab[2])
        } else {
            null
        }
//        return distanceEuclidean(lab1, lab2) <= 7;
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
//            filepath: String,
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
// Inflate the layout for this fragment
//            return inflater.inflate(R.layout.image, container, false)
        val view = LayoutInflater.from(context).inflate(R.layout.image, null, false)
        val imageView: CustomSubsamplingScaleImageView = view.findViewById(R.id.imageView);
        imageView.main = activity as MainActivity?

        imageView.isPixelArt = imageView.main!!.pixelArtList.contains(innerFilepath)

//        override imageView.onTouchEvent =
//        imageView.setMinimumDpi(4)
        imageView.maxScale = 64f
//        CustomSubsamplingScaleImageView.setPreferredBitmapConfig(ARGB_8888)
//        imageView.setRegionDecoderClass(MyImageRegionDecoder)
        imageView.setOnImageEventListener(object : CustomSubsamplingScaleImageViewJava.OnImageEventListener {

            override fun onReady() {
//                Log.d(":nht...", "onReady")
//                super.onReady()
            }

            override fun onImageLoaded() {
                imageView.initialScale = imageView.scale
                imageView.initialCenter = imageView.center
            }

            override fun onPreviewLoadError(e: Exception) {
//                Log.d(":nht...", "onPreviewLoadError")
            }

            override fun onImageLoadError(e: Exception) {
//                Log.d(":nht...", "onImageLoadError")
            }

            override fun onTileLoadError(e: Exception) {
//                Log.d(":nht...", "onTileLoadError")
            }

            override fun onPreviewReleased() {
//                TODO("Not yet implemented")
            }
        })

        val imageViewBackground: ImageView = view.findViewById(R.id.imageViewBackground);
        val ims: InputStream = requireActivity().getAssets().open(innerFilepath)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(ims, null, options)

        val width_t = options.outWidth
        val height_t = options.outHeight

        val decoder: BitmapRegionDecoder? = BitmapRegionDecoder.newInstance(ims, false)

        val region = decoder!!.decodeRegion(Rect(0, 0, 1, 1), null)
        val pixel1: Int = region.getPixel(0, 0)

        val region2 = decoder!!.decodeRegion(Rect(width_t-2, height_t-2, width_t-1, height_t-1), null)
        val pixel2: Int = region2.getPixel(0, 0)

        val region3 = decoder!!.decodeRegion(Rect(width_t-2, 0, width_t-1, 1), null)
        val pixel3: Int = region2.getPixel(0, 0)

        val region4 = decoder!!.decodeRegion(Rect(0, height_t-2, 1, height_t-1), null)
        val pixel4: Int = region2.getPixel(0, 0)

        ims.close();

        Log.v("width", width_t.toString())
        Log.v("height", height_t.toString())
        Log.v("pixel 1", pixel1.toString())
        Log.v("pixel 2", pixel2.toString())

        val backgroundColor = blendCloseColors(pixel1, pixel2, pixel3, pixel4)

        if (backgroundColor != null) {
            imageViewBackground.setBackgroundColor(backgroundColor);
        } else {
            val scale = if (imageView.main!!.binding.slideshowsettings!!.orientation == "landscape") {
                max(1920.0f / width_t, 1080.0f / height_t)
            } else {
                max(1080.0f / width_t, 1920.0f / height_t)
            }

            val m = Matrix()
            m.reset()
            m.postScale(scale, scale);
            imageViewBackground.setImageMatrix(m);

            Glide.with(this)
                .load(Uri.parse("file:///android_asset/$innerFilepath"))
                .apply(bitmapTransform(BlurTransformation(25, 3)))
                .into(imageViewBackground)

            val borderView: View = view.findViewById(R.id.borderGradient)
            borderView.visibility = View.VISIBLE
        }

        imageView.setImage(ImageSource.asset(innerFilepath))

        return view
    }

    }

@UnstableApi class PlayerFragment(filepath: String) : Fragment() {
    val innerFilepath = filepath
    var playerView: PlayerView? = null
//    var surfaceView: SurfaceView? = null
    var player: ExoPlayer? = null

    override fun onCreateView(
//            filepath: String,
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = LayoutInflater.from(context).inflate(R.layout.player, null, false)
        player = ExoPlayer.Builder(requireContext()).build()

        playerView = view.findViewById(R.id.playerView);
        playerView!!.player = player
        playerView!!.useController = false

//        surfaceView = view.findViewById(R.id.surfaceView)
//        player!!.setVideoSurfaceView(surfaceView)
//        player!!.videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
//        player!!.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
//        player!!.videoScalingMode = 1
//        player!!.set


        val filename = innerFilepath.split('.')[0].lowercase()

        val uriPath = "android.resource://" + requireActivity().packageName + "/raw/" + filename
        val uri = Uri.parse(uriPath)
//        playerView!!.setVideoURI(uri)
//
//        playerView!!.setOnPreparedListener(OnPreparedListener { mp -> mp.isLooping = true })
//
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(uriPath)
//        val width =
//            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
////        val height =
////            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
//        retriever.release()
//
//        surfaceView!!.layoutParams.width = width;

        // Build the media item.
        val mediaItem = MediaItem.fromUri(uri)
// Set the media item to be played.
        player!!.setMediaItem(mediaItem)
        player!!.repeatMode = REPEAT_MODE_ONE
// Prepare the player.
        player!!.prepare()
// Start the playback.
        player!!.play()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        player!!.release()
    }

//    fun onVideoSizeChanged(
//        width: Int,
//        height: Int,
//        unappliedRotationDegrees: Int,
//        pixelRatio: Float
//    ) {
//        if (!mRatioAlreadyCalculated && mVideoWidthHeightRatio !== width.toFloat() / height) {
//            mVideoWidthHeightRatio = width.toFloat() / height * pixelRatio
//            mRatioAlreadyCalculated = true
//        }
//        updateVideoRatio()
//    }
}


class VideoFragment(filepath: String) : Fragment() {

    val innerFilepath = filepath
    var videoView: VideoView? = null

    private val mOnErrorListener: MediaPlayer.OnErrorListener =
        MediaPlayer.OnErrorListener { _, _, _ -> // Your code goes here
            Log.v("video view", "handling error?")
//            videoView!!.seekTo(0)
            videoView!!.start()
            true
        }

    override fun onCreateView(
//            filepath: String,
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
// Inflate the layout for this fragment
//            return inflater.inflate(R.layout.image, container, false)
        val view = LayoutInflater.from(context).inflate(R.layout.video, null, false)
        videoView = view.findViewById(R.id.videoView);

        val filename = innerFilepath.split('.')[0].lowercase()
//        val uriPath = "android.resource://com.example.myapplication/assets/$filename"
//        val uriPath = "android.resource://com.example.myapplication/assets/$innerFilepath"
        val uriPath = "android.resource://" + requireActivity().packageName + "/raw/" + filename
        val uri = Uri.parse(uriPath)
        videoView!!.setVideoURI(uri)


        videoView!!.setOnErrorListener(mOnErrorListener)
//        videoView.canPause(false)
//        videoView!!.start()
//        videoView!!.setOnPreparedListener(OnPreparedListener { mp -> mp.isLooping = true })
        videoView!!.setOnPreparedListener(OnPreparedListener { mp -> mp.isLooping = true; videoView!!.start() })
        return view
    }

    override fun onDestroy() {
        videoView!!.stopPlayback();
        super.onDestroy()
    }

//    override fun onResume() {
//        super.onResume()
//        videoView!!.start()
//    }
}

class GifFragment(filepath: String) : Fragment() {

    val innerFilepath = filepath

    @SuppressLint("ResourceType")
    override fun onCreateView(
//            filepath: String,
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
// Inflate the layout for this fragment
//            return inflater.inflate(R.layout.image, container, false)
        val view = LayoutInflater.from(context).inflate(R.layout.gif, null, false)
        val gifView: GifImageView = view.findViewById(R.id.gifView);

        val filename = innerFilepath.split('.')[0].lowercase()
        val resId = resources.getIdentifier(
            filename, "raw",
            requireActivity().packageName
        )
        gifView.setImageResource(resId)

        return view
    }

}

//class MyViewPagerContainer(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {
//    fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//        Log.v("intercepted touch", "here4")
//        return true
//    }
//}

class GestureTap : SimpleOnGestureListener() {
    override fun onDoubleTap(e: MotionEvent): Boolean {
        Log.i("onDoubleTap :", "" + e.action)
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        Log.i("onSingleTap :", "" + e.action)
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        Log.i("onDown :", "" + e.action)
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.i("onSingleTapUp :", "" + e.action)
        return false
    }
}

class ViewPagerContainer : RelativeLayout {
    lateinit var mainActivity: MainActivity
    lateinit var detector: GestureDetector

    constructor(context: Context?) : super(context) {
        detector = GestureDetector(context, GestureTap())
//        context.getApplicationContext().getCurrentActivity()
        mainActivity = context as MainActivity
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        detector = GestureDetector(context, GestureTap())
        mainActivity = context as MainActivity
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        detector = GestureDetector(context, GestureTap())
        mainActivity = context as MainActivity
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        Log.v("intercepted touch", "here4")
        if (detector.onTouchEvent(ev)) {
//            val mainActivity = activity as MainActivity?
            mainActivity.toggleMenu()
        }
        return false
    }
}
//
// figure out screen pinning
// some gifs (i think not images) aren't being displayed
// if unlocking on image, unzoom first before proceeding
// reshuffle on returning to start
// might need to make explicit list of blurred background/colored background -or, at least, overrides. or could check if pixels are within some distance of each other.
// swiping is finnicky
// loading from memory card
// pushing updates over internet
// long images
// image align (left/right/up/down/center)
// have gallery view that you can click on to view full screen and start shuffle from there
// keep scroll position in gallery
// i guess... gallery should correspond to current filters?
// and gallery should be for current order, and it'll have a reshuffle button...?
// hm, maybe two views, one alphabetical, one for current sort
// need support for urls
// like aura, if screen doesn't get filled try to find another image(s) that will fill the space
// write a script for formatting files correctly
// jaggy scrolling may be related to loading new page at the same time
// -> supposedly a solution is to set offscreen page limit to 1 less, then scroll,
// -> then set it back to normal, but that doesn't seem to have had an effect
// videos stop playing after looping a few times. if there is no permanent solution,
// -> at least figuring out how to handle the error gracefully (and maybe try to restart it?)
// -> would be a good start
//
// settings:
// monitor orientation: landscape xor vertical
// proceed speed
// display images: true xor false
// display gifs: true xor false
// display videos: true xor false
// display long images: true xor false
// display images that are bad for orientation: true xor false (maybe with a cutoff?)
// only pixel art: true xor false