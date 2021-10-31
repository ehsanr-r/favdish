package com.example.favdish.viewmodel

import androidx.lifecycle.*
import com.example.favdish.model.database.FavDishRepository
import com.example.favdish.model.entities.FavDish
import kotlinx.coroutines.launch

class FavDishViewModel(private val repository: FavDishRepository) : ViewModel() {

    fun insert(favDish: FavDish) = viewModelScope.launch {
        repository.insertFavDishData(favDish)
    }

    val allDishesList: LiveData<List<FavDish>> = repository.allDishesList.asLiveData()

    fun update(favDish: FavDish) = viewModelScope.launch {
        repository.updateFavDish(favDish)
    }

    val favoriteDishesList: LiveData<List<FavDish>> = repository.favoriteDishesList.asLiveData()

    fun delete(dish: FavDish) = viewModelScope.launch {
        // Call the repository function and pass the details.
        repository.deleteFavDishData(dish)
    }

    fun getFilteredList(value: String): LiveData<List<FavDish>> =
        repository.filteredListDishes(value).asLiveData()


}

class FavDishViewModelFactory(private val repository: FavDishRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavDishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavDishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}