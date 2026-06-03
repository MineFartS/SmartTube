package minefarts.smarttube.utils.app.nsigsolver.impl

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8ScriptExecutionException
import minefarts.smarttube.utils.app.nsigsolver.common.loadScript
import minefarts.smarttube.utils.app.nsigsolver.common.withLock
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderError
import minefarts.smarttube.utils.app.nsigsolver.runtime.JsRuntimeChalBaseJCP
import minefarts.smarttube.utils.app.nsigsolver.runtime.Script
import minefarts.smarttube.utils.app.nsigsolver.runtime.ScriptSource
import minefarts.smarttube.utils.app.nsigsolver.runtime.ScriptType
import minefarts.smarttube.utils.app.nsigsolver.runtime.ScriptVariant

internal object V8ChallengeProvider: JsRuntimeChalBaseJCP() {
    private val tag = V8ChallengeProvider::class.simpleName
    
    private val v8NpmLibFilename = listOf(
        "nsigsolver/polyfill.js", 
        "nsigsolver/meriyah-6.1.4.min.js", 
        "nsigsolver/astring-1.9.0.min.js"
    )
    
    private val v8Runtime = ThreadLocal<V8>()
    private val v8Lock = Any()

    override fun iterScriptSources(): Sequence<Pair<ScriptSource, (ScriptType) -> Script?>> = sequence {
        for ((source, func) in super.iterScriptSources()) {
            if (source == ScriptSource.WEB || source == ScriptSource.BUILTIN)
                yield(Pair(ScriptSource.BUILTIN, ::v8NpmSource))
            yield(Pair(source, func))
        }
    }

    private fun v8NpmSource(scriptType: ScriptType): Script? {
        
        if (scriptType != ScriptType.LIB)
            return null
        // V8-specific lib scripts that uses Deno NPM imports
        
        val code = loadScript(
            v8NpmLibFilename, 
            "Failed to read v8 challenge solver lib script"
        )

        return Script(
            scriptType, 
            ScriptVariant.V8_NPM,
            ScriptSource.BUILTIN, 
            scriptVersion, 
            code
        )
    }

    override fun runJsRuntime(stdin: String): String {
        synchronized(v8Lock) {
            try {
                initRuntime()
                return runV8(stdin)
            } finally {
                shutdownIfNeeded()
            }
        }
    }

    private fun runV8(stdin: String): String {
        val runtime = v8Runtime.get() ?: throw JsChallengeProviderError("V8 runtime not initialized yet")
        try {
            return runtime.withLock {
                it.executeStringScript(stdin) ?: throw JsChallengeProviderError("V8 runtime error: empty response")
            }
        } catch (e: V8ScriptExecutionException) {
            if (e.message?.contains("Invalid or unexpected token") ?: false)
                ie.cache.clear(cacheSection) // cached data broken?
            throw JsChallengeProviderError("V8 runtime error: ${e.message}", e)
        }
    }

    private fun initRuntime() {
        if (v8Runtime.get() != null)
            return
        v8Runtime.set(V8.createV8Runtime())
        runV8(constructCommonStdin()) // ignore the result, just warm up
    }

    private fun disposeRuntime() {
        val runtime = v8Runtime.get() ?: return

        // NOTE: getting lock fixes "Invalid V8 thread access: the locker has been released!"
        runtime.withLock {
            it.release(false)
        }
        v8Runtime.remove()
    }

    fun warmup() {
        synchronized(v8Lock) {
            initRuntime()
        }
    }

    fun shutdown() {
        synchronized(v8Lock) {
            disposeRuntime()
        }
    }

    fun forceRecreate() {
        synchronized(v8Lock) {
            disposeRuntime()

            initRuntime()
        }
    }

    private fun shutdownIfNeeded() {
        // NOTE: Possible Invalid thread access if using RxHelper runAsync
        // NOTE: Shutdown should run on the same thread that created V8 engine.
        disposeRuntime()
    }
}