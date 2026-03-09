package org.rulii.test.script.js;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.script.Script;

import javax.script.ScriptEngine;

public class ScriptBuilderTest {

    @Test
    public void test1() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 10);
        bindings.bind("b", int.class, 20);
        bindings.bind("c", int.class, 0);

        Script<Integer> script = Script.builder().build(createEngine(), "b.c = b.a + b.b;");
        Integer result = script.run(bindings);
        Assertions.assertEquals(30, result);
    }

    private static ScriptEngine createEngine() {
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
