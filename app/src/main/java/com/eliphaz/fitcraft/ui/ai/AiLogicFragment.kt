package com.eliphaz.fitcraft.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.eliphaz.fitcraft.R
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.ai.core.util.content
import com.google.firebase.ai.generative.GenerativeImage



class AiLogicFragment : Fragment() {

    private lateinit var promptInput: EditText
    private lateinit var resultText: TextView
    private lateinit var generateButton: Button
    private lateinit var model: GenerativeModel
    private lateinit var imageButton: Button
    private var imageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ai_logic, container, false)

        promptInput = view.findViewById(R.id.prompt_input)
        resultText = view.findViewById(R.id.result_text)
        generateButton = view.findViewById(R.id.btn_generate)
        imageButton = view.findViewById(R.id.btn_select_image)


        model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                resultText.text = "Imagem selecionada. Pronto para gerar."
            } else {
                resultText.text = "Nenhuma imagem selecionada."
            }
        }

        imageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString().trim()
            resultText.text = "Aguardando resposta..."

            if (imageUri != null) {
                generateFromPromptAndImage(prompt, imageUri!!)
            } else if (prompt.isNotEmpty()) {
                generateFromPrompt(prompt)
            } else {
                resultText.text = "Digite um prompt ou selecione uma imagem."
            }
        }


        return view
    }

    private fun generateFromPromptAndImage(prompt: String, uri: Uri) {
        lifecycleScope.launch {
            try {
                val stream = requireContext().contentResolver.openInputStream(uri)
                val image = GenerativeImage.fromStream(stream!!)
                val response = model.generateContent(prompt, listOf(image))
                resultText.text = response.text ?: "Sem resposta da IA."
            } catch (e: Exception) {
                resultText.text = "Erro ao gerar com imagem: ${e.message}"
            }
        }
    }

    private fun generateFromPrompt(prompt: String) {
        lifecycleScope.launch {
            try {
                val response = model.generateContent(prompt)
                resultText.text = response.text ?: "Nenhuma resposta recebida."
            } catch (e: Exception) {
                resultText.text = "Erro ao gerar resposta: ${e.message}"
            }
        }
    }
}

