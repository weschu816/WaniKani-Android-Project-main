package edu.utap.wanikani.ui

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
import androidx.fragment.app.replace
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import edu.utap.wanikani.MainViewModel
import edu.utap.wanikani.R
import edu.utap.wanikani.api.WanikaniApi
import edu.utap.wanikani.api.WanikaniSubjects
import edu.utap.wanikani.glide.Glide
import kotlinx.android.synthetic.main.fragment_lesson.*
import kotlinx.android.synthetic.main.fragment_lesson.charTV
import kotlinx.android.synthetic.main.fragment_lesson.counterTV
import kotlinx.android.synthetic.main.fragment_review_quiz.*

class Lesson : Fragment() {

    private val MAX_LESSON_COUNT = 5

    private val viewModel: MainViewModel by activityViewModels()

    //Use myTypeId to tell if we want to display stuff for 0: radical, 1: kanji, 2: vocabulary
    private var myTypeId : Int =0

    private var currentIdx : Int =0
    //fragment tabs for the radicals section
    private var tabCount : Int = 1
    private var currentTabIdx : Int = 0
    private val radicalTabsTitles : List<String> = listOf("Name Mnemonic", "Kanji Example")          //This is the correct list, and we'll need to use this list once we are able to get the examples
//    private val radicalTabsTitles : List<String> = listOf("Name Mnemonic")

    //Some hardcoded values just to see what my layout looks like.
//    private val debug_characters : List<String> = listOf("一", "ハ")
//    private val debug_meaning : List<String> = listOf("Ground", "Fins")
//    private val debug_nameMnemonic :List<String> = listOf("This radical consists of a single, horizontal stroke. What's the biggest, single, horizontal stroke? That's the ground. Look at the ground, look at this radical, now look at the ground again. Kind of the same, right?", "Picture a fish. Now picture the fish a little worse, like a child's drawing of the fish. Now erase the fish's body and... you're left with two fins! Do you see these two fins? Yeah, you see them.")
//    private val debug_examples :List<String> = listOf("Here is a glimpse of some of the kanji you will be learning that utilize Ground. Can you see where the radical fits in the kanji?", "Here is a glimpse of some of the kanji you will be learning that utilize Fins. Can you see where the radical fits in the kanji?")

    private var subject_list = mutableListOf<WanikaniSubjects>()
    private var related_subject_list = mutableListOf<WanikaniSubjects>()
    private var subject_id_list = mutableListOf<Int>()

