/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.script.janino;

import org.codehaus.janino.*;
import org.rulii.bind.Bindings;
import org.rulii.lib.spring.util.Assert;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.ScriptCompiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * {@link ScriptCompiler} implementation that uses <a href="https://janino-compiler.github.io/janino/">Janino</a>
 * to JIT-compile Java script fragments into native bytecode at runtime.
 *
 * <h2>Compilation pipeline</h2>
 * <ol>
 *   <li>The raw script source is optionally pre-processed by {@link #autoReturn} — if the
 *       {@code returnType} is not {@code void} and the last statement is a bare expression
 *       (no explicit {@code return}), it is automatically wrapped in {@code return (expr);}
 *       so users can write concise one-liners like {@code ctx.age >= 18} as a condition.</li>
 *   <li>Each block statement in the (pre-processed) source is parsed by Janino's {@link Parser}
 *       and then passed through {@link ASTMutator}, which rewrites every {@code ctx.<name>}
 *       binding access into an explicit {@link Bindings} API call.</li>
 *   <li>The rewritten statements are serialised back to source text and handed to Janino's
 *       {@link ScriptEvaluator}, which compiles them to bytecode and exposes a single
 *       {@code evaluate(Object[])} entry point.</li>
 * </ol>
 *
 * <h2>Binding variable</h2>
 * <p>The compiled script receives the {@link Bindings} instance as a single parameter whose
 * name matches {@link #bindingsName} (default {@code "ctx"}).  The {@link ASTMutator} rewrites
 * {@code ctx.foo} reads and writes at the AST level so the script appears to access named
 * variables directly while actually calling {@code ctx.getValue("foo")} /
 * {@code ctx.setValueOrBind("foo", value)} at runtime.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ASTMutator
 * @see JaninoScriptProcessor
 * @see JaninoScriptProcessorFactory
 */
public class JaninoScriptCompiler implements ScriptCompiler {

    private final String languageName;
    private final String bindingsName;

    /**
     * Creates a new {@code JaninoScriptCompiler}.
     *
     * @param languageName the language identifier registered with the
     *                     {@link org.rulii.script.ScriptProcessorManager} (e.g. {@code "java"});
     *                     must not be null or empty.
     * @param bindingsName the name of the bindings variable inside scripts (e.g. {@code "ctx"});
     *                     must not be null or empty.
     */
    public JaninoScriptCompiler(String languageName, String bindingsName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.languageName = languageName;
        this.bindingsName = bindingsName;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a lazily-compiled {@link JITScript} that defers bytecode generation until the
     * first {@link JaninoScriptProcessor#evaluate evaluate} call, when live bindings are available.
     */
    @Override
    public String getLanguageName() {
        return languageName;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a lazily-compiled {@link JITScript} that defers bytecode generation until the
     * first {@link JaninoScriptProcessor#evaluate evaluate} call, when live bindings are available.
     */
    @Override
    public <T> Script<T> compile(String script, Class<?> returnType) {
        return new JITScript<>(languageName, script, returnType);
    }

    /**
     * Parses, mutates, and JIT-compiles a script against a concrete set of bindings.
     *
     * <p>This is the eagerly-compiling overload called by {@link JaninoScriptProcessor} on the
     * first evaluation.  Each {@code ctx.<name>} reference is resolved against {@code bindings}
     * at compile time to determine the correct cast type.
     *
     * @param script     the raw script source; must not be null or empty.
     * @param bindings   the live bindings snapshot used for compile-time type resolution;
     *                   must not be null.
     * @param returnType the expected return type of the script; {@code void.class} for actions.
     * @return a compiled {@link ScriptEvaluator} ready for repeated {@code evaluate(Object[])} calls.
     * @throws BuildScriptException if the script contains a syntax error, references an unknown
     *                              binding, or cannot otherwise be compiled.
     */
    public ScriptEvaluator compile(String script, Bindings bindings, Class<?> returnType) throws Exception {
        try {
            String source = (returnType != void.class) ? autoReturn(script) : script;
            Scanner scanner = new Scanner("Script.java", new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
            Parser parser = new Parser(scanner);

            while (parser.peek("import")) {
                parser.parseImportDeclaration();
            }

            ByteArrayOutputStream translatedStream = new ByteArrayOutputStream();

            while (!parser.peek(TokenType.END_OF_INPUT)) {
                Java.BlockStatement blockStatement = parser.parseBlockStatement();
                ASTMutator mutator = new ASTMutator(bindingsName, script, bindings);
                Java.BlockStatement rewritten = mutator.translate(blockStatement);
                translatedStream.write(rewritten.toString().getBytes(StandardCharsets.UTF_8));
                translatedStream.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            }

            String[] parameterNames = {bindingsName};
            Class<?>[] parameterTypes = {Bindings.class};

            return new ScriptEvaluator(translatedStream.toString(), returnType, parameterNames, parameterTypes);
        } catch (Exception e) {
            throw new BuildScriptException(script, e.getMessage(), e);
        }
    }

    /**
     * Pre-processes a script to add an implicit {@code return} when the script is a bare
     * expression rather than a statement.
     *
     * <p>This allows users to write concise scripts such as {@code ctx.age >= 18} instead of
     * {@code return ctx.age >= 18;} when the result is required (e.g. for a {@code Condition}
     * or {@code Function}).  The heuristic applies only when {@code returnType != void.class}.
     *
     * <p>Detection strategy:
     * <ol>
     *   <li>If the script already starts with {@code return}, it is left unchanged.</li>
     *   <li>The last top-level semicolon (outside string/char literals and comments) is located
     *       via {@link #lastTopLevelSemicolon}.</li>
     *   <li>The text after that semicolon is the candidate last expression.  If it is non-empty
     *       and does not itself start with {@code return}, it is wrapped as
     *       {@code return (<expr>);}.</li>
     * </ol>
     *
     * <p>Multi-statement scripts with an explicit trailing {@code return} are left unchanged.
     *
     * @param script the raw script source.
     * @return the (possibly modified) source with an implicit return added.
     */
    private static String autoReturn(String script) {
        String trimmed = script.trim();
        if (!trimmed.endsWith(";")) trimmed = trimmed + ";";
        // Strip trailing semicolon to expose the last expression
        String body = trimmed.endsWith(";") ? trimmed.substring(0, trimmed.length() - 1).trim() : trimmed;
        // Find where the last statement begins (after the last remaining semicolon)
        int lastSemi = lastTopLevelSemicolon(body);
        String lastStmt = lastSemi == -1 ? body : body.substring(lastSemi + 1).trim();
        if (lastStmt.isEmpty() || lastStmt.startsWith("return ") || lastStmt.startsWith("return(")) return script;
        String prefix = lastSemi == -1 ? "" : body.substring(0, lastSemi + 1) + "\n";
        return prefix + "return (" + lastStmt + ");";
    }

    /**
     * Returns the index of the last {@code ;} character in {@code s} that is not inside a
     * string literal, character literal, line comment, or block comment.
     *
     * @param s the source text to scan.
     * @return the index of the last top-level semicolon, or {@code -1} if none is found.
     */
    private static int lastTopLevelSemicolon(String s) {
        int result = -1;
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char next = i + 1 < s.length() ? s.charAt(i + 1) : 0;
            if (inLineComment) {
                if (c == '\n') inLineComment = false;
            } else if (inBlockComment) {
                if (c == '*' && next == '/') { inBlockComment = false; i++; }
            } else if (inString) {
                if (c == '\\') i++;
                else if (c == '"') inString = false;
            } else if (inChar) {
                if (c == '\\') i++;
                else if (c == '\'') inChar = false;
            } else {
                if (c == '/' && next == '/') { inLineComment = true; i++; }
                else if (c == '/' && next == '*') { inBlockComment = true; i++; }
                else if (c == '"') inString = true;
                else if (c == '\'') inChar = true;
                else if (c == ';') result = i;
            }
        }
        return result;
    }
}
