package edu.utap.wanikani.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Repository(private val wanikaniApi: WanikaniApi) {

    suspend fun create_Review(Request: WanikaniApi.NestedJSON) {
        wanikaniApi.create_review(Request)
    }

    suspend fun fetchVocab(id: Int) : MutableLiveData<WanikaniSubjects> {
//        var temp=MutableLiveData<WanikaniSubjects>()
//        temp= MutableLiveData(wanikaniApi.single_character(id).data)
        var api_response=wanikaniApi.single_character(id)
        var answer=api_response.data
        answer.subject_id= (api_response.id)
        return MutableLiveData(answer)

//        return wanikaniApi.single_character(id).data
    }

    private fun unpackPosts(response: WanikaniApi.ListingData):  List<WanikaniAssignments> {
        // XXX Write me.
        var postlist= mutableListOf<WanikaniAssignments>()
        for(i in response.data){
            var temp2=i.data
            temp2.sub_id=i.id
            postlist.add(temp2)

        }
        return postlist
    }

    suspend fun fetchAssignments() : List<WanikaniAssignments> {
        return unpackPosts(wanikaniApi.get_assignments_for_lesson())
    }

    suspend fun fetchFutureAssignments() : List<WanikaniAssignments> {
        val currentTime = LocalDateTime.now()

        Log.d("XXXcurrenttime", "Current time is: $currentTime")
        //val formatter = DateTimeFormatter.ISO_INSTANT
        //val formattedCurrentTime = currentTime.format(formatter)
        //Log.d("XXXcurrenttime", "Current time is: $currentTime formatted time is $formattedCurrentTime")
        return unpackPosts(wanikaniApi.get_assignments_available_after(currentTime.toString()))
    }

    suspend fun fetchAssignments_for_review() : List<WanikaniAssignments> {
        return unpackPosts(wanikaniApi.get_assignments_for_review())
    }

    suspend fun fetch_subjects_data(subjects:String) : List<WanikaniAssignments> {
        return unpackPosts(wanikaniApi.get_subjects(subjects))
    }

//    private fun unpackPosts_assignments(response: WanikaniApi.ListingData):  List<Int> {
    private fun unpackPosts_assignments(response: WanikaniApi.ListingData):  HashMap<Int,Int> {
        // XXX Write me.
        var postlist= HashMap<Int,Int>()
        for(i in response.data){
//            postlist.add(i.assignment_id)
            postlist[i.id]=i.data.sub_id


        }
        return postlist
    }

    suspend fun get_available_assignments_for_lesson() : WanikaniApi.ListingData {
        return wanikaniApi.get_assignments_for_lesson()
    }

    suspend fun get_available_assignments_for_review() : WanikaniApi.ListingData {
        return wanikaniApi.get_assignments_for_review()
    }

    fun get_sub_ids_from_available_assignments(response: WanikaniApi.ListingData):  List<Int> {
        var listSubjectIds= mutableListOf<Int>()
        for(i in response.data){
            listSubjectIds.add(i.data.sub_id)
        }
        Log.d("XXXWes", "here is the list of $listSubjectIds . Here it is in comma separated form: ${listSubjectIds.joinToString(separator = ",")}")
        return listSubjectIds
    }

    //This function returns the available subjects from a string of comma separated ids
    suspend fun get_subjects_from_available_ids(available_subj_ids: String) : List<WanikaniSubjects> {
        return unpackSubjects(wanikaniApi.get_subjects_from_ids(available_subj_ids))
    }

    suspend fun fetchAssignments_ids() : HashMap<Int,Int> {
        return unpackPosts_assignments(wanikaniApi.get_assignments_for_lesson())
    }

    suspend fun fetchAssignments_ids_review() : HashMap<Int,Int> {
        return unpackPosts_assignments(wanikaniApi.get_assignments_for_review())
    }

    suspend fun fetchUser() : WanikaniUser {
        return wanikaniApi.getUser().data
    }

    private fun unpackSubjects(response: WanikaniApi.WanikaniSubjectsResponse) : List<WanikaniSubjects>{
        var myListSubjects = mutableListOf<WanikaniSubjects>()

        for (i in response.data){
            i.data.subject_id=i.id
            myListSubjects.add(i.data)
        }
        return myListSubjects
    }

    suspend fun start_assign(id: Int){
        wanikaniApi.start_assignment(id)
    }
}