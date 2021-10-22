package edu.utap.wanikani.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import edu.utap.wanikani.MainViewModel
import edu.utap.wanikani.R
import edu.utap.wanikani.api.WanikaniApi
import kotlinx.android.synthetic.main.main_fragment.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment(R.layout.main_fragment) {
    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

    private val viewModel: MainViewModel by activityViewModels()
    private var lessonSize = 0
    private var reviewSize = 0

    private fun setUserName(myName: String){
        nameTV.text = myName
    }

    private fun setReviewsTV(n_reviews: Int){
        myReviewTV.text = n_reviews.toString() + " reviews available"
    }

    private fun setLessonsTV(n_lessons: Int){
        myLessonsTV.text = n_lessons.toString() + " lessons available"
    }

    private fun initReviewObserver() {
        viewModel.observeAvailableReviewSubjectsId().observe(viewLifecycleOwner,
            Observer { myReviewList ->
                swipeRefreshLayout.isRefreshing = false
                if (myReviewList != null) {
                    reviewSize = myReviewList.size
                    setReviewsTV(reviewSize)
                } else {
                    setReviewsTV(0)
                }
            })
    }

    private fun initLessonObserver() {
        viewModel.observeAvailableLessonSubjectsId().observe(viewLifecycleOwner,
            Observer { myLessonList ->
                swipeRefreshLayout.isRefreshing = false
                if (myLessonList != null) {
                    lessonSize = myLessonList.size
                    setLessonsTV(lessonSize)
                    Log.d("XXXlesson size:", "$myLessonList")
                } else {
                    setLessonsTV(0)
                }
            })
    }

    private fun initNameObserver() {
        viewModel.observeUsername().observe(viewLifecycleOwner,
            Observer { user ->
                setUserName(user.username)
            })
    }

    private fun initSwipeLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            Log.d("XXX5", " refreshing")
            swipeRefreshLayout.isRefreshing = true
            viewModel.netIdsLessons()
            viewModel.netIdsReview()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        viewModel.observeWanikaniSubject().observe(viewLifecycleOwner,
            Observer {
                swipeRefreshLayout.isRefreshing = false
                if (it != null) {
                    val subject = it.cha
                    Log.d("XXXFrag", "My subject character is ${subject}")
                } else {
                    Log.d("XXXFrag", "subject is null?")

                }
            }
        )

        //val testButton = (activity as AppCompatActivity).findViewById<TextView>(R.id.testBut)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initNameObserver()
        initReviewObserver()
        initLessonObserver()
        initSwipeLayout()

        startBut.setOnClickListener{
            if(lessonSize > 0) {
                val lessonFragment = Lesson.newInstance(0)
                parentFragmentManager.beginTransaction()
                    .add(R.id.main_frame, lessonFragment)
                    .addToBackStack("backHome")
                    .commit()
            } else {
                Toast.makeText(context, "No lessons currently available", Toast.LENGTH_SHORT).show()
            }
        }

        reviewBut.setOnClickListener{
            if(reviewSize>0) {
                //Use isQuiz=0 to indicate this is not a quiz
                val reviewFragment = ReviewQuiz.newInstance(0)
                parentFragmentManager.beginTransaction()
                    .add(R.id.main_frame, reviewFragment)
                    .addToBackStack("backHome")
                    .commit()
            } else {
                Toast.makeText(context, "No reviews currently available", Toast.LENGTH_SHORT).show()

            }
        }

        accountBut.setOnClickListener{
//            val reviewFragment = Review.newInstance(0)
            //Use isQuiz=0 to indicate this is not a quiz
            val accountFragment=AccountSettings.newInstance()
            parentFragmentManager.beginTransaction()
                .add(R.id.main_frame, accountFragment)
                .addToBackStack("backHome")
                .commit()
        }

    }
}