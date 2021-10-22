package edu.utap.wanikani.ui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import edu.utap.wanikani.MainViewModel
import edu.utap.wanikani.R
import edu.utap.wanikani.api.WanikaniApi
import edu.utap.wanikani.api.WanikaniSubjects
import edu.utap.wanikani.glide.Glide
import kotlinx.android.synthetic.main.fragment_review_quiz.*
import kotlinx.android.synthetic.main.fragment_review_quiz.charTV
import kotlinx.android.synthetic.main.fragment_review_quiz.counterTV

class ReviewQuiz : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    //Is review tells whether to use the passed down subjects from lesson or fetch from the net
    private var isQuiz: Int = 0

    private var currentIdx: Int = 0
    private var tries: Int = 0
    //fragment tabs for the radicals section

    //Some hardcoded values just to see what my layout looks like.
//    private val debug_characters : List<String> = listOf("一", "ハ")
//    private val debug_answers : List<String> = listOf("Ground", "Fins")
    private var quiz_data = mutableListOf<WanikaniSubjects>()
    private var characters = mutableListOf<String>()
    private var answers = mutableListOf<String>()
    private var assignments_ids = HashMap<Int, Int>()
    private var correct_answers = mutableListOf<WanikaniSubjects>()

    private var questionDone: MutableList<Boolean> = arrayListOf()

    private fun setCounter() {
        counterTV.text = (currentIdx + 1).toString() + "/" + answers.size
    }

    private fun initCharacters() {
        if (characters[currentIdx].contains("https:")) {

            var url = characters[currentIdx]
            Glide.glideFetch(url, url, charImageTV)
            charTV.text = "" //set textview to null and need to load image view somehow
        } else {
            charImageTV.setImageDrawable(null)
            charTV.text = characters[currentIdx]
        }
//        charTV.text = characters[0]
    }

    private fun initTitle() {
        nameTypeTV.text = "Radical Name"
    }

    private fun initHint() {
        hintTV.setOnClickListener {
            Toast.makeText(context, "The answer is: ${answers[currentIdx]}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun initAnswerCheck() {

        answerArrow.setOnClickListener {
            if(responseET.text.toString().toLowerCase().trim().isEmpty()){
                Toast.makeText(context, "need to input an answer", Toast.LENGTH_SHORT)
                    .show()
            }else{
                checkAnswer()
            }

        }
    }

    private fun checkAnswer() {
//        if (responseET.text.toString().toLowerCase() == debug_answers[currentIdx].toLowerCase()) {
        if (responseET.text.toString().toLowerCase().trim() == answers[currentIdx].toLowerCase()) {
            if (isQuiz == 1) {
                correct_answers.add(quiz_data[currentIdx])
            }
            else{
                quiz_data = viewModel.observeAvailableReviewSubjects().value?.toMutableList()!!
                correct_answers.add(quiz_data[currentIdx])
            }

            answerLay.setBackgroundColor(Color.GREEN)
            responseET.setBackgroundColor(Color.GREEN)
            questionDone[currentIdx] = true
            //Thread.sleep(2000)
            Log.d("XXXcheckanswer", "correct answer")

            if (lessonFinished()) {
                finishLesson()
            } else
                nextQuestion()

        } else {
            //User gets two tries otherwise we move on
            if (tries == 1) {
                questionDone[currentIdx] = true
                nextQuestion()
            } else {
                answerLay.setBackgroundColor(Color.RED)
                responseET.setBackgroundColor(Color.RED)
                //Thread.sleep(2000)
                tries++
            }
        }
    }

    private fun finishLesson() {
        //submit put requests to mark items as succesfully completed and move into review queue
        if (isQuiz == 1) {
            for (item in quiz_data) {
                var temp2 = assignments_ids
                var assignment_id_key =
                    assignments_ids.filterValues { it == item.subject_id }.keys.iterator()
                        .next()
                //check if key actually is right
                var temp = assignment_id_key
                //only move if answer was right.
                if (correct_answers.contains(item)) {
                    viewModel.move_to_reviews(assignment_id_key)
                }

            }
            //Call a refresh on the ids when the quiz/review is completed.
            viewModel.netRefresh()
            viewModel.netIdsLessons()
            viewModel.netIdsReview()
            //Pop back twice if coming from the lesson frag; pop back once if coming from the home frag
            parentFragmentManager.popBackStack()
            parentFragmentManager.popBackStack()
        } else {
            quiz_data = viewModel.observeAvailableReviewSubjects().value?.toMutableList()!!
            for (item in quiz_data) {
                assignments_ids = viewModel.observeAssignment_ids().value!!
                var temp2 = assignments_ids
                var assignment_id_key: Int? =
                    assignments_ids.filterValues { it == item.subject_id }.keys.iterator()
                        .next()
                if (assignment_id_key == null) {
                    //somethig went wrong
                } else {
                    if (correct_answers.contains(item)) { //only send review create request if answer is correct
                        var temp = assignment_id_key
                        viewModel.create_review(
                            WanikaniApi.NestedJSON(
                                WanikaniApi.NestedJSON_single(
                                    assignment_id = assignment_id_key.toString(),
                                    incorrect_meaning_answers = "0",
                                    incorrect_reading_answers = "0"
                                )
                            )
                        )
                    }
                }
                //check if key actually is right

            }
            //Call a refresh on the ids when the quiz/review is completed.
            viewModel.netRefresh()
            viewModel.netIdsLessons()
            viewModel.netIdsReview()
            parentFragmentManager.popBackStack()
        }

    }

    private fun nextQuestion() {
        tries = 0

        if(currentIdx==answers.size-1){
            responseET.setBackgroundColor(Color.WHITE)
            answerLay.setBackgroundColor(Color.WHITE)
            finishLesson()
        }else {
            var nextIdx = questionDone.subList(currentIdx + 1, questionDone.size).indexOf(false)
            Log.d("XXXnextQuestion0", "$nextIdx is the next idx")
            if (nextIdx == -1) {
                nextIdx = questionDone.indexOf(false)
            } else {
                //Accounts for the offset introduced by taking the sublist
                nextIdx += currentIdx + 1
            }

            Log.d("XXXnextQuestion1", "$nextIdx is the next idx")

            currentIdx = nextIdx
            setCounter()
            responseET.setBackgroundColor(Color.WHITE)
            answerLay.setBackgroundColor(Color.WHITE)

            if (characters[currentIdx].contains("https:")) {

                var url = characters[currentIdx]
                Glide.glideFetch(url, url, charImageTV)
                charTV.text = "" //set textview to null and need to load image view somehow
            } else {
                charImageTV.setImageDrawable(null)
                charTV.text = characters[currentIdx]
            }

            responseET.text.clear()
        }
}

    private fun lessonFinished() :Boolean {
        return !questionDone.contains(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_review_quiz, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (this.arguments != null) {
            //Use isReview to tell whether this is coming from a lesson frag and just needs to get the subjects from the lesson
            // or if this is from home frag and we need to fetch data across the network
            isQuiz = this.requireArguments().getInt(isQuizKey)

            //Use previous lesson's subjects
            if (isQuiz==1) {
                quiz_data = viewModel.get_quiz_data()
                for (i in quiz_data) {
                    answers.add(i.meanings[0].toString().split(",")[0].removePrefix("{meaning="))

                    if(i.cha!=null){
                        characters.add(i.cha)
                    }
                    else{
                        //need to fetch url image here...
                        var counter=0
                        var char_temp=i.character_image[0].toString().split(",")[0].removePrefix("{url=").removeSuffix("\"")
                        while(char_temp.contains("svg")){
                            counter++
                            char_temp=i.character_image[counter].toString().split(",")[0].removePrefix("{url=").removeSuffix("\"")
                        }
                        characters.add(char_temp)
                    }
                    var temp=characters
                }
                assignments_ids= viewModel.observeAssignment_ids().value!!

                for (i in answers.indices){
                    questionDone.add(false)
                    Log.d("XXXquizDone", "$i is set to false")
                }
                initCharacters()
                initHint()
                setCounter()
                //Pop back twice if coming from the lesson frag
                requireActivity().onBackPressedDispatcher.addCallback(this){
                    parentFragmentManager.popBackStack()
                    parentFragmentManager.popBackStack()
                }
            }
            //fetch the network for a review
            else {
                Log.d("XXXReview", "Start of review frag")
                viewModel.netSubjectsReview()

                viewModel.observeAvailableReviewSubjects().observe(viewLifecycleOwner,
                        Observer {
                            if (it!= null){
                                characters = mutableListOf()
                                answers = mutableListOf()
                                questionDone = mutableListOf()

                                Log.d("XXXReview", "list is size ${it.size}")
                                for (i in it){
                                   Log.d("XXXReview", "Adding subject id: ${i.subject_id}")
//                                    characters.add(i.cha)
                                    if(i.cha!=null){
                                        characters.add(i.cha)
                                    }
                                    else{
                                        //need to fetch url image here...
                                        var counter=0
                                        var char_temp=i.character_image[0].toString().split(",")[0].removePrefix("{url=").removeSuffix("\"")
                                        while(char_temp.contains("svg")){
                                            counter++
                                            char_temp=i.character_image[counter].toString().split(",")[0].removePrefix("{url=").removeSuffix("\"")
                                        }
                                        characters.add(char_temp)
                                    }
                                    var temp=characters
                                    answers.add(i.meanings[0].toString().split(",")[0].removePrefix("{meaning="))

                                    questionDone.add(false)
                                }
                                initCharacters()
                                initHint()
                                setCounter()
                            }
                        })


                //Pop back once if coming from the home frag
                requireActivity().onBackPressedDispatcher.addCallback(this){
                    parentFragmentManager.popBackStack()
                }

            }
        }

        initTitle()
        initAnswerCheck()
    }

    companion object {
        const val isQuizKey = "isQuizKey"
        fun newInstance(isQuiz: Int) : ReviewQuiz {
            val b = Bundle()
            b.putInt(isQuizKey, isQuiz)
            val frag = ReviewQuiz()
            frag.arguments = b
            return frag
        }
    }
}