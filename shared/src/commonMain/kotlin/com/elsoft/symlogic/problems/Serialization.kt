package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * A custom SerializersModule that registers all the concrete implementations of the Rule interface.
 * This is necessary for kotlinx.serialization to handle polymorphism correctly.
 */
val appSerializersModule = SerializersModule {
    polymorphic(Rule::class) {
        // Rules of Inference
        subclass(ModusPonens::class)
        subclass(ModusTollens::class)
        subclass(HypotheticalSyllogism::class)
        subclass(DisjunctiveSyllogism::class)
        subclass(Simplification::class)
        subclass(Conjunction::class)
        subclass(Addition::class)
        subclass(ConstructiveDilemma::class)
        subclass(DestructiveDilemma::class)

        // Rules of Replacement
        subclass(DeMorgan::class)
        subclass(Commutativity::class)
        subclass(Associativity::class)
        subclass(Distribution::class)
        subclass(DoubleNegation::class)
        subclass(Transposition::class)
        subclass(MaterialImplication::class)
        subclass(MaterialEquivalence::class)
        subclass(Exportation::class)
        subclass(Tautology::class)
    }
}

/**
 * A globally configured Json instance that uses our custom serializers module.
 * All serialization/deserialization in the app should use this instance.
 */
val AppJson = Json {
    prettyPrint = true
    serializersModule = appSerializersModule
}
