package org.jetbrains.krpc

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import org.jetbrains.krpc.client.clientOf
import org.jetbrains.krpc.server.rpcServiceMethodSerializationTypeOf
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KType
import kotlin.reflect.typeOf

val logger = KotlinLogging.logger("KSPGeneratorTest")

interface EmptyService {
    val flow: Flow<Int>

    val sharedFlow: SharedFlow<Int>

    val stateFlow: StateFlow<Int>

    suspend fun empty()
}

val stubEngine = object : RPCEngine {
    override val coroutineContext: CoroutineContext = Job()

    override suspend fun call(callInfo: RPCCallInfo, deferred: CompletableDeferred<*>): Any? {
        logger.info { "Called ${callInfo.callableName}" }
        return null
    }

    override fun <T> registerPlainFlowField(fieldName: String, type: KType): Flow<T> {
        logger.info { "registered flow: $fieldName" }
        return flow {  }
    }

    override fun <T> registerSharedFlowField(fieldName: String, type: KType): SharedFlow<T> {
        logger.info { "registered flow: $fieldName" }
        return MutableSharedFlow(1)
    }

    override fun <T : Any?> registerStateFlowField(fieldName: String, type: KType): StateFlow<T> {
        logger.info { "registered flow: $fieldName" }

        @Suppress("UNCHECKED_CAST")
        return MutableStateFlow<Any?>(null) as StateFlow<T>
    }
}

interface CommonService : RPC, EmptyService {
    override val flow: Flow<Int>

    override val sharedFlow: SharedFlow<Int>

    override val stateFlow: StateFlow<Int>

    override suspend fun empty()
}

suspend inline fun <reified T> testService() where T : RPC, T : EmptyService {
    val test: suspend T.() -> Unit = {
        empty()
        flow
        sharedFlow
        stateFlow
    }

    RPC.clientOf<T>(stubEngine).test()
    RPC.clientOf<T>(typeOf<T>(), stubEngine).test()

    logger.info { rpcServiceMethodSerializationTypeOf<T>("empty") }
    logger.info { rpcServiceMethodSerializationTypeOf(typeOf<T>(), "empty") }
}
