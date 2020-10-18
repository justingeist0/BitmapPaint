package com.fantasma.drawingapp

import kotlin.coroutines.*
import kotlinx.coroutines.*
import kotlin.math.abs

var continuation: Continuation<String>? = null

fun main(args: Array<String>) {
    val job = GlobalScope.launch(Dispatchers.Unconfined) {
        while(true) {
            println(suspendHere())
            println("here 1")
            println("here 2")
            println("here 3")
            println("here 1")
            println("here 1")
            println("here 1")
            println("here 1")
            println("here 1")
            println("here 1")
            println("here 1")
            println("here 1")
        }
    }

    continuation!!.resume("Resumed first time")
    continuation!!.resume("Resumed second time")
    var to = abs(3)
}

suspend fun suspendHere() = suspendCancellableCoroutine<String> {
    continuation = it
}