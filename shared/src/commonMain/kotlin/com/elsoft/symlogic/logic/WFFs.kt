package com.elsoft.symlogic.logic

/**
 * Normalizes a string containing special logic symbols into a canonical ASCII format
 * that the parser can understand.
 *
 * @param input The string with special symbols (e.g., "p → (q ∧ ~r)").
 * @return The normalized string (e.g., "p -> (q & ~r)").
 */
fun normalizeWffString(input: String): String {
    return input
        .replace("¬", "~")
        .replace("~", "~") // Allow both negation symbols
        .replace("∧", "&")
        .replace("·", "&") // Allow dot for conjunction
        .replace("∨", "|")
        .replace("→", "->")
        .replace("⊃", "->") // Allow horseshoe for implication
        .replace("↔", "<->")
        .replace("≡", "<->") // Allow triple bar for equivalence
}
