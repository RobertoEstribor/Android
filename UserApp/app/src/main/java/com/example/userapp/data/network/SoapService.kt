package com.example.userapp.data.network

import android.util.Log
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.transport.HttpTransportSE

object SoapService {

    private const val NAMESPACE = "https://www.todoestribor.com"
    private const val METHOD_NAME = "GetUsuarios"
    // Per instructions, prioritize GetUsuarios, but be mindful of ObtenerUsuarios if issues arise.
    private const val SOAP_ACTION = "https://www.todoestribor.com/GetUsuarios" 
    private const val URL = "https://www.todoestribor.com/ilionservices4/wsmantenimiento/wsmantenimiento.asmx"

    suspend fun getUsers(companyCode: String): String? {
        val request = SoapObject(NAMESPACE, METHOD_NAME)
        // Ensure the parameter name "nempresa" is exactly what the web service expects.
        request.addProperty("nempresa", companyCode)

        val envelope = SoapEnvelope(SoapEnvelope.VER11) // SOAP 1.1
        envelope.dotNet = true // Required for ASP.NET web services
        envelope.setOutputSoapObject(request)

        val transport = HttpTransportSE(URL)
        // Adding a timeout (e.g., 15 seconds) can be beneficial
        // transport.timeout = 15000 

        return try {
            Log.d("SoapService", "Attempting to call SOAP service. URL: $URL, Action: $SOAP_ACTION")
            Log.d("SoapService", "Request: $request")
            // Make the SOAP call
            transport.call(SOAP_ACTION, envelope)

            // Process the response
            val response = envelope.response
            Log.d("SoapService", "Raw response: $response")

            if (response is SoapPrimitive) {
                val result = response.toString()
                Log.d("SoapService", "SOAP call successful. Response: $result")
                result
            } else if (response is SoapObject) {
                // Handle cases where the response might be a complex object
                // This might indicate an error or an unexpected response structure
                val resultText = response.toString()
                Log.w("SoapService", "Unexpected SoapObject response: $resultText")
                // Depending on the service, you might want to parse this SoapObject further
                // For now, returning its string representation or null if it's an error structure
                resultText // Or null, or parse further
            }
            else {
                Log.e("SoapService", "Unexpected response type: ${response?.javaClass?.name}. Response: $response")
                null
            }
        } catch (e: Exception) {
            Log.e("SoapService", "Error during SOAP call: ${e.message}", e)
            // More specific error handling can be added here (e.g., for SoapFault)
            e.printStackTrace()
            null
        }
    }
}
