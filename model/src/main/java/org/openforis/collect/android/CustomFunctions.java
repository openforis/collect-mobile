package org.openforis.collect.android;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;

import java.util.*;

public abstract class CustomFunctions implements Functions {
    private final Map<FunctionKey, Function> functions = new HashMap<FunctionKey, Function>();

    protected abstract String getNamespace();

    @Override
    public Set getUsedNamespaces() {
        return Collections.singleton("test");
    }

    @Override
    public Function getFunction(String namespace, String name, Object[] parameters) {
        return functions.get(new FunctionKey(name, parameters.length));
    }

    protected void registerFunction(String name, int parameterCount, Function function) {
        functions.put(new FunctionKey(name, parameterCount), function);
    }

    public static class TestFunctions extends CustomFunctions {
        public TestFunctions() {
            registerFunction("doSomething", 2, new Function() {
                public Object invoke(ExpressionContext expressionContext, Object... parameters) {
                    return doTheThing((String) parameters[0], (String) parameters[1]);
                }
            });
        }

        @Override
        protected String getNamespace() {
            return "test";
        }

        private Object doTheThing(String param1, String param2) {
            return String.format("Done something to %s and %s", param1, param2);
        }
    }



    private static class FunctionKey {
        public final String name;
        public final int parameterCount;

        public FunctionKey(String name, int parameterCount) {
            this.name = name;
            this.parameterCount = parameterCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FunctionKey that = (FunctionKey) o;

            if (parameterCount != that.parameterCount) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + parameterCount;
            return result;
        }
    }
}
