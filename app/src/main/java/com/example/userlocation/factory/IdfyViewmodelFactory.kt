package com.example.userlocation.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.userlocation.repository.IdfyRepository
import com.example.userlocation.viewmodel.IdfyViewmodel


class IdfyViewmodelFactory (private val idfyRepository: IdfyRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return  IdfyViewmodel(idfyRepository) as T
    }

}