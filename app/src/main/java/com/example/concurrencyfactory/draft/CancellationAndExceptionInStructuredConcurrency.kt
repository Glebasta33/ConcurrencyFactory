package com.example.concurrencyfactory.draft

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CancellationAndExceptionInStructuredConcurrency {

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("MyTest", "CoroutineExceptionHandler: ${throwable.message}")
    }

    /**
     * Добавление CoroutineExceptionHandler в контекст родительского скоупа перехватывает исключения,
     * выброшенные дочерними корутинами.
     */
    val rootScope = CoroutineScope(Dispatchers.Default + exceptionHandler)

    init {
        val rootJob = rootScope.launch(/*exceptionHandler*/) {
            // exceptionHandler в самом родительском биледере будет перехватывать исключения

            val job1 = launch {
                delay(3000)
                val job1Child1 = launch {
                    delay(3000)
                }

                val job1Child2 = launch {
                    while (true) {
                        delay(1000)
                        Log.d("MyTest", "Hi from job1Child2. isActive: $isActive")
                        /**
                         *  CancellationException не отменяет работу родительских корутин. Его не перехватывет ExceptionHandler.
                         *  Видимо CancellationException не передаётся вверх по иерархии Job.
                         */
//                        if (!isActive) throw CancellationException("My CancellationException")
                        /**
                         * Вызов cancel у родительской корутины не отменит дочернюю, если она не бросает  CancellationException ?!
                         */
                    }
                }

//                while (true) if (!isActive) throw CancellationException("My CancellationException")

                /**
                 * Вызов Cancel лишь переводит статус корутины isActive в false, но не останавливает её выполнение.
                 * Чтобы остановить корутину необходимо бросить CancellationException.
                 */
//                job1Child2.cancel()
            }

            launch {
                while (true) {
                    delay(1000)
                    Log.d("MyTest", "job1 isActive: $isActive")
                }
            }

            delay(7000)
            //ПОЧЕМУ CANCEL НЕ РАБОТАЕТ ПРИ ОТМЕНЕ РОДИТЕЛЬСКОЙ КОРУТИНЫ???
            job1.cancel()

            // exceptionHandler не перехватывает исключения в билдерах дочерних корутин
            val job2 = launch(/*exceptionHandler*/) {
                val job2Child1 = launch(/*exceptionHandler*/) {
                    delay(3000)
                    /**
                     * Выброшенное в любой корутине исключение отменяет работу всей иерархии корутин
                     * и приводит к крашу приложения, если исключение не обработать.
                     */
//                    throw RuntimeException("My exception from coroutine")
                }

                val job2Child2 = launch {
                    delay(3000)
                }
                while (true) {
                    delay(1000)
                    Log.d(
                        "MyTest",
                        "job2Child1.isActive: ${job2Child1.isActive}. job2Child2.isActive: ${job2Child2.isActive}"
                    )
                }
            }

            while (true) {
                delay(1000)
                Log.d("MyTest", "job1.isActive: ${job1.isActive}. job2.isActive: ${job2.isActive}")
            }

        }

    }

}