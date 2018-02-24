/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.oracle.truffle.regex;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.regex.result.RegexResult;
import com.oracle.truffle.regex.runtime.RegexObjectExecMethod;
import com.oracle.truffle.regex.runtime.RegexObjectMessageResolutionForeign;

public class RegexObject implements RegexLanguageObject {

    private final RegexEngine engine;
    private final RegexSource source;
    private CompiledRegex compiledRegex;
    private final RegexObjectExecMethod execMethod;
    private RegexProfile regexProfile;

    public RegexObject(RegexEngine engine, RegexSource source) {
        this.engine = engine;
        this.source = source;
        execMethod = new RegexObjectExecMethod(this);
        if (source.getOptions().isRegressionTestMode()) {
            // compile expression eagerly in regression test mode
            getCompiledRegex();
        }
    }

    public RegexSource getSource() {
        return source;
    }

    public CompiledRegex getCompiledRegex() {
        if (compiledRegex == null) {
            compiledRegex = compileRegex();
        }
        return compiledRegex;
    }

    @CompilerDirectives.TruffleBoundary
    private CompiledRegex compileRegex() {
        try {
            return engine.compile(source);
        } catch (RegexSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCompiledRegex(CompiledRegex compiledRegex) {
        this.compiledRegex = compiledRegex;
    }

    /**
     * A call target to the underlying {@link RegexExecRootNode} which will return a
     * {@link RegexResult} object. The signature of this operation corresponds to:
     * <code>{@link RegexResult} find(String input, int fromIndex);</code>
     */
    public CallTarget getCallTarget() {
        return getCompiledRegex().getRegexCallTarget();
    }

    public RegexObjectExecMethod getExecMethod() {
        return execMethod;
    }

    public RegexProfile getRegexProfile() {
        if (regexProfile == null) {
            regexProfile = new RegexProfile();
        }
        return regexProfile;
    }

    public static boolean isInstance(TruffleObject object) {
        return object instanceof RegexObject;
    }

    @Override
    public ForeignAccess getForeignAccess() {
        return RegexObjectMessageResolutionForeign.ACCESS;
    }
}