    private var lessonDone : MutableList<Boolean> = arrayListOf()          //Use this to check that User has gone through each tab in the lesson
    private var userCanStartQuiz  = 0          //Use this to check that User has gone through each tab in the lesson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun setCounter(){
        counterTV.text = (currentIdx+1).toString() + "/" + subject_id_list.size
    }

    private fun initCharacters(){
        if (subject_list[currentIdx].cha.isNullOrBlank()) {
            //need to make sure it's a png
            var counter=0

            var url = subject_list[currentIdx].character_image[0].toString().split(",")[0].removePrefix("{url=").removeSuffix("\"")
            while(url.contains("svg")){
                counter++
                url=subject_list[currentIdx].character_image[counter].toString().split(",")[0].removePrefix("{url=").removeSuffix("\"")
            }

            Glide.glideFetch(url, url, charImageTV2)
            charTV.text = "" //set textview to null and need to load image view somehow
        } else {
            charImageTV2.setImageDrawable(null)
            charTV.text = subject_list[currentIdx].cha
        }

//        charTV.text = subject_list[currentIdx].cha


    }

    private fun initMeaning(){
        meaningTV.text = subject_list[currentIdx].meanings[0].toString().split(",")[0].removePrefix("{meaning=") //dirty way to clean up the meanings
    }

    private fun initTabs(){

        tabTitleTV.text = radicalTabsTitles[0]
        textBlockTV.text= subject_list[currentIdx].meaning_mnemonic
        radTab1TV.setTextColor(Color.RED)


        //radTab1TV.setOnClickListener{
        //    currentTabIdx=0
        //    openTab(currentTabIdx)
        //}

        //radTab2TV.setOnClickListener{
        //    currentTabIdx=1
        //    openTab(currentTabIdx)
        //}
    }

    private fun initQuiz(){
        quizBut.isClickable=false
        quizBut.isEnabled=false

        quizBut.setOnClickListener{
            viewModel.store_for_review(subject_list)
            val quizFragment = ReviewQuiz.newInstance(isQuiz = 1)
            parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, quizFragment)
                    .addToBackStack("backHome")
                    .commit()
        }
    }

    private fun init_related_vocab(){
        viewModel.launch_subject_data(subject_list[currentIdx].related_subject_ids[0])
    }

    private fun continue_related_vocab(){
        viewModel.launch_subject_data(subject_list[currentIdx+1].related_subject_ids[0])
    }
    
    private fun checkIfLessonDone() : Boolean {
        if ( lessonDone.contains(false))
            return false
        else if (userCanStartQuiz ==0) {
            //XXX probably need to use the API to return some call back to the servers here
            userCanStartQuiz=1              //Used so we only enter in this once
            quizBut.isClickable=true
            quizBut.isEnabled=true
            quizBut.setBackgroundColor(Color.GREEN)
            Toast.makeText(context, "Congratulations, you have finished the lesson and can now start the quiz", Toast.LENGTH_SHORT).show()
            return true
        } else {
            return true
        }
    }

    private fun openTab(tabIdx: Int) {
        tabTitleTV.text = radicalTabsTitles[tabIdx]


        if (tabIdx == 0){
             textBlockTV.text = subject_list[currentIdx].meaning_mnemonic
            radTab1TV.setTextColor(Color.RED)
            radTab2TV.setTextColor(Color.WHITE)
        } else {
//            textBlockTV.text="sample here"
            //need to fetch images for related subject ids
            var temp=""

            temp=related_subject_list[currentIdx].cha.plus("meaning: ")

            textBlockTV.text=temp.plus(related_subject_list[currentIdx].meanings[0].toString().split(",")[0].removePrefix("{meaning="))
            radTab1TV.setTextColor(Color.WHITE)
            radTab2TV.setTextColor(Color.RED)
        }
    }

    private fun initLeftRightArrow(){
        leftArrowTV.setOnClickListener{
            if( currentIdx != 0) {
                decrementIdx()
                currentTabIdx = 0
                openTab(currentTabIdx)

                charTV.text = subject_list[currentIdx].cha
                meaningTV.text = subject_list[currentIdx].meanings[0].toString()
                    .split(",")[0].removePrefix("{meaning=")
                setCounter()
            } else {
                currentTabIdx = 0
                openTab(currentTabIdx)
            }
        }

        rightArrowTV.setOnClickListener{
            if(currentIdx==subject_list.size-1){
                //reached end of quiz
                //don't allow user to continue
                if(currentTabIdx ==0){
                    Log.d("XXXtabidx", "1b tab idx is $currentTabIdx")
                    currentTabIdx++
                    openTab(currentTabIdx)
                }
                checkIfLessonDone()
            }else{

                continue_related_vocab()//fetch next related vocab
                if (currentTabIdx == radicalTabsTitles.size-1) {
                    incrementIdx()

                    Log.d("XXXtabidx", "1 tab idx is $currentTabIdx")
                    currentTabIdx=0
                    openTab(currentTabIdx)
                }
                else {
                    Log.d("XXXtabidx", "0 tab idx is $currentTabIdx")
                    currentTabIdx++
                    openTab(currentTabIdx)
                }
                Log.d("XXXidx", "idx is $currentIdx")

                charTV.text = subject_list[currentIdx].cha
                meaningTV.text = subject_list[currentIdx].meanings[0].toString().split(",")[0].removePrefix("{meaning=")
                setCounter()

                //val lessonDoneIdx = currentIdx*tabCount+currentTabIdx
                lessonDone[currentIdx]=true
                //Log.d("XXXlessonDone", "$lessonDoneIdx is set to true")
                checkIfLessonDone()
            }
        }
    }

    private fun decrementIdx() {
        if( currentIdx == 0)
           currentIdx = subject_list.size-1
        else
            currentIdx--
    }

    private fun incrementIdx() {
        if( currentIdx == subject_list.size-1)
            currentIdx = 0
        else
            currentIdx++
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lesson, container, false)

        viewModel.netSubjectsLesson()

        viewModel.observeAvailableLessonSubjects().observe(viewLifecycleOwner,
                Observer {
                    if (it!= null){
                        Log.d("XXXWestag", "Message observed")
                        lessonDone= mutableListOf()
                        subject_list= mutableListOf()
                        subject_id_list= mutableListOf()
                        userCanStartQuiz=0

                        var type = "Radical"
                        var ctr = 0
                        for (i in it){
                            //Log.d("XXXwessubjects", "subject char is: ${i.cha}")
                            subject_list.add(i)
                            subject_id_list.add(i.subject_id)

                            //XXX When we can get the name examples will need to modify the logic below:
                            //when (type){
                            //    "Radical"->tabCount=2
                            //    "Kanji"->tabCount=4
                            //    "Vocabulary"->tabCount=3
                            //    else->tabCount=0
                            //}
                            lessonDone.add(false)
//                            for(x in i.related_subject_ids){ //too many requests this way
//                                viewModel.launch_subject_data(x)
//                            }
//                            viewModel.launch_subject_data(i.related_subject_ids[0])

                            ctr++
                            if(ctr==MAX_LESSON_COUNT)
                                break
                        }
                        lessonDone[0]=true
                        initTabs()
                        initCharacters()
                        initMeaning()
                        setCounter()
                        init_related_vocab()

//                        var id_related=subject_list[currentIdx].related_subject_ids[0]
//                        viewModel.launch_subject_data(id_related)
                    }
                })

        viewModel.observeWanikaniSubject().observe(viewLifecycleOwner,
            Observer {
                if (it!= null){
                    if(related_subject_list.contains(it)){
//                        var temp=viewModel.observeWanikaniSubject()
//                        var temp2=viewModel.observeWanikaniSubject()

                    }else{
                        related_subject_list.add(it)

                    }

                    }


            })


        requireActivity().onBackPressedDispatcher.addCallback(this){
            parentFragmentManager.popBackStack()
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (this.arguments != null) {
            myTypeId = this.requireArguments().getInt(typeIdKey)
        }
        // This tells us how many tabs there are based on whether this lesson is type: Radicals, Kanji, Vocabulary
        //when (myTypeId){
        //    0->tabCount=2
        //    1->tabCount=4
        //    2->tabCount=3
        //    else->tabCount=0
        //}

        //I used a 2d array that's been flattened to 1d
        //for (i in 0 until debug_characters.size * tabCount) {
        //    lessonDone.add(false)
        //    Log.d("XXXlessonDone", "$i is set to false")
        //}
        //lessonDone[0] = true

        //initCharacters()
        //initMeaning()
        //initTabs()
        initQuiz()
        initLeftRightArrow()
    }

    companion object {
        const val typeIdKey = "typeIdKey"
        fun newInstance(typeId: Int) : Lesson {
            val b = Bundle()
            b.putInt(typeIdKey, typeId)
            val frag = Lesson()
            frag.arguments = b
            return frag
        }
    }
}