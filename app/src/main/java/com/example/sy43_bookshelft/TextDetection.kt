package com.example.sy43_bookshelft


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer

class TextDetection {

    val resourceId  = R.drawable.imagetest

    // Decode the image file into a Bitmap
    val bitmap: Bitmap = BitmapFactory.decodeResource(this.resources, resourceId)

    // Créer une instance de TextRecognizer
    val textRecognizer = TextRecognizer.create()

    // Convertir la bitmap en InputImage
    val image = InputImage.fromBitmap(bitmap)

// Traiter l'image avec le TextRecognizer
    textRecognizer.process(image)
    .addOnSuccessListener { text ->
        // Traiter les résultats de la reconnaissance de texte
        for (block in text.blocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val textContent = element.text
                    // Utiliser le texte extrait
                }
            }
        }
    }
    .addOnFailureListener { e ->
        // Gérer les erreurs de reconnaissance de texte
        e.printStackTrace()
    }

// Libérer les ressources de TextRecognizer
    textRecognizer.close()


}