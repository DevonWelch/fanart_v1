package com.example.myapplication

import android.R
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter


//
//class ViewPagerFragmentAdapter(fragmentManager: FragmentManager) :
//    FragmentStateAdapter(fragmentManager) {
//    private val arrayList = ArrayList<Fragment>()
//    fun addFragment(fragment: Fragment) {
//        arrayList.add(fragment)
//    }
//
//    override fun getItemCount(): Int {
//        return arrayList.size
//    }
//
//    override fun createFragment(position: Int): Fragment {
//        // return a brand new fragment that corresponds to this 'position'
//        return PageFragment.newInstance(position) // Create a new instance of the page fragment
//    }
//}
//
//class ViewPagerFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
//
//    override fun getItemCount(): Int = 100
//
//    override fun createFragment(position: Int): Fragment {
//        // Return a NEW fragment instance in createFragment(int)
//        val fragment = DemoObjectFragment()
//        fragment.arguments = Bundle().apply {
//            // Our object is just an integer :-P
//            putInt(ARG_OBJECT, position + 1)
//        }
//        return fragment
//    }
//}
//
class ViewPagerFragmentAdapter(
//    private val total: Int,
    private val context: Context,
    private val filepathList: List<String>,
    private val makeImage: (filepath: String, context: Context) -> Void
) :
    PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return makeImage(filepathList[position], context)
//        val inflater = activity.layoutInflater
//        val viewItem: View = inflater.inflate(R.layout.image_item, container, false)
//        val imageView = viewItem.findViewById<View>(R.id.imageView) as ImageView
//        imageView.setImageResource(imagesArray[position])
//        val textView1 = viewItem.findViewById<View>(R.id.textview) as TextView
//        textView1.text = namesArray[position]
//        (container as ViewPager).addView(viewItem)
//        return viewItem
    }

    override fun getCount(): Int {
        // TODO Auto-generated method stub
        return filepathList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        // TODO Auto-generated method stub
        return view === `object` as View
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        // TODO Auto-generated method stub
        (container as ViewPager).removeView(`object` as View)
    }
}