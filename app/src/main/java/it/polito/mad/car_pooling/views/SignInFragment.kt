package it.polito.mad.car_pooling.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.car_pooling.R

@Suppress("UNREACHABLE_CODE")
class SignInFragment : Fragment() {

    companion object {
        private const val RC_SIGN_IN = 100
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var input_uri: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        if (user != null){
            writeSharedPreferences()
            findNavController().navigate(R.id.nav_other_list_trip)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("785578307134-aa8d3165sln0kqhm489fdmvt0nfntuph.apps.googleusercontent.com")
            //.requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val sign_in_button = view.findViewById<SignInButton>(R.id.google_sign_in)
        sign_in_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful){
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("signInFragment", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("signInFragment", "Google sign in failed", e)
                }
            } else {
                Log.w("signInFragment", exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signInFragment", "signInWithCredential:success")
                    writeSharedPreferences()
                    val acc = GoogleSignIn.getLastSignedInAccount(requireContext())
                    val db = FirebaseFirestore.getInstance()
                    val users = db.collection("Users")
                    val my_profile = users.document(acc?.getEmail().toString())

                    my_profile.get().addOnSuccessListener {document ->
                        if (document.data != null) {
                            initializeFirebaseMyProfile("yes", users, acc?.getEmail().toString())
                        } else {
                            initializeFirebaseMyProfile("", users, acc?.getEmail().toString())
                        }
                    }
                    findNavController().navigate(R.id.nav_other_list_trip)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("signInFragment", "signInWithCredential:failure", task.exception)
                }
            }
    }

    fun initializeFirebaseMyProfile(input_uri: String, user: CollectionReference, email: String){
        user.document(email).set(
            mapOf("full_name" to "Full Name",
                    "nick_name" to "Nick Name",
                    "email" to email,
                    //"email" to "Email@Address",
                    "location" to "Location",
                    "birthday" to "Birthday",
                    "phone_number" to "PhoneNumber",
                    "image_uri" to input_uri,
                    "hideAll" to "false",
                    "showLocation" to "false",
                    "showBirthday" to "false",
                    "showPhoneNumber" to "false",
                    "hidePlate" to "false")
        )
    }

    private fun writeSharedPreferences() {
        val acc = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (acc != null) {
            //Log.d("signInFragment", "email : ${acc.getEmail()}")
            val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString( getString(R.string.keyCurrentAccount), acc.getEmail().toString())
                commit()
            }
        }
    }

}