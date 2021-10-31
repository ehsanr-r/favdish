package com.example.favdish.model.network

import com.example.favdish.model.entities.RandomDish
import com.example.favdish.utils.Constants
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

// TODO Step 1: Create a API service class name as RandomDishApiService.
// START
class RandomDishApiService {

    // TODO Step 3: Create a variable of Retrofit Builder as below.
    // START
    /**
     * Retrofit adapts a Java interface to HTTP calls by using annotations on the declared methods to
     * define how requests are made. Create instances using {@linkplain Builder the builder} and pass
     * your interface to {create} to generate an implementation.
     */
    private val api = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL) // Set the API base URL.
        // Add converter factory for serialization and deserialization of objects.
        /**
         * A Converter.Factory converter which uses Gson for JSON.
         *
         * Because Gson is so flexible in the types it supports, this converter assumes that it can handle
         * all types.
         */
        .addConverterFactory(GsonConverterFactory.create())
        /**
         * **
         * Add a call adapter factory for supporting service method return types other than.
         *
         * A CallAdapter.Factory call adapter which uses RxJava 3 for creating observables.
         *
         * Adding this class to Retrofit allows you to return an Observable, Flowable, Single, Completable
         * or Maybe from service methods.
         */
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build() // Create the Retrofit instance using the configured values.
        // Create an implementation of the API endpoints defined by the service interface in our case it is RandomDishAPI.
        .create(RandomDishAPI::class.java)
    // END

    // TODO Step 4: Create a function that will initial the API call and returns the API response.
    // START
    fun getRandomDish(): Single<RandomDish.Recipes> {
        // TODO Step 6: Pass the values to the method as required params
        // START
        return api.getRandomDish(
            Constants.API_KEY_VALUE,
            Constants.LIMIT_LICENSE_VALUE,
            Constants.TAGS_VALUE,
            Constants.NUMBER_VALUE
        )
        // END
    }
    // END
}
// END