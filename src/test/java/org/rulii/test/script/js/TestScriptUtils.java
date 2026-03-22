package org.rulii.test.script.js;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;

import javax.script.ScriptEngine;

public final class TestScriptUtils {

    private TestScriptUtils() {
        super();
    }

    public static ScriptEngine createEngine() {
        Engine engine = Engine.newBuilder("js")
                .option("engine.WarnInterpreterOnly", "false")
                .build();
        return GraalJSScriptEngine.create(engine,
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.ALL)
                        .allowHostClassLookup(s -> true)
                        .option("js.ecmascript-version", "2022"));
    }
}
