# Using the Application

This guide explains how to use the different features of the Symbolic Logic Game application.

## Parts of the UI

### Entry Screen

This is the main menu of the application. From here you can navigate to the different
sections of the app:
- **Practice with Generated Problems**: Creates a new, random logic problem for you to solve.
- **Solve Pre-written Problems**: Lets you browse and solve problems from saved sets, 
  including the "Generated" set where your saved generated problems are stored.
- **Manage Problem Sets**: Allows you to create, edit, delete, and import problem sets.
- **Help**: Brings you to this help screen.

### Generating Problems

On this screen, you can create a random logic problem. You can select specific rules you 
want the problem to focus on and choose a target number of steps for the solution. 
Click "Generate Problem" to create a new challenge and start solving.

### Creating New Problems

You can create your own problems and save them in custom sets.
1. Go to **Manage Problem Sets**.
2. Select an existing set or create a new one.
3. In the right-hand pane, click the **+** icon to open the "Add Problem" dialog.
4. Fill in the **Problem ID** (a unique name), the **Premises** (one per line), 
   and the **Conclusion**.
5. Click **Save**.

### Importing Problem Sets

The app supports importing problem sets from plain text files. This is useful for 
loading homework assignments or sharing problems.
1. Go to **Manage Problem Sets**.
2. In the top-left, click the **Import from File** icon (upward arrow).
3. In the dialog, enter a unique name for the new problem set.
4. Click the **Load from File** button and select your `.txt` file.
5. Click **Import and Save**.

This will parse the file and save the resulting problem set with the name you provided.

#### Pre-Written Problem Format

The plain text importer expects a specific format. Each problem starts with
an identifier on a line by itself.  The identifier is simply a name of the problem.
This is, followed by premises on lines starting with
`Premises:`, also on a line by itself, followed by all the premises for the problem, each
on their own line.  While it is not required, indenting the premises by a space or two makes
the problem easier to verify as you create it.  Finally, you need the word `Prove:` on a line
by itself, followed by the conclusion to be proved.

**Example:**
```text
Exercise 1.1
Premises:
  p -> q
  p
Prove:
  q

Exercise 1.2
Premises:
  p -> q
  ~q
Prove:
  ~p
```

### Solving Problems - The Game Screen

This is the main screen where you solve a proof. The premises and the target conclusion 
are displayed at the top. Your goal is to derive the conclusion from the premises using 
the available rules.

#### Selecting Premises/WFFs

To use an existing line (a premise or a previously derived step) as a parent for a 
new step, simply click on it. It will become highlighted. You can select multiple lines.

#### Adding Derived WFFs

1. Select the parent line(s) you want to use.
2. Click the **+** FAB (Floating Action Button) at the bottom right.
3. In the dialog, the "Parent IDs" field will be pre-filled.
4. The "Rule" dropdown will automatically filter to show only the rules that can be 
   applied to your selected parents.
5. Select the correct rule.
6. Type the new Well-Formed Formula (WFF) that you derived into the "New WFF" field.
7. The "Add Step" button will become enabled when your new WFF is a valid result of 
   applying the rule to the parents. Click it to add the step to your proof.

#### Adding Sub-Proofs (Implication Introduction)

To prove an implication (e.g., `A -> B`), you can start a sub-proof.
1. Click the **+** FAB.
2. In the dialog, type the antecedent (the "if" part, `A`) into the "New WFF" field.
3. Click the **Add Assumption** button. This will add the WFF to your proof as an indented
   step, starting the sub-proof.
4. Continue deriving steps within the sub-proof until you reach the consequent 
   (the "then" part, `B`).
5. Once `B` is the last line of your sub-proof, click the **+** FAB again.
6. Click the **Close Sub-proof (II)** button. This will add the final `A -> B` 
   implication to your proof.

# Rules

## Rules of Inference

This section describes the basic rules of inference used to derive new truths from 
existing premises. Each of these takes one or more existing WFFs and combines them 
into a new WFF that can be logically derived from the selected WFF(s).

### Modus Ponens (MP)
Also known as "affirming the antecedent," this rule states that if you have an 
implication, and you also have its antecedent (the "if" part), you can conclude the 
consequent (the "then" part).
##### Form:
```text
1. P -> Q
2. P
∴ Q
```

