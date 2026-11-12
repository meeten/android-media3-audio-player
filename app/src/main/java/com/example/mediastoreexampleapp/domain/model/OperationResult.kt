package com.example.mediastoreexampleapp.domain.model

sealed interface OperationResult {

    sealed interface Download : OperationResult {

        object Success : Download

        object Failure : Download
    }

    sealed interface Delete : OperationResult {

        object Success : Delete

        object Failure : Delete

        object Other : Delete
    }
}