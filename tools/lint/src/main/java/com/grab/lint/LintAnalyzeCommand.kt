package com.grab.lint

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString
import com.android.tools.lint.Main as LintCli

class LintAnalyzeCommand : LintBaseCommand() {

    override val createProjectXml: Boolean = true

    override fun run(
        workingDir: Path,
        projectXml: File,
        tmpBaseline: File,
    ) {
        val cliArgs = (defaultLintOptions + listOf(
            "--cache-dir", workingDir.resolve("cache").pathString,
            "--project", projectXml.toString(),
            "--analyze-only" // Only do analyze
        )).toTypedArray()
        LintCli().run(cliArgs)
        sanitizePartialResults()
    }

    private fun sanitizePartialResults() = runBlocking {
        val concurrency = Runtime.getRuntime().availableProcessors() / 2
        partialResults.walkTopDown().filter { it.isFile }.asFlow()
        .flatMapMerge(concurrency) { partialResult ->
            flow { emit(partialResult) }
                .map { sanitizer.sanitize(it) }
                .flowOn(Dispatchers.IO)
        }.collect()
    }
}