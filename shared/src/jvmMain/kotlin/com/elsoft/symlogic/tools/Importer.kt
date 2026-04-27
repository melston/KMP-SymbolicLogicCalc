package com.elsoft.symlogic.tools

import com.elsoft.symlogic.problems.JvmProblemSetRepository
import com.elsoft.symlogic.problems.parsers.ProblemSetParser
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * A command-line tool to import a plain text problem set file into the application's
 * managed storage.
 */
fun main(args: Array<String>) = runBlocking {
    if (args.isEmpty()) {
        println("Usage: please provide the path to the problem set file to import.")
        return@runBlocking
    }

    val filePath = args[0]
    val file = File(filePath)

    if (!file.exists()) {
        println("Error: File not found at '$filePath'")
        return@runBlocking
    }

    println("Reading file: ${file.absolutePath}")
    val fileContent = file.readText()

    try {
        // The name of the problem set is derived from the filename
        val setName = file.nameWithoutExtension.replace("_", " ").capitalize()
        
        val parser = ProblemSetParser()
        val problemSet = parser.parse(setName, fileContent)
        
        println("Successfully parsed '${problemSet.name}' with ${problemSet.problems.size} problems.")

        val repository = JvmProblemSetRepository()
        repository.saveProblemSet(problemSet)
        
        println("Problem set saved successfully to application storage.")
        println("You can now run the main application to see it in the list.")

    } catch (e: Exception) {
        println("An error occurred during parsing or saving:")
        e.printStackTrace()
    }
}
