package edu.utap.wanikani

import android.util.Log
import androidx.lifecycle.*
import edu.utap.wanikani.api.*
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val wanikaniApi = WanikaniApi.create()
    private val repo = Repository(wanikaniApi)

    private var wanikanisubject = MutableLiveData<WanikaniSubjects>()
    private val subject_ids=MutableLiveData<List<WanikaniAssignments>>()
    private val subject_Data=MutableLiveData<List<WanikaniAssignments>>()
    private val subject_ids_for_review=MutableLiveData<List<WanikaniAssignments>>()
    private val assignments_ids=MutableLiveData<HashMap<Int,Int>>()
    private var subject_meanings_list = mutableListOf<WanikaniSubjects>()

    private var userName = MutableLiveData<WanikaniUser>()

    private var available_subject_ids_lessons = MutableLiveData<List<Int>>()
    private var available_subject_ids_review = MutableLiveData<List<Int>>()
    private var available_lesson_subjects = MutableLiveData<List<WanikaniSubjects>>()
    private var available_review_subjects = MutableLiveData<List<WanikaniSubjects>>()
    private val pending_assignments=MutableLiveData<List<WanikaniAssignments>>()

    init {
        netUser()
        netRefresh()
        netIdsLessons()
        netIdsReview()
    }

    fun netUser (){
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO) {
            userName.postValue(repo.fetchUser())
        }
    }

    fun observeUsername(): MutableLiveData<WanikaniUser>{
        return userName
    }

    //Updates the list of subject IDs that are currently available for lesson
    fun netIdsLessons (){
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO) {
            available_subject_ids_lessons.postValue(repo.get_sub_ids_from_available_assignments(repo.get_available_assignments_for_lesson()))
            assignments_ids.postValue(repo.fetchAssignments_ids())
        }
    }

    //Updates the list of subject IDs that are currently available for review
    fun netIdsReview (){
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO) {
            available_subject_ids_review.postValue(repo.get_sub_ids_from_available_assignments(repo.get_available_assignments_for_review()))
        }
    }

    //Updates list of lesson subjects that are currently available
    fun netSubjectsLesson() {
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val mySubjIds = available_subject_ids_lessons.value?.joinToString(",")
            available_lesson_subjects.postValue(mySubjIds?.let { repo.get_subjects_from_available_ids(it) })
        }
    }

    //Updates list of review subjects that are currently available
    fun netSubjectsReview() {
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val mySubjIds = available_subject_ids_review.value?.joinToString(",")
            Log.d("XXXMainviewmodel_net", "Fetching review subjects using ${mySubjIds}")
            available_review_subjects.postValue(mySubjIds?.let { repo.get_subjects_from_available_ids(it) })
            assignments_ids.postValue(repo.fetchAssignments_ids_review())
        }
    }

    fun netPendingAssignments(){
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO)
        {
            pending_assignments.postValue(repo.fetchFutureAssignments())
        }
    }

    fun observePendingAssignments() : MutableLiveData<List<WanikaniAssignments>>{
        return pending_assignments
    }

    fun observeAvailableLessonSubjects() : MutableLiveData<List<WanikaniSubjects>>{
        return available_lesson_subjects
    }

    fun observeAvailableReviewSubjects() : MutableLiveData<List<WanikaniSubjects>>{
        return available_review_subjects
    }

    fun observeAvailableLessonSubjectsId() : MutableLiveData<List<Int>>{
        return available_subject_ids_lessons
    }

    fun observeAvailableReviewSubjectsId() : MutableLiveData<List<Int>>{
        Log.d("XXXMainviewmodel_observe", "my review subject ids are: ${available_subject_ids_review}")
        return available_subject_ids_review
    }

    fun netRefresh() {
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO) {
//            wanikanisubject.postValue(repo.fetchVocab(1))//need to figure out how to link the subject ID to this postvalue when we want to look something up...1=ground, 11=nine, for example.
            subject_ids.postValue(repo.fetchAssignments())
            subject_ids_for_review.postValue(repo.fetchAssignments_for_review())
            assignments_ids.postValue(repo.fetchAssignments_ids())
//            assignments.postValue(repo.fetchAssignments())
        }
    }


    fun launch_subject_data(subject_id:Int){
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO)
        {
            var temp=subject_id
            var temp2=repo.fetchVocab(subject_id).value
            Log.d("XXXsubjectdata", "launch subject data is $temp2")
            wanikanisubject.postValue(temp2)
        }
    }

    fun create_list_subject_data(subject_ids:MutableLiveData<List<WanikaniAssignments>>){
//        for x in subject_ids{
//
//        }
    }
    fun create_review(Request: WanikaniApi.NestedJSON) {
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO)
        {
            repo.create_Review(Request)//this should be assignment id
        }
    }

    // XXX Another function is necessary
    fun observeWanikaniSubject() : MutableLiveData<WanikaniSubjects> {
        var temp=wanikanisubject
        return wanikanisubject
    }

    fun move_to_reviews(id:Int)
    {
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO)
        {
            repo.start_assign(id)//this should be assignment id
        }
    }

    fun get_assignments(){
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO)
        {
            subject_ids.postValue(repo.fetchAssignments())
        }
    }
    fun get_subject_data(data:String){
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO)
        {
            subject_Data.postValue(repo.fetch_subjects_data(data))
        }
    }

    fun observeSubject_data():LiveData<List<WanikaniAssignments>>{
        var temp=subject_Data
        return subject_Data
    }


    fun observeSubjects():LiveData<List<WanikaniAssignments>>{
        var temp=subject_ids
        return subject_ids
    }

    fun observeSubjects_for_review():LiveData<List<WanikaniAssignments>>{
        var temp=subject_ids_for_review
        return subject_ids_for_review
    }

    fun get_assignments_ids(){
        viewModelScope.launch( context = viewModelScope.coroutineContext + Dispatchers.IO)
        {
            assignments_ids.postValue(repo.fetchAssignments_ids())
        }
    }

    fun observeAssignment_ids():MutableLiveData<HashMap<Int,Int>>{
        var temp=assignments_ids
        return assignments_ids
    }

    fun store_for_review(data: MutableList<WanikaniSubjects>){
        subject_meanings_list=data
    }

    fun get_quiz_data():MutableList<WanikaniSubjects>{
        return subject_meanings_list
    }
}