### Modus Tollens (MT)
Also known as "denying the consequent," this rule states that if you have an 
implication, and you also have the negation of its consequent (the "then" part), 
you can conclude the negation of the antecedent (the "if" part).
##### Form:
```text
1. P -> Q
2. ~Q
∴ ~P
```

### Hypothetical Syllogism (HS)
This rule allows you to chain two implications together. If the consequent of one 
implication is the same as the antecedent of another, you can create a new implication 
that links the beginning of the first to the end of the second.
##### Form:
```text
1. P -> Q
2. Q -> R
∴ P -> R
```

### Disjunctive Syllogism (DS)
If you have a disjunction (an "or" statement) and you also have the negation of one 
of its sides, you can conclude that the other side must be true.
##### Form:
```text
1. P | Q
2. ~P
∴ Q
```

### Simplification (Simp)
If you have a conjunction (an "and" statement), you can conclude either of its 
parts individually.
##### Form:
```text
1. P & Q
∴ P
```

### Conjunction (Conj)
If you have two separate true statements, you can combine them into a single true 
statement with a conjunction ("and").
##### Form:
```text
1. P
2. Q
∴ P & Q
```

### Addition (Add)
If you have a true statement, you can create a disjunction ("or") by combining it 
with any other statement. This is because if `P` is true, then `P | Q` must also be true, 
regardless of what `Q` is.
##### Form:
```text
1. P
∴ P | Q
```

### Constructive Dilemma (CD)
Given two implications and the disjunction of their antecedents, you can conclude 
the disjunction of their consequents.
##### Form:
```text
1. (P -> Q) & (R -> S)
2. P | R
∴ Q | S
```

### Destructive Dilemma (DD)
Given two implications and the disjunction of the negations of their consequents, 
you can conclude the disjunction of the negations of their antecedents.
##### Form:
```text
1. (P -> Q) & (R -> S)
2. ~Q | ~S
∴ ~P | ~R
```

### Reiteration (R)
This rule allows you to repeat a line that has already been proven in an outer scope. 
This is most useful for bringing an already established fact into a sub-proof.
##### Form:
```text
1. P
...
∴ P (in a sub-proof)
```

## Rules of Replacement
These rules differ from Rules of Inference in that they are statements of logical 
equivalence. They can be applied to an entire line or to just a part of a line.

### De Morgan's Rules (DM)
These rules describe how to distribute a negation over a conjunction or disjunction.
- `~(P & Q)` is equivalent to `~P | ~Q`
- `~(P | Q)` is equivalent to `~P & ~Q`

### Commutativity (Com)
The order of operands does not matter for conjunction or disjunction.
- `(P & Q)` is equivalent to `(Q & P)`
- `(P | Q)` is equivalent to `(Q | P)`

### Associativity (Assoc)
The grouping of operands does not matter when they have the same operator.
- `(P & (Q & R))` is equivalent to `((P & Q) & R)`
- `(P | (Q | R))` is equivalent to `((P | Q) | R)`

### Distribution (Dist)
This rule describes how to distribute one operator over another.
- `(P & (Q | R))` is equivalent to `((P & Q) | (P & R))`
- `(P | (Q & R))` is equivalent to `((P | Q) & (P | R))`

### Double Negation (DN)
A statement is equivalent to the negation of its negation.
- `P` is equivalent to `~~P`

### Transposition (Trans)
An implication is equivalent to an implication where the two sides are swapped and negated.
- `(P -> Q)` is equivalent to `(~Q -> ~P)`

### Material Implication (MI)
An implication is equivalent to a disjunction of the negation of its antecedent 
and its consequent.
- `(P -> Q)` is equivalent to `(~P | Q)`

### Material Equivalence (ME)
A biconditional is equivalent to a conjunction of two implications.
- `(P <-> Q)` is equivalent to `((P -> Q) & (Q -> P))`
- `(P <-> Q)` is equivalent to `((P & Q) | (~P & ~Q))`

### Exportation (Exp)
This rule relates nested implications to conjunctions.
- `((P & Q) -> R)` is equivalent to `(P -> (Q -> R))`

### Tautology (Taut)
A statement is equivalent to a conjunction or disjunction with itself.
- `P` is equivalent to `(P & P)`
- `P` is equivalent to `(P | P)`

### Identity
A disjunction with a contradiction is equivalent to the other part. A conjunction
with a tautology is equivalent to the other part.
- `(P | (Q & ~Q))` is equivalent to `P`
- `(P & (Q | ~Q))` is equivalent to `P`
```
