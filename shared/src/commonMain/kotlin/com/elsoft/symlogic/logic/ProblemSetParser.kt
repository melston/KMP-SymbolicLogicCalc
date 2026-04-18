package com.elsoft.symlogic.logic

class ProblemSetParser {
    
    class ParseException(message: String, val lineNumber: Int) : Exception("Line $lineNumber: $message")

    /**
     * Parses a block of text into a ProblemSet containing multiple ProblemDefinitions.
     * 
     * Expected format:
     * <Identifier>
     * Premises:
     *     <WFF>
     *     <WFF>
     * Prove:
     *     <WFF>
     * 
     * Problems can be separated by blank lines or a line starting with three or more dashes (e.g., "---").
     */
    fun parse(name: String, input: String): ProblemSet {
        val lines = input.lines()
        val problems = mutableListOf<ProblemDefinition>()
        val exprParser = ExpressionParser()

        var currentId: String? = null
        var currentPremises = mutableListOf<Expression>()
        var parsingState = State.IDENTIFIER

        var lineNumber = 0
        for (rawLine in lines) {
            lineNumber++
            val line = rawLine.trim()

            // Treat a line starting with 3 dashes as a visual separator (just like a blank line)
            val isSeparator = line.startsWith("---")
            
            // Skip empty lines or visual separators, unless we just finished a problem and are looking for the next identifier
            if (line.isEmpty() || isSeparator) {
                if (parsingState == State.DONE) {
                    parsingState = State.IDENTIFIER // Reset state for the next problem block
                }
                continue
            }

            try {
                when (parsingState) {
                    State.IDENTIFIER -> {
                        currentId = line
                        currentPremises = mutableListOf()
                        parsingState = State.EXPECT_PREMISES
                    }
                    State.EXPECT_PREMISES -> {
                        if (line.equals("Premises:", ignoreCase = true)) {
                            parsingState = State.READING_PREMISES
                        } else {
                            throw ParseException("Expected 'Premises:', but found '$line'", lineNumber)
                        }
                    }
                    State.READING_PREMISES -> {
                        if (line.equals("Prove:", ignoreCase = true)) {
                            parsingState = State.EXPECT_CONCLUSION
                        } else {
                            // Parse as a premise WFF
                            currentPremises.add(exprParser.parse(line))
                        }
                    }
                    State.EXPECT_CONCLUSION -> {
                        // The line after Prove: is the target conclusion
                        val conclusion = exprParser.parse(line)
                        problems.add(ProblemDefinition(currentId!!, currentPremises, conclusion))
                        parsingState = State.DONE
                    }
                    State.DONE -> {
                        // Found text when we expected a blank line/separator separating problems
                        // We will allow consecutive problems without blank lines by treating this line as the next ID.
                        currentId = line
                        currentPremises = mutableListOf()
                        parsingState = State.EXPECT_PREMISES
                    }
                }
            } catch (e: ExpressionParser.ParseException) {
                throw ParseException("WFF Parsing Error: ${e.message}", lineNumber)
            }
        }

        // Catch an incomplete final block
        if (parsingState != State.DONE && parsingState != State.IDENTIFIER) {
            throw Exception("Unexpected end of file while parsing problem '$currentId'. Current state: $parsingState")
        }

        return ProblemSet(name, problems)
    }

    private enum class State {
        IDENTIFIER, EXPECT_PREMISES, READING_PREMISES, EXPECT_CONCLUSION, DONE
    }
}
