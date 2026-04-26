package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.Expression
import com.elsoft.symlogic.problems.ProblemSetRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProblemSetRepositoryTest {

    @Test
    fun testSaveAndLoadProblemSet_Jvm(): Unit = runBlocking {
        // 1. Create a sample ProblemSet
        val problem = ProblemDefinition(
            id = "Test-1",
            premises = listOf(
                Expression.Implies(Expression.Variable("P"), Expression.Variable("Q")),
                Expression.Variable("P")
            ),
            conclusion = Expression.Variable("Q")
        )
        val problemSet = ProblemSet(name = "My Test Set", problems = listOf(problem))
        
        val filename = "test_problem_set.json"
        val repository = getProblemSetRepository()

        // 2. Save the ProblemSet
        repository.saveProblemSet(problemSet, filename)

        // 3. Load the ProblemSet back
        val loadedProblemSet = repository.loadProblemSet(filename)

        // 4. Assert that the loaded data is correct
        assertNotNull(loadedProblemSet)
        assertEquals(problemSet.name, loadedProblemSet.name)
        assertEquals(1, loadedProblemSet.problems.size)
        assertEquals(problem.id, loadedProblemSet.problems.first().id)
        assertEquals(problem.conclusion, loadedProblemSet.problems.first().conclusion)
        
        // Clean up the test file
        File(filename).delete()
    }
}